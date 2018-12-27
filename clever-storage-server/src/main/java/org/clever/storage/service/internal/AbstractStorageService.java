package org.clever.storage.service.internal;

import org.clever.storage.dto.request.UploadFileReq;
import org.clever.storage.entity.FileInfo;
import org.clever.storage.service.IStorageService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-27 12:02 <br/>
 */
public abstract class AbstractStorageService implements IStorageService {

    @Override
    public FileInfo lazySaveFile(UploadFileReq uploadFileReq, long uploadTime, String fileName, String digest, Integer digestType) {
        return null;
    }

    @Override
    public FileInfo saveFile(UploadFileReq uploadFileReq, long uploadTime, MultipartFile multipartFile) throws Exception {
        return null;
    }

    @Override
    public boolean isExists(FileInfo fileInfo) {
        return false;
    }

    @Override
    public FileInfo getFileInfo(String newName) {
        return null;
    }

    @Override
    public FileInfo getFileInfo(Long fileId) {
        return null;
    }

    @Override
    public void openFileSpeedLimit(FileInfo fileInfo, OutputStream outputStream, long off, long len, long maxSpeed) throws IOException {

    }

    @Override
    public FileInfo deleteFile(Long fileId) {
        return null;
    }
}
