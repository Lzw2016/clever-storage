package org.clever.storage.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.clever.common.exception.BusinessException;
import org.clever.common.utils.validator.BaseValidatorUtils;
import org.clever.common.utils.validator.ValidatorFactoryUtils;
import org.clever.storage.dto.request.UploadFileReq;
import org.clever.storage.dto.response.UploadFilesRes;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.utils.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 作者：LiZW <br/>
 * 创建时间：2017/1/17 17:01 <br/>
 */
@Transactional(readOnly = true)
@Service
@Slf4j
public class ManageStorageService {

    @Autowired
    private IStorageService storageService;

    /**
     * 解析请求 request 得到 UploadFileReq
     */
    private UploadFileReq parseUploadFileReq(HttpServletRequest request) {
        // 解析请求参数
        UploadFileReq uploadFileReq = new UploadFileReq();
        uploadFileReq.setFileSource(request.getParameter("fileSource"));
        // 是否公开可以修改(0不是，1是)
        uploadFileReq.setPublicWrite(NumberUtils.toInt(request.getParameter("publicWrite"), 0));
        // 是否公开可以访问(0不是，1是)
        uploadFileReq.setPublicRead(NumberUtils.toInt(request.getParameter("publicRead"), 1));
        // 校验请求数据
        BaseValidatorUtils.validateThrowException(ValidatorFactoryUtils.getHibernateValidator(), uploadFileReq);
        return uploadFileReq;
    }

    /**
     * 上传文件通用方法
     *
     * @return 失败返回null, 务必调用 message.setResult(fileInfoList)
     */
    @Transactional
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

//    /**
//     * 通过文件签名实现文件秒传，只能一次上传一个文件<br/>
//     *
//     * @return 失败返回null, 务必调用 message.setResult(fileInfo)
//     */
//    public static FileInfo uploadLazy(IStorageService storageService, HttpServletRequest request, HttpServletResponse response, FileUploadLazyVo fileUploadLazyVo, AjaxMessage message) {
//        // 验证是否是Hex编码
//        String fileDigest = fileUploadLazyVo.getFileDigest().toLowerCase();
//        if (!EncodeDecodeUtils.isHexcode(fileDigest)) {
//            message.setSuccess(false);
//            message.setFailMessage("文件签名必须使用Hex编码");
//            return null;
//        }
//        Character digestType;
//        // 验证文件签名长度
//        if (FileInfo.MD5_DIGEST.toString().equals(fileUploadLazyVo.getDigestType())) {
//            digestType = FileInfo.MD5_DIGEST;
//            // 验证长度MD5 长度为：32
//            if (fileDigest.length() != 32) {
//                message.setSuccess(false);
//                message.setFailMessage("文件签名长度不正确(MD5签名长度32，SHA1签名长度40)");
//                return null;
//            }
//        } else if (FileInfo.SHA1_DIGEST.toString().equals(fileUploadLazyVo.getDigestType())) {
//            digestType = FileInfo.SHA1_DIGEST;
//            // 验证长度SHA1 长度为：40
//            if (fileDigest.length() != 40) {
//                message.setSuccess(false);
//                message.setFailMessage("文件签名长度不正确(MD5签名长度32，SHA1签名长度40)");
//                return null;
//            }
//        } else {
//            message.setSuccess(false);
//            message.setFailMessage("不支持的文件签名：" + fileUploadLazyVo.getDigestType());
//            return null;
//        }
//        // 验证通过
//        FileInfo fileInfo = null;
//        try {
//            fileInfo = storageService.lazySaveFile(fileUploadLazyVo.getFileName(), fileUploadLazyVo.getFileDigest(), digestType);
//        } catch (Throwable e) {
//            message.setSuccess(false);
//            message.setFailMessage("上传文件失败，系统异常");
//            message.setException(e);
//        }
//        if (fileInfo == null) {
//            message.setSuccess(false);
//            message.setFailMessage("文件秒传失败，该文件从未上传过");
//            return null;
//        }
//        return fileInfo;
//    }
}
