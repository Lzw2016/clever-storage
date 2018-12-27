package org.clever.storage.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.service.ManageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-27 11:55 <br/>
 */
@Api(description = "文件信息")
@RestController
@RequestMapping("/api")
@Slf4j
public class FileInfoController {

    @Autowired
    private ManageStorageService manageStorageService;

    @ApiOperation("获取文件信息")
    @GetMapping("/file_info/{fileId}")
    public FileInfo getFileInfo(@PathVariable Long fileId) {
        return manageStorageService.getFileInfo(fileId);
    }
}
