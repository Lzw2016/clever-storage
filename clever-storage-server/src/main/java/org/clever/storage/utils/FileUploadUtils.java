package org.clever.storage.utils;

import org.clever.storage.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-25 20:38 <br/>
 */
public class FileUploadUtils {

    public static FileInfo fillFileInfo(FileInfo fileInfo, MultipartFile multipartFile) {
        if (fileInfo == null) {
            fileInfo = new FileInfo();
        }
        fileInfo.setFileName(multipartFile.getOriginalFilename());
        fileInfo.setFileSize(multipartFile.getSize());
        return fileInfo;
    }

    public static FileInfo fillFileInfo(MultipartFile multipartFile) {
        return fillFileInfo(null, multipartFile);
    }
}
