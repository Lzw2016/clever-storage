package org.clever.storage.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.clever.storage.dto.request.FileUploadLazyReq;
import org.clever.storage.dto.response.UploadFilesRes;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.service.ManageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-25 10:15 <br/>
 */
@Api(description = "文件上传")
@RestController
@RequestMapping("/api")
@Slf4j
public class FileuploadController {

    @Autowired
    private ManageStorageService manageStorageService;

    @ApiOperation("文件上传，支持多文件上传")
    @PostMapping("/upload")
    public UploadFilesRes upload(HttpServletRequest request) {
        return manageStorageService.upload(request);
    }

    @ApiOperation("文件秒传")
    @PostMapping("/lazy_upload")
    public FileInfo uploadLazy(@RequestBody @Validated FileUploadLazyReq fileUploadLazyReq) {
        return manageStorageService.uploadLazy(fileUploadLazyReq);
    }
}
