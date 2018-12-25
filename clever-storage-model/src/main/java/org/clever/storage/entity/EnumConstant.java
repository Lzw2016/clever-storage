package org.clever.storage.entity;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-09-17 14:15 <br/>
 */
public class EnumConstant {
    /**
     * 上传文件的存储类型 1：当前服务器硬盘
     */
    public static final Integer StoredType_1 = 1;

    /**
     * 上传文件的存储类型 2：ftp服务器
     */
    public static final Integer StoredType_2 = 2;

    /**
     * 上传文件的存储类型 3：阿里云OSS
     */
    public static final Integer StoredType_3 = 3;

    /**
     * 文件签名算法类型（1：md5）
     */
    public static final Integer DigestType_1 = 1;

    /**
     * 文件签名算法类型（2：sha-1；）
     */
    public static final Integer DigestType_2 = 2;

    /**
     * 是否公开可以访问(0不是)
     */
    public static final Integer PublicRead_0 = 0;

    /**
     * 是否公开可以访问(1是)
     */
    public static final Integer PublicRead_1 = 1;

    /**
     * 是否公开可以修改(0不是)
     */
    public static final Integer PublicWrite_0 = 0;

    /**
     * 是否公开可以修改(1是)
     */
    public static final Integer PublicWrite_1 = 1;
}
