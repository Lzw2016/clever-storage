package org.clever.storage.service;

import org.clever.storage.dto.request.UploadFileReq;
import org.clever.storage.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 上传文件存储接口<br/>
 * <p>
 * 作者：LiZW <br/>
 * 创建时间：2016/11/17 22:10 <br/>
 */
public interface IStorageService {

    /**
     * 根据文件签名保存文件，实现文件秒传<br>
     *
     * @param uploadFileReq 请求上传参数
     * @param uploadTime    文件上传所用时间
     * @param fileName      文件名称
     * @param digest        文件签名
     * @param digestType    签名类型
     * @return 保存成功返回文件信息，失败返回null
     */
    FileInfo lazySaveFile(UploadFileReq uploadFileReq, long uploadTime, String fileName, String digest, Integer digestType);

    /**
     * 保存文件，当文件较大时此方法会占用磁盘IO，因为common-fileupload会将上传文件写入硬盘的临时文件<br>
     * <p>
     * <b>注意：如果上传的文件在服务器端存在(通过文件签名判断)，就不会存储文件只会新增文件引用</b>
     *
     * @param uploadFileReq 请求上传参数
     * @param uploadTime    文件上传所用时间
     * @param multipartFile 上传的文件信息
     * @return 返回存储后的文件信息
     * @throws Exception 保存失败抛出异常
     */
    FileInfo saveFile(UploadFileReq uploadFileReq, long uploadTime, MultipartFile multipartFile) throws Exception;

    /**
     * 判断文件在服务端是否存在<br>
     */
    boolean isExists(FileInfo fileInfo);

    /**
     * 判断文件在服务端是否存在(数据库存在文件信息且文件存在硬盘)<br>
     *
     * @param newName 文件名称
     * @return 不存在返回null，存在返回文件信息
     */
    FileInfo getFileInfo(String newName);

    /**
     * 判断文件在服务端是否存在(数据库存在文件信息且文件存在硬盘)<br>
     *
     * @param fileId 文件ID
     * @return 不存在返回null，存在返回文件信息
     */
    FileInfo getFileInfo(Long fileId);

    /**
     * 打开文件到OutputStream(限制打开文件速度，适用于客户端下载文件) 可以控制打开速度<br>
     * <b>注意：使用此方法会限制打开文件速度(字节/秒)</b>
     *
     * @param fileInfo     文件信息
     * @param outputStream 输出流，用于打开文件
     * @param off          读取起始字节(小于等于0就重头开始读取)
     * @param len          读取长度(小于等于0就读完数据流)
     * @param maxSpeed     最大打开文件速度(字节/秒)(值小于等于0则不限速)
     * @throws IOException 操作失败
     */
    void openFileSpeedLimit(FileInfo fileInfo, OutputStream outputStream, long off, long len, long maxSpeed) throws IOException;

    /**
     * 删除文件<br>
     *
     * @param fileId 文件ID
     */
    FileInfo deleteFile(Long fileId);
}

