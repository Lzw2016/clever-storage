package org.clever.storage.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 上传文件信息表(FileInfo)实体类
 *
 * @author lizw
 * @since 2018-12-25 11:24:35
 */
@Data
public class FileInfo implements Serializable {
    private static final long serialVersionUID = -87670006797109613L;
    /**
     * 编号
     */
    private Long id;

    /**
     * 上传文件的存储类型（1：当前服务器硬盘；2：ftp服务器；3：；阿里云OSS）
     */
    private Integer storedType;

    /**
     * 文件存储节点
     */
    private String storedNode;

    /**
     * 上传文件存放路径
     */
    private String filePath;

    /**
     * 文件签名，用于判断是否是同一文件
     */
    private String digest;

    /**
     * 文件签名算法类型（1：md5；2：sha-1；）
     */
    private Integer digestType;

    /**
     * 文件大小，单位：byte(1kb = 1024byte)
     */
    private Long fileSize;

    /**
     * 文件原名称，用户上传时的名称
     */
    private String fileName;

    /**
     * 文件当前名称（uuid + 后缀名）
     */
    private String newName;

    /**
     * 文件后缀名
     */
    private String fileSuffix;

    /**
     * 是否公开可以访问(0不是，1是)
     */
    private Integer publicRead;

    /**
     * 是否公开可以修改(0不是，1是)
     */
    private Integer publicWrite;

    /**
     * 访问url
     */
    private String readUrl;

    /**
     * 文件上传所用时间（毫秒）
     */
    private Long uploadTime;

    /**
     * 文件存储用时，单位：毫秒（此时间只包含服务器端存储文件所用的时间，不包括文件上传所用的时间）
     */
    private Long storedTime;

    /**
     * 文件来源（可以是系统模块名）
     */
    private String fileSource;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 更新时间
     */
    private Date updateAt;
}