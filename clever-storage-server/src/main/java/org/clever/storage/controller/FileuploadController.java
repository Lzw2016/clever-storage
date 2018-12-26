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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * 通过文件签名实现文件秒传，只能一次上传一个文件<br/>
     * 文件秒传：实际上是通过文件签名在以前上传的文件中找出一样的文件，并未真正的上传文件<br/>
     * <b>注意：文件秒传取决于此文件已经上传过了而且使用的文件签名类型相同</b>
     */
    @ApiOperation("文件秒传")
    @PostMapping("/lazy_upload")
    public FileInfo uploadLazy(@RequestBody @Validated FileUploadLazyReq fileUploadLazyReq) {
        return manageStorageService.uploadLazy(fileUploadLazyReq);
    }

    @ApiOperation("根据文件UUID，下载文件")
    @GetMapping("/download/{newName}")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable String newName) {
//        log.info("### ========== {}", newName);
    }
}
