package org.clever.storage.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.service.ManageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-27 14:48 <br/>
 */
@Api(description = "文件删除")
@RestController
@RequestMapping("/api")
@Slf4j
public class FileDeleteController {

    @Autowired
    private ManageStorageService manageStorageService;

    @ApiOperation("删除文件")
    @DeleteMapping("/delete_file/{fileId}")
    public FileInfo deleteFile(@PathVariable Long fileId) {
        return manageStorageService.deleteFile(fileId);
    }
}
