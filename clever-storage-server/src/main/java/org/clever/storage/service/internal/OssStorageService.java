package org.clever.storage.service.internal;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.clever.common.server.service.BaseService;
import org.clever.storage.config.GlobalConfig;
import org.clever.storage.dto.request.UploadFileReq;
import org.clever.storage.entity.EnumConstant;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.mapper.FileInfoMapper;
import org.clever.storage.service.IStorageService;
import org.clever.storage.utils.FileDigestUtils;
import org.clever.storage.utils.FileUploadUtils;
import org.clever.storage.utils.StoragePathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-27 21:58 <br/>
 */
@Transactional(readOnly = true)
@Service("OssStorageService")
@Slf4j
public class OssStorageService extends BaseService implements IStorageService, Closeable {

    @Autowired
    private FileInfoMapper fileInfoMapper;
    /**
     * 文件存储节点, bucketName
     */
    private final String storedNode;
    private final OSS ossClient;

    public OssStorageService(GlobalConfig globalConfig) {
        GlobalConfig.AliOssConfig aliOssConfig = globalConfig.getAliOssConfig();
        storedNode = aliOssConfig.getBucketName();
        ossClient = new OSSClientBuilder().build(aliOssConfig.getEndpoint(), aliOssConfig.getAccessKeyId(), aliOssConfig.getAccessKeySecret());
        if (!ossClient.doesBucketExist(aliOssConfig.getBucketName())) {
            log.info("[阿里云OSS]创建存储空间 BucketName={}", aliOssConfig.getBucketName());
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(aliOssConfig.getBucketName());
            createBucketRequest.setCannedACL(CannedAccessControlList.Private);
            // createBucketRequest.setProgressListener();
            ossClient.createBucket(createBucketRequest);
        }
    }

    @Override
    public FileInfo lazySaveFile(UploadFileReq uploadFileReq, long uploadTime, String fileName, String digest, Integer digestType) {
        return null;
    }

    /**
     * TODO 事务不可长
     */
    @Transactional
    @Override
    public FileInfo saveFile(UploadFileReq uploadFileReq, long uploadTime, MultipartFile multipartFile) throws Exception {
        // 设置文件签名类型 和 文件签名
        String digest;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            digest = FileDigestUtils.FileDigestByMD5(inputStream);
        }
        // 通过文件签名检查服务器端是否有相同文件
        FileInfo lazyFileInfo = this.lazySaveFile(uploadFileReq, uploadTime, multipartFile.getOriginalFilename(), digest, EnumConstant.DigestType_1);
        if (lazyFileInfo != null) {
            return lazyFileInfo;
        }
        // 服务器端不存在相同文件
        FileInfo fileInfo = new FileInfo();
        fileInfo = FileUploadUtils.fillFileInfo(fileInfo, multipartFile);
        fileInfo.setPublicRead(uploadFileReq.getPublicRead());
        fileInfo.setPublicWrite(uploadFileReq.getPublicWrite());
        fileInfo.setFileSource(uploadFileReq.getFileSource());
        fileInfo.setUploadTime(uploadTime);
        fileInfo.setDigest(digest);
        fileInfo.setDigestType(EnumConstant.DigestType_1);
        // 上传文件的存储类型：当前服务器硬盘
        fileInfo.setStoredType(EnumConstant.StoredType_3);
        fileInfo.setStoredNode(storedNode);
        // 设置文件存储之后的名称：UUID + 后缀名(此操作依赖文件原名称)
        String newName = StoragePathUtils.generateNewFileName(fileInfo.getFileName());
        fileInfo.setNewName(newName);
        fileInfo.setFileSuffix(FilenameUtils.getExtension(fileInfo.getFileName()).toLowerCase());
        // 上传文件存储到当前服务器的路径(相对路径，相对于 FILE_STORAGE_PATH)
        String filePath = StoragePathUtils.generateFilePathByDate("", "/");
//        String filePath = StoragePathUtils.generateFilePathByDate(fileInfo.getFileSource(), "/");
        fileInfo.setFilePath(filePath);
        // 计算文件的绝对路径，保存文件
        String absoluteFilePath = filePath + File.separator + newName;
        // 上传文件到阿里云OSS
        long storageStart = System.currentTimeMillis();
        try (InputStream inputStream = multipartFile.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(storedNode, absoluteFilePath, inputStream);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setObjectAcl(CannedAccessControlList.Private);
            if (Objects.equals(fileInfo.getPublicRead(), EnumConstant.PublicRead_0)) {
                objectMetadata.setObjectAcl(CannedAccessControlList.Private);
            }
            if (Objects.equals(fileInfo.getPublicRead(), EnumConstant.PublicRead_1)) {
                objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);
            }
            putObjectRequest.setMetadata(objectMetadata);
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            log.info("[阿里云OSS]上传文件成功 FileKey={}", absoluteFilePath);
            log.info("[阿里云OSS]上传文件成功 FileKey={}", putObjectResult);
        }
        long storageEnd = System.currentTimeMillis();
        // 设置存储所用的时间
        fileInfo.setStoredTime(storageEnd - storageStart);
        log.info("[阿里云OSS]文件存储所用时间:[{}ms]", fileInfo.getStoredTime());
        // TODO 设置公开可读的Url
//        if (Objects.equals(EnumConstant.PublicRead_1, fileInfo.getPublicRead())) {
//            fileInfo.setReadUrl("");
//        }
        // 保存文件信息
        fileInfoMapper.insert(fileInfo);
        return fileInfo;
    }

    @Override
    public boolean isExists(FileInfo fileInfo) {
        return false;
    }

    @Override
    public FileInfo getFileInfo(String newName) {
        return null;
    }

    @Override
    public FileInfo getFileInfo(Long fileId) {
        return null;
    }

    @Override
    public void openFileSpeedLimit(FileInfo fileInfo, OutputStream outputStream, long off, long len, long maxSpeed) throws IOException {

    }

    @Override
    public FileInfo deleteFile(Long fileId) {
        return null;
    }

    @Override
    public void close() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
