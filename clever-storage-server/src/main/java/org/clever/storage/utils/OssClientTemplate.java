//package org.clever.storage.utils;
//
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.model.CannedAccessControlList;
//import com.aliyun.oss.model.CreateBucketRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.clever.storage.config.GlobalConfig;
//
///**
// * 作者： lzw<br/>
// * 创建时间：2018-12-27 22:01 <br/>
// */
//@Slf4j
//public class OssClientTemplate {
//
//    private final OSS ossClient;
//
//    public OssClientTemplate(GlobalConfig.AliOssConfig aliOssConfig) {
//        ossClient = new OSSClientBuilder().build(aliOssConfig.getEndpoint(), aliOssConfig.getAccessKeyId(), aliOssConfig.getAccessKeySecret());
//        if (!ossClient.doesBucketExist(aliOssConfig.getBucketName())) {
//            log.info("创建存储空间 {}", aliOssConfig.getBucketName());
//            CreateBucketRequest createBucketRequest = new CreateBucketRequest(aliOssConfig.getBucketName());
//            createBucketRequest.setCannedACL(CannedAccessControlList.Private);
//            // createBucketRequest.setProgressListener();
//            ossClient.createBucket(createBucketRequest);
//        }
//    }
//
//
//}
