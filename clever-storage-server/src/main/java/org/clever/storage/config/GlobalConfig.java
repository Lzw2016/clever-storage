package org.clever.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 12:44 <br/>
 */
@Component
@ConfigurationProperties(prefix = "clever.config")
@Data
public class GlobalConfig {

    /**
     * 文件上传到本地硬盘的基础路径
     */
    @NestedConfigurationProperty
    private LocalStorageConfig localStorageConfig;

    /**
     * 阿里云OSS配置
     */
    @NestedConfigurationProperty
    private AliOssConfig aliOssConfig;

    /**
     * Ftp配置
     */
    @NestedConfigurationProperty
    private FtpConfig ftpConfig;

    @Data
    public static class LocalStorageConfig implements Serializable {
        private String diskBasePath;
        /**
         * 文件存储节点, 只支持本机IP
         */
        private String storedNode;
    }

    @Data
    public static class AliOssConfig implements Serializable {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
    }

    @Data
    public static class FtpConfig implements Serializable {
        public String host;
        public String port;
        public Integer username;
        public String password;
    }
}
