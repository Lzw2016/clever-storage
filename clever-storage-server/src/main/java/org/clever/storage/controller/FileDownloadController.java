package org.clever.storage.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.clever.storage.service.ManageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-26 22:00 <br/>
 */
@Api(description = "文件下载")
@RestController
@RequestMapping("/api")
@Slf4j
public class FileDownloadController {

    @Autowired
    private ManageStorageService manageStorageService;

    @ApiOperation("根据文件newName打开文件")
    @GetMapping("/open_speed_limit/{newName}")
    public void openFileSpeedLimit(HttpServletRequest request, HttpServletResponse response, @PathVariable String newName) {
        manageStorageService.openFile(true, request, response, newName);
    }

    @ApiOperation("根据文件newName，下载文件")
    @GetMapping("/download_speed_limit/{newName}")
    public void downloadSpeedLimit(HttpServletRequest request, HttpServletResponse response, @PathVariable String newName) {
        manageStorageService.download(true, request, response, newName);
    }

    @ApiOperation("根据文件newName，下载文件")
    @GetMapping("/download/{newName}")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable String newName) {
        manageStorageService.download(false, request, response, newName);
    }
}
