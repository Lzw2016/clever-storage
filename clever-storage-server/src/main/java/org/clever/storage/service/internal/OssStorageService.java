package org.clever.storage.service.internal;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.clever.storage.config.GlobalConfig;
import org.clever.storage.dto.request.UploadFileReq;
import org.clever.storage.entity.EnumConstant;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.mapper.FileInfoMapper;
import org.clever.storage.utils.StoragePathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-27 21:58 <br/>
 */
@Service("OssStorageService")
@Slf4j
public class OssStorageService extends AbstractStorageService implements Closeable {

    @Autowired
    private FileInfoMapper fileInfoMapper;
    /**
     * 文件存储节点, bucketName
     */
    private final String storedNode;
    private final String endpoint;
    private final OSS ossClient;

    public OssStorageService(GlobalConfig globalConfig) {
        GlobalConfig.AliOssConfig aliOssConfig = globalConfig.getAliOssConfig();
        storedNode = aliOssConfig.getBucketName();
        endpoint = aliOssConfig.getEndpoint();
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
    protected FileInfoMapper getFileInfoMapper() {
        return fileInfoMapper;
    }

    @Override
    protected String getStoredNode() {
        return storedNode;
    }

    @Override
    protected Integer getStoredType() {
        return EnumConstant.StoredType_3;
    }

    // 秒传文件处理
    @Override
    protected void internalLazySaveFile(FileInfo dbFileInfo, FileInfo newFileInfo) {
        // 私有读 -> 公开可读
        if (Objects.equals(dbFileInfo.getPublicRead(), EnumConstant.PublicRead_0) && Objects.equals(newFileInfo.getPublicRead(), EnumConstant.PublicRead_1)) {
            // 私有文件
            String privateFilePath = dbFileInfo.getFilePath() + "/" + dbFileInfo.getNewName();
            // // 公开文件
            // newFileInfo.setFilePath(StoragePathUtils.generateFilePathByDate(newFileInfo.getFileSource(), "/"));
            // newFileInfo.setNewName(StoragePathUtils.generateNewFileName(newFileInfo.getFileName()));
            // String publicFilePath = newFileInfo.getFilePath() + "/" + newFileInfo.getNewName();
            // CopyObjectRequest copyObjectRequest = new CopyObjectRequest(storedNode, privateFilePath, storedNode, publicFilePath);
            // ossClient.copyObject(copyObjectRequest);
            // ossClient.setObjectAcl(storedNode, publicFilePath, CannedAccessControlList.PublicRead);
            ossClient.setObjectAcl(storedNode, privateFilePath, CannedAccessControlList.PublicRead);
        }
    }

    // 上传文件
    @Override
    protected void internalSaveFile(FileInfo fileInfo, UploadFileReq uploadFileReq, long uploadTime, MultipartFile multipartFile) throws IOException {
        // 上传文件存储到当前服务器的路径(相对路径，相对于 FILE_STORAGE_PATH)
        String filePath = StoragePathUtils.generateFilePathByDate(fileInfo.getFileSource(), "/");
        fileInfo.setFilePath(filePath);
        // 计算文件的绝对路径，保存文件
        String absoluteFilePath = filePath + "/" + fileInfo.getNewName();
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
            // RateLimiter rateLimiter = RateLimiter.create(1024 * 8);
            // putObjectRequest.setProgressListener(progressEvent -> {
            //     if (ProgressEventType.TRANSFER_COMPLETED_EVENT == progressEvent.getEventType()) {
            //         log.info("[阿里云OSS]上传文件成功 FileKey={}", absoluteFilePath);
            //     }
            //     if (ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT == progressEvent.getEventType()) {
            //         double sleepTime = rateLimiter.acquire((int) progressEvent.getBytes());
            //         log.info("[阿里云OSS] 上传字节:[{}], 休眠时间:[{}]秒", progressEvent.getBytes(), sleepTime);
            //     }
            // });
            ossClient.putObject(putObjectRequest);
            log.info("[阿里云OSS]上传文件成功 FileKey={}", absoluteFilePath);
        }
        long storageEnd = System.currentTimeMillis();
        // 设置存储所用的时间
        fileInfo.setStoredTime(storageEnd - storageStart);
        log.info("[阿里云OSS]文件存储所用时间:[{}ms]", fileInfo.getStoredTime());
        // 设置公开可读的Url
        if (Objects.equals(EnumConstant.PublicRead_1, fileInfo.getPublicRead())) {
            // https://clever-dev-bucket.oss-cn-hangzhou.aliyuncs.com/test/2018/2018-12/2018-12-28/d470044c-5d93-4e95-a7d8-17b43ff4441d.png
            fileInfo.setReadUrl(String.format(
                    "https://%s.%s/%s",
                    storedNode,
                    endpoint.replaceAll("http://", "").replaceAll("https://", ""),
                    absoluteFilePath
            ));
        }
    }

    // 限速打开文件
    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void internalOpenFileSpeedLimit(FileInfo fileInfo, OutputStream outputStream, long off, long len, long maxSpeed) throws IOException {
        RateLimiter rateLimiter = null;
        if (maxSpeed > 0) {
            rateLimiter = RateLimiter.create(maxSpeed);
        }
        String absoluteFilePath = fileInfo.getFilePath() + "/" + fileInfo.getNewName();
        GetObjectRequest getObjectRequest = new GetObjectRequest(storedNode, absoluteFilePath);
        // 获取0~1000字节范围内的数据，包括0和1000，共1001个字节的数据。如果指定的范围无效（比如开始或结束位置的指定值为负数，或指定值大于文件大小），则下载整个文件。
        if (off >= 0) {
            if (len > 0) {
                getObjectRequest.setRange(off, off + len - 1);
            } else {
                getObjectRequest.setRange(off, fileInfo.getFileSize() - 1);
            }
        }
        OSSObject ossObject = ossClient.getObject(getObjectRequest);
        try (InputStream inputStream = ossObject.getObjectContent()) {
            byte[] data = new byte[8 * 1024];
            int readByte;
            double sleepTime = 0;
            while (true) {
                readByte = inputStream.read(data);
                if (readByte == 0) {
                    continue;
                }
                if (readByte < 0) {
                    break;
                }
                outputStream.write(data, 0, readByte);
                if (rateLimiter != null) {
                    sleepTime = rateLimiter.acquire(readByte);
                }
                log.debug("[阿里云OSS]打开文件NewName:[{}], 读取字节数:[{}], 休眠时间:[{}]秒", fileInfo.getNewName(), readByte, sleepTime);
            }
            outputStream.flush();
        }
    }

    // 彻底删除文件
    @Override
    protected void internalDeleteFile(FileInfo fileInfo) {
        String absoluteFilePath = fileInfo.getFilePath() + "/" + fileInfo.getNewName();
        ossClient.deleteObject(storedNode, absoluteFilePath);
    }

    @Override
    public boolean isExists(FileInfo fileInfo) {
        if (fileInfo == null) {
            return false;
        }
        String absoluteFilePath = fileInfo.getFilePath() + "/" + fileInfo.getNewName();
        boolean exists = ossClient.doesObjectExist(storedNode, absoluteFilePath);
        if (exists) {
            return true;
        }
        log.warn("[阿里云OSS]文件引用[NewName={}]对应的文件不存在", fileInfo.getNewName());
        return false;
    }

    @Override
    public void close() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
