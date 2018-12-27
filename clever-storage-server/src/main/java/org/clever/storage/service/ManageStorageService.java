package org.clever.storage.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.clever.common.exception.BusinessException;
import org.clever.common.utils.codec.EncodeDecodeUtils;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.common.utils.validator.BaseValidatorUtils;
import org.clever.common.utils.validator.ValidatorFactoryUtils;
import org.clever.storage.config.GlobalConfig;
import org.clever.storage.dto.request.FileUploadLazyReq;
import org.clever.storage.dto.request.UploadFileReq;
import org.clever.storage.dto.response.UploadFilesRes;
import org.clever.storage.entity.EnumConstant;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.utils.ContentTypeUtils;
import org.clever.storage.utils.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 作者：LiZW <br/>
 * 创建时间：2017/1/17 17:01 <br/>
 */
@Component
@Slf4j
public class ManageStorageService {

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    @Qualifier("OssStorageService")
    private IStorageService storageService;

    /**
     * 解析请求 request 得到 UploadFileReq
     */
    private UploadFileReq parseUploadFileReq(HttpServletRequest request) {
        // 解析请求参数
        UploadFileReq uploadFileReq = new UploadFileReq();
        uploadFileReq.setFileSource(request.getParameter("fileSource"));
        // 是否公开可以修改(0不是，1是)
        uploadFileReq.setPublicWrite(NumberUtils.toInt(request.getParameter("publicWrite"), EnumConstant.PublicWrite_0));
        // 是否公开可以访问(0不是，1是)
        uploadFileReq.setPublicRead(NumberUtils.toInt(request.getParameter("publicRead"), EnumConstant.PublicWrite_1));
        // 目前只支持 (公开可读-私有写) 和 (私有读-私有写) 两种模式
        if (!Objects.equals(EnumConstant.PublicWrite_0, uploadFileReq.getPublicWrite())) {
            throw new BusinessException("只支持私有写(publicWrite=0)");
        }
        // 校验请求数据
        BaseValidatorUtils.validateThrowException(ValidatorFactoryUtils.getHibernateValidator(), uploadFileReq);
        return uploadFileReq;
    }

    public UploadFilesRes upload(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            throw new BusinessException("当前请求并非上传文件的请求");
        }
        // 保存上传文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        final long uploadStart = System.currentTimeMillis();
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        final long uploadEnd = System.currentTimeMillis();
        // 计算上传时间
        int fileCount = 0;
        for (String fileName : fileMap.keySet()) {
            MultipartFile mFile = fileMap.get(fileName);
            if (mFile.isEmpty()) {
                continue;
            }
            fileCount++;
        }
        if (fileCount <= 0) {
            throw new BusinessException("上传文件不能为空");
        }
        final long uploadTimeSum = uploadEnd - uploadStart;
        final long uploadTimeAvg = uploadTimeSum / fileCount;
        log.info("总共上传文件数量{}个,总共上传时间{}ms. 平均每个文件上传时间{}ms", fileCount, uploadTimeSum, uploadTimeAvg);
        // 解析请求参数
        UploadFileReq uploadFileReq = parseUploadFileReq(request);
        // 调用文件上传服务
        UploadFilesRes uploadFilesRes = new UploadFilesRes();
        for (String fileName : fileMap.keySet()) {
            MultipartFile mFile = fileMap.get(fileName);
            if (mFile.isEmpty()) {
                continue;
            }
            long uploadTime = uploadTimeAvg;
            if (mFile.getSize() > 0) {
                uploadTime = mFile.getSize() * uploadTimeSum / multipartRequest.getContentLengthLong();
            }
            try {
                FileInfo fileInfo = storageService.saveFile(uploadFileReq, uploadTime, mFile);
                uploadFilesRes.getSuccessList().add(fileInfo);
            } catch (Throwable e) {
                log.error("文件上传失败", e);
                uploadFilesRes.getFailList().add(FileUploadUtils.fillFileInfo(mFile));
            }
        }
        return uploadFilesRes;
    }

    public FileInfo uploadLazy(FileUploadLazyReq fileUploadLazyReq) {
        // 验证是否是Hex编码
        String fileDigest = fileUploadLazyReq.getFileDigest().toLowerCase();
        if (!EncodeDecodeUtils.isHexCode(fileDigest)) {
            throw new BusinessException("文件签名必须使用Hex编码");
        }
        // 验证文件签名长度
        if (Objects.equals(EnumConstant.DigestType_1, fileUploadLazyReq.getDigestType())) {
            // 验证长度MD5 长度为：32
            if (fileDigest.length() != 32) {
                throw new BusinessException("文件签名长度不正确(MD5签名长度32，SHA1签名长度40)");
            }
        } else if (Objects.equals(EnumConstant.DigestType_2, fileUploadLazyReq.getDigestType())) {
            // 验证长度SHA1 长度为：40
            if (fileDigest.length() != 40) {
                throw new BusinessException("文件签名长度不正确(MD5签名长度32，SHA1签名长度40)");
            }
        } else {
            throw new BusinessException("不支持的文件签名类型");
        }
        UploadFileReq uploadFileReq = BeanMapper.mapper(fileUploadLazyReq, UploadFileReq.class);
        // 目前只支持 (公开可读-私有写) 和 (私有读-私有写) 两种模式
        if (!Objects.equals(EnumConstant.PublicWrite_0, uploadFileReq.getPublicWrite())) {
            throw new BusinessException("只支持私有写(publicWrite=0)");
        }
        // 验证通过
        FileInfo fileInfo = storageService.lazySaveFile(
                uploadFileReq,
                0L,
                fileUploadLazyReq.getFileName(),
                fileUploadLazyReq.getFileDigest(),
                fileUploadLazyReq.getDigestType()
        );
        if (fileInfo == null) {
            throw new BusinessException("文件秒传失败，该文件从未上传过");
        }
        return fileInfo;
    }

    /**
     * 读取文件(支持断点续传)
     */
    private void readFile(boolean speedLimit, HttpServletRequest request, HttpServletResponse response, String newName, Function<FileInfo, Void> function) {
        FileInfo fileInfo = storageService.getFileInfo(newName);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在", 404);
        }
        function.apply(fileInfo);
        // Range: bytes=0-499 表示第 0-499 字节范围的内容
        // Range: bytes=500-999 表示第 500-999 字节范围的内容
        // Range: bytes=-500 表示最后 500 字节的内容
        // Range: bytes=500- 表示从第 500 字节开始到文件结束部分的内容
        // Range: bytes=0-0,-1 表示第一个和最后一个字节
        // Range: bytes=500-600,601-999 同时指定几个范围
        String range = request.getHeader("Range");
        // Content-Range: bytes 0-499/22400
        // 0－499 是指当前发送的数据的范围，而 22400 则是文件的总大小
        String ifRange = request.getHeader("If-Range");
        // 起始位置
        long off = -1;
        // 读取长度
        long len = -1;
        if (StringUtils.isNotBlank(range) && (StringUtils.isBlank(ifRange) || Objects.equals(ifRange, fileInfo.getDigest()))) {
            range = StringUtils.trim(range);
            range = range.replaceAll("bytes=", "");
            String[] tmp = range.split("-");
            off = NumberUtils.toLong(tmp[0], -1);
            if (tmp.length >= 2) {
                len = NumberUtils.toLong(tmp[1], -1);
                if (len >= off) {
                    len = len - off + 1;
                }
            }
            if (len <= 0) {
                len = fileInfo.getFileSize() - off + 1;
            }
        }
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Last-Modified", String.valueOf(fileInfo.getUpdateAt() == null ? fileInfo.getCreateAt() : fileInfo.getUpdateAt()));
        response.setHeader("Etag", fileInfo.getDigest());
        if (off > 0 || len > 0) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", String.format("bytes %s-%s/%s", off, len + off - 1, fileInfo.getFileSize()));
            response.setHeader("Content-Length", String.valueOf(len));
        } else {
            response.setHeader("Content-Range", String.format("%s-%s/%s", 0, fileInfo.getFileSize(), fileInfo.getFileSize()));
            response.setHeader("Content-Length", fileInfo.getFileSize().toString());
        }
        try {
            OutputStream outputStream = response.getOutputStream();
            if (speedLimit) {
                storageService.openFileSpeedLimit(fileInfo, outputStream, off, len, globalConfig.getDownloadSpeedLimit());
            } else {
                storageService.openFileSpeedLimit(fileInfo, outputStream, off, len, -1);
            }
            outputStream.flush();
            log.info("文件下载成功, 文件NewName={}", fileInfo.getNewName());
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    public void openFile(boolean speedLimit, HttpServletRequest request, HttpServletResponse response, String newName) {
        readFile(speedLimit, request, response, newName, fileInfo -> {
            ContentTypeUtils.setContentTypeNoCharset(response, ContentTypeUtils.getContentType(FilenameUtils.getExtension(fileInfo.getFileName())));
            return null;
        });
    }

    public void download(boolean speedLimit, HttpServletRequest request, HttpServletResponse response, String newName) {
        readFile(speedLimit, request, response, newName, fileInfo -> {
            // 文件存在，下载文件
            String fileName = EncodeDecodeUtils.browserDownloadFileName(request.getHeader("User-Agent"), fileInfo.getFileName());
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            ContentTypeUtils.setContentTypeNoCharset(response, "application/octet-stream");
            return null;
        });
    }

    public FileInfo getFileInfo(Long fileId) {
        return storageService.getFileInfo(fileId);
    }

    public FileInfo deleteFile(Long fileId) {
        return storageService.deleteFile(fileId);
    }
}
