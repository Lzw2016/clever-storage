package org.clever.storage.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.server.service.BaseService;
import org.clever.common.utils.IDCreateUtils;
import org.clever.common.utils.IPAddressUtils;
import org.clever.storage.config.GlobalConfig;
import org.clever.storage.entity.EnumConstant;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.mapper.FileInfoMapper;
import org.clever.storage.service.IStorageService;
import org.clever.storage.utils.FileDigestUtils;
import org.clever.storage.utils.StoragePathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

/**
 * 上传文件存储到当前服务器的Service<br>
 * <p>
 * 作者：LiZW <br/>
 * 创建时间：2016/11/17 22:17 <br/>
 */
@Transactional(readOnly = true)
@Service("LocalStorageService")
@Slf4j
public class LocalStorageService extends BaseService implements IStorageService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    /**
     * 上传文件存储到当前服务器的路径，如：F:\fileStoragePath<br>
     * <p>
     * <b>注意：路径后面没有多余的“\”或者“/”</b>
     */
    private final String diskBasePath;
    /**
     * 文件存储节点, 只支持本机IP
     */
    private final String storedNode;

    public LocalStorageService(GlobalConfig globalConfig) {
        diskBasePath = globalConfig.getLocalStorageConfig().getDiskBasePath();
        if (StringUtils.isBlank(diskBasePath)) {
            throw new IllegalArgumentException("文件上传到本地硬盘的基础路径(diskBasePath)未配置");
        }
        File file = new File(diskBasePath);
        if (file.exists() && file.isFile()) {
            throw new IllegalArgumentException("文件上传到本地硬盘的基础路径(diskBasePath)=[" + diskBasePath + "]不能是文件");
        }
        if (!file.exists() && file.mkdirs()) {
            log.info("[本地服务器]创建文件夹：" + diskBasePath);
        }

        storedNode = globalConfig.getLocalStorageConfig().getStoredNode();
        if ("127.0.0.1".equals(storedNode)) {
            throw new IllegalArgumentException("文件存储节点(storedNode)=[" + storedNode + "]不能是127.0.0.1");
        }
        Set<String> ipAddress = IPAddressUtils.getInet4Address();
        if (!ipAddress.contains(storedNode)) {
            ipAddress.remove("127.0.0.1");
            throw new IllegalArgumentException("文件存储节点(storedNode)=[" + storedNode + "]可选值：" + ipAddress.toString());
        }
    }


//    @Override
//    public FileInfo lazySaveFile(String fileName, String fileDigest, Character digestType) throws Exception {
//        FileInfo dbFileInfo;
//        if (StringUtils.isBlank(fileDigest) || digestType == null) {
//            return null;
//        }
//        // 到数据库查找判断此文件是否已经上传过了 - 此文件是否已经上传过了，不需要重复保存
//        dbFileInfo = fileInfoDao.findFileInfoByDigest(fileDigest, digestType);
//        if (dbFileInfo == null) {
//            logger.debug("[本地服务器]秒传失败，文件没有上传过");
//            return null;
//        }
//        if (StringUtils.isBlank(dbFileInfo.getFilePath()) || StringUtils.isBlank(dbFileInfo.getNewName())) {
//            logger.warn("[本地服务器]秒传失败，数据库里文件(FilePath、NewName)信息为空，文件信息UUID={}", dbFileInfo.getUuid());
//            return null;
//        }
//        String filepath = FILE_STORAGE_PATH + dbFileInfo.getFilePath() + File.separator + dbFileInfo.getNewName();
//        File file = new File(filepath);
//        if (!file.exists() || !file.isFile()) {
//            logger.warn("[本地服务器]秒传失败，上传文件不存在(可能已经被删除)，文件路径[{}]", filepath);
//            return null;
//        }
//        logger.warn("[本地服务器]文件秒传成功，文件存储路径[{}]", filepath);
//        return dbFileInfo;
//    }

    @Transactional
    @Override
    public FileInfo saveFile(long uploadTime, String fileSource, MultipartFile multipartFile) throws Exception {
        // 设置文件签名类型 和 文件签名
        String digest;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            digest = FileDigestUtils.FileDigestByMD5(inputStream);
        }
//        // 通过文件签名检查服务器端是否有相同文件
//        FileInfo lazyFileInfo = this.lazySaveFile(multipartFile.getOriginalFilename(), digest, FileInfo.MD5_DIGEST);
//        if (lazyFileInfo != null) {
//            return lazyFileInfo;
//        }
        // 服务器端不存在相同文件
        FileInfo fileInfo = new FileInfo();
        // TODO 抽离
        fileInfo.setPublicRead(EnumConstant.PublicRead_0);
        fileInfo.setPublicWrite(EnumConstant.PublicWrite_0);
        fileInfo.setFileSource(fileSource);
        fileInfo.setUploadTime(uploadTime);
        // 基本属性
        fileInfo.setFileName(multipartFile.getOriginalFilename());
        fileInfo.setFileSize(multipartFile.getSize());
        fileInfo.setDigest(digest);
        fileInfo.setDigestType(EnumConstant.DigestType_1);
        // 上传文件的存储类型：当前服务器硬盘
        fileInfo.setStoredType(EnumConstant.StoredType_1);
        fileInfo.setStoredNode(storedNode);
        // 设置文件存储之后的名称：UUID + 后缀名(此操作依赖文件原名称)
        String newName = IDCreateUtils.uuid();
        String fileExtension = FilenameUtils.getExtension(fileInfo.getFileName());
        if (StringUtils.isNotBlank(fileExtension)) {
            newName = newName + "." + fileExtension.toLowerCase();
        }
        fileInfo.setNewName(newName);
        // 上传文件存储到当前服务器的路径(相对路径，相对于 FILE_STORAGE_PATH)
        String filePath = StoragePathUtils.generateFilePathByDate("");
        fileInfo.setFilePath(filePath);
        // 计算文件的绝对路径，保存文件
        String absoluteFilePath = diskBasePath + filePath + File.separator + newName;
        File file = new File(absoluteFilePath);
        long storageStart = System.currentTimeMillis();
        // 文件夹不存在，创建文件夹
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (parentFile.mkdirs()) {
                log.info("[本地服务器]创建文件夹：" + parentFile.getPath());
            } else {
                throw new RuntimeException("创建文件夹[" + parentFile.getPath() + "]失败");
            }
        }
        // 如果filePath表示的不是一个路径，文件就会被存到System.getProperty("user.dir")路径下
        multipartFile.transferTo(file);
        long storageEnd = System.currentTimeMillis();
        // 设置存储所用的时间
        fileInfo.setStoredTime(storageEnd - storageStart);
        log.info("[本地服务器]文件存储所用时间:[{}ms]", fileInfo.getStoredTime());
        // 保存文件信息
        fileInfoMapper.insert(fileInfo);
        return fileInfo;
    }

//    @Transactional(readOnly = false)
//    @Override
//    public int deleteFile(Serializable fileInfoUuid, boolean lazy) throws Exception {
//        // 1：成功删除fileInfo和服务器端文件；2：只删除了fileInfo；3：fileInfo不存在
//        FileInfo fileInfo = fileInfoDao.getFileInfoByUuid(fileInfoUuid);
//        if (fileInfo == null) {
//            // FileInfo 不存在或已经被删除
//            return 3;
//        }
//        int count = fileInfoDao.deleteFileInfo(fileInfo.getFilePath(), fileInfo.getNewName());
//        logger.info("[本地服务器]删除文件引用数量：{} 条", count);
//        if (lazy) {
//            // lazy == true:只删除FileInfo
//            return 2;
//        }
//        String fullPath = FILE_STORAGE_PATH + fileInfo.getFilePath();
//        fullPath = FilenameUtils.concat(fullPath, fileInfo.getNewName());
//        File file = new File(fullPath);
//        if (file.exists() && file.isFile()) {
//            if (!file.delete()) {
//                throw new Exception("[本地服务器]文件删除失败：" + fullPath);
//            }
//        } else {
//            throw new Exception("[本地服务器]文件删除失败：" + fullPath);
//        }
//        logger.warn("[本地服务器]删除文件成功，文件路径[{}]", fullPath);
//        return 1;
//    }

//    @Override
//    public FileInfo isExists(Serializable fileInfoUuid) throws Exception {
//        FileInfo fileInfo = fileInfoDao.getFileInfoByUuid(fileInfoUuid);
//        if (fileInfo == null) {
//            return null;
//        }
//        String fullPath = FILE_STORAGE_PATH + fileInfo.getFilePath();
//        fullPath = FilenameUtils.concat(fullPath, fileInfo.getNewName());
//        File file = new File(fullPath);
//        if (file.exists() && file.isFile()) {
//            return fileInfo;
//        }
//        logger.warn("[本地服务器]文件引用[UUID={}]对应的文件不存在", fileInfo.getUuid());
//        return null;
//    }

//    @Override
//    public FileInfo openFile(Serializable fileInfoUuid, OutputStream outputStream) throws Exception {
//        FileInfo fileInfo = fileInfoDao.getFileInfoByUuid(fileInfoUuid);
//        if (fileInfo == null) {
//            return null;
//        }
//        String fullPath = FILE_STORAGE_PATH + fileInfo.getFilePath();
//        fullPath = FilenameUtils.concat(fullPath, fileInfo.getNewName());
//        File file = new File(fullPath);
//        if (file.exists() && file.isFile()) {
//            try (InputStream inputStream = FileUtils.openInputStream(file)) {
//                byte[] data = new byte[256 * 1024];
//                while (inputStream.read(data) > -1) {
//                    outputStream.write(data);
//                }
//                outputStream.flush();
//            }
//            return fileInfo;
//        }
//        logger.warn("[本地服务器]文件引用[UUID={}]对应的文件不存在", fileInfo.getUuid());
//        return null;
//    }

//    @Override
//    public FileInfo openFileSpeedLimit(Serializable fileInfoUuid, OutputStream outputStream, long maxSpeed) throws Exception {
//        if (maxSpeed <= 0) {
//            maxSpeed = Max_Open_Speed;
//        }
//        RateLimiter rateLimiter = RateLimiter.create(maxSpeed);
//        FileInfo fileInfo = fileInfoDao.getFileInfoByUuid(fileInfoUuid);
//        if (fileInfo == null) {
//            return null;
//        }
//        String fullPath = FILE_STORAGE_PATH + fileInfo.getFilePath();
//        fullPath = FilenameUtils.concat(fullPath, fileInfo.getNewName());
//        File file = new File(fullPath);
//        if (file.exists() && file.isFile()) {
//            try (InputStream inputStream = FileUtils.openInputStream(file)) {
//                byte[] data = new byte[32 * 1024];
//                int readByte;
//                double sleepTime;
//                while (true) {
//                    readByte = inputStream.read(data);
//                    if (readByte <= 0) {
//                        break;
//                    }
//                    outputStream.write(data);
//                    sleepTime = rateLimiter.acquire(readByte);
//                    logger.debug("[本地服务器]打开文件UUID:[{}], 读取字节数:[{}], 休眠时间:[{}]秒", fileInfo.getUuid(), readByte, sleepTime);
//                }
//                outputStream.flush();
//            }
//            return fileInfo;
//        }
//        logger.warn("[本地服务器]文件引用[UUID={}]对应的文件不存在", fileInfo.getUuid());
//        return null;
//    }
}
