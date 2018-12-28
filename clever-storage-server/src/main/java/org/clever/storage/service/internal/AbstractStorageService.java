package org.clever.storage.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.exception.BusinessException;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.storage.dto.request.UploadFileReq;
import org.clever.storage.entity.EnumConstant;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.mapper.FileInfoMapper;
import org.clever.storage.service.IStorageService;
import org.clever.storage.utils.FileDigestUtils;
import org.clever.storage.utils.FileUploadUtils;
import org.clever.storage.utils.StoragePathUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-27 12:02 <br/>
 */
@Transactional(readOnly = true)
@Slf4j
public abstract class AbstractStorageService implements IStorageService {

    protected abstract FileInfoMapper getFileInfoMapper();

    protected abstract String getStoredNode();

    protected abstract Integer getStoredType();

    @Override
    public FileInfo lazySaveFile(UploadFileReq uploadFileReq, long uploadTime, String fileName, String digest, Integer digestType) {
        if (StringUtils.isBlank(digest) || digestType == null) {
            return null;
        }
        // 到数据库查找判断此文件是否已经上传过了 - 此文件是否已经上传过了，不需要重复保存
        FileInfo dbFileInfo = getFileInfoMapper().getFileInfoByDigest(digest, digestType, getStoredType());
        if (dbFileInfo == null) {
            log.debug("秒传失败，文件没有上传过");
            return null;
        }
        if (StringUtils.isBlank(dbFileInfo.getFilePath()) || StringUtils.isBlank(dbFileInfo.getNewName())) {
            log.warn("秒传失败，数据库里文件(FilePath、NewName)信息为空，文件ID={}", dbFileInfo.getId());
            return null;
        }
        if (!isExists(dbFileInfo)) {
            log.warn("秒传失败，上传文件不存在(可能已经被删除)，文件路径[{}]", dbFileInfo.getFilePath() + File.separator + dbFileInfo.getNewName());
            return null;
        }
        // 需要新增记录
        FileInfo newFileInfo = BeanMapper.mapper(dbFileInfo, FileInfo.class);
        newFileInfo.setId(null);
        newFileInfo.setUpdateAt(null);
        newFileInfo.setCreateAt(null);
        newFileInfo.setPublicRead(uploadFileReq.getPublicRead());
        newFileInfo.setPublicWrite(uploadFileReq.getPublicWrite());
        newFileInfo.setFileSource(uploadFileReq.getFileSource());
        newFileInfo.setUploadTime(uploadTime);
        newFileInfo.setFileName(fileName);
        newFileInfo.setStoredTime(0L);
        internalLazySaveFile(dbFileInfo, newFileInfo);
        getFileInfoMapper().insert(newFileInfo);
        log.info("文件秒传成功，文件存储路径[{}]", dbFileInfo.getFilePath() + File.separator + dbFileInfo.getNewName());
        return newFileInfo;
    }

    /**
     * 当文件读写权限变化时的处理
     */
    protected abstract void internalLazySaveFile(FileInfo dbFileInfo, FileInfo newFileInfo);

    @Transactional(propagation = Propagation.NEVER)
    @Override
    public FileInfo saveFile(UploadFileReq uploadFileReq, long uploadTime, MultipartFile multipartFile) throws IOException {
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
        // 上传文件的存储类型：阿里云OSS
        fileInfo.setStoredType(getStoredType());
        fileInfo.setStoredNode(getStoredNode());
        // 设置文件存储之后的名称：UUID + 后缀名(此操作依赖文件原名称)
        String newName = StoragePathUtils.generateNewFileName(fileInfo.getFileName());
        fileInfo.setNewName(newName);
        fileInfo.setFileSuffix(FilenameUtils.getExtension(fileInfo.getFileName()).toLowerCase());
        // 调用内部保存文件方法
        internalSaveFile(fileInfo, uploadFileReq, uploadTime, multipartFile);
        // 保存文件信息
        getFileInfoMapper().insert(fileInfo);
        return fileInfo;
    }

    /**
     * 存储文件之后,回写以下字段 <br />
     * fileInfo.setFilePath(filePath); <br />
     * fileInfo.setStoredTime(storageEnd - storageStart); <br />
     * fileInfo.setReadUrl(""); <br />
     */
    protected abstract void internalSaveFile(FileInfo fileInfo, UploadFileReq uploadFileReq, long uploadTime, MultipartFile multipartFile) throws IOException;


    @Override
    public FileInfo getFileInfo(String newName) {
        FileInfo fileInfo = getFileInfoMapper().getByNewName(newName, getStoredType());
        return isExists(fileInfo) ? fileInfo : null;
    }

    @Override
    public FileInfo getFileInfo(Long fileId) {
        FileInfo fileInfo = getFileInfoMapper().selectById(fileId);
        return isExists(fileInfo) ? fileInfo : null;
    }

    @Transactional(propagation = Propagation.NEVER)
    @Override
    public void openFileSpeedLimit(FileInfo fileInfo, OutputStream outputStream, long off, long len, long maxSpeed) throws IOException {
        if (fileInfo == null) {
            throw new IllegalArgumentException("文件信息不能为空");
        }
        if (!isExists(fileInfo)) {
            throw new BusinessException("文件不存在", 404);
        }
        internalOpenFileSpeedLimit(fileInfo, outputStream, off, len, maxSpeed);
    }

    /**
     * 限速打开文件
     */
    protected abstract void internalOpenFileSpeedLimit(FileInfo fileInfo, OutputStream outputStream, long off, long len, long maxSpeed) throws IOException;

    @Transactional
    @Override
    public FileInfo deleteFile(Long fileId) {
        FileInfo fileInfo = getFileInfoMapper().selectById(fileId);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }
        getFileInfoMapper().deleteById(fileId);
        FileInfo other = getFileInfoMapper().getFileInfoByDigest(fileInfo.getDigest(), fileInfo.getDigestType(), getStoredType());
        if (other == null && isExists(fileInfo)) {
            internalDeleteFile(fileInfo);
        }
        return fileInfo;
    }

    /**
     * 彻底删除文件
     */
    protected abstract void internalDeleteFile(FileInfo fileInfo);
}
