package org.clever.storage.controller;

import org.clever.storage.dto.response.UploadFilesRes;
import org.clever.storage.service.IStorageService;
import org.clever.storage.utils.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-25 10:15 <br/>
 */
@RestController
@RequestMapping("/api")
public class FileuploadController {

    @Autowired
    private IStorageService storageService;

    /**
     * 文件上传存储到当前服务器磁盘示例，支持多文件上传<br>
     */
    @PostMapping("/upload")
    public UploadFilesRes upload(HttpServletRequest request, HttpServletResponse response) {
        return FileUploadUtils.upload("clever-storage", storageService, request, response);
    }
}
