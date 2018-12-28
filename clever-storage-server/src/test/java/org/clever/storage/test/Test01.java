package org.clever.storage.test;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.model.*;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-23 14:24 <br/>
 */
@Slf4j
public class Test01 {

    private final static String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private final static String accessKeyId = "LTAIzet6zCcLaw1R";
    private final static String accessKeySecret = "p35d2Rm3vWTus9F3nMoQIry83mUkOf";
    private final static String bucketName = "clever-dev-bucket";
    // private final static String fileKey = IDCreateUtils.uuidNotSplit() + ".txt";
    private final static String fileKey = "目录/不可读.txt";

    // 综合示例
    @Test
    public void t01() throws IOException {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                log.info("创建存储空间 {}", bucketName);
                // ossClient.createBucket(bucketName);
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
                ossClient.createBucket(createBucketRequest);
            }

            log.info("存储空间列表--------------------------------------------------");
            ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
            listBucketsRequest.setMaxKeys(500);
            for (Bucket bucket : ossClient.listBuckets()) {
                log.info("存储空间 -> {}", bucket.getName());
            }

            log.info("上传文件");
            PutObjectResult putObjectResult = ossClient.putObject(new PutObjectRequest(bucketName, fileKey, createSampleFile()));
            log.info("[阿里云OSS]上传文件成功 ETag={}", putObjectResult.getETag());

            boolean exists = ossClient.doesObjectExist(bucketName, fileKey);
            log.info("文件是否存在，存储空间=[{}]，是否存在=[{}]", bucketName, exists);

            ossClient.setObjectAcl(bucketName, fileKey, CannedAccessControlList.PublicRead);
            ossClient.setObjectAcl(bucketName, fileKey, CannedAccessControlList.Default);

            ObjectAcl objectAcl = ossClient.getObjectAcl(bucketName, fileKey);
            log.info("存储空间=[{}]，文件名=[{}]，ACL=[{}]", bucketName, fileKey, objectAcl.getPermission().toString());

            log.info("下载文件");
            OSSObject object = ossClient.getObject(bucketName, fileKey);
            log.info("文件属性 Content-Type: {}", object.getObjectMetadata().getContentType());
            displayTextInputStream(object.getObjectContent());

            log.info("文件列表---------------------------------------------------------");
            // ObjectListing objectListing = ossClient.listObjects(bucketName, "My");
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(bucketName);
            listObjectsRequest.setDelimiter("/");
            // listObjectsRequest.setPrefix("");
//            listObjectsRequest.setPrefix("目录/");
            listObjectsRequest.setMaxKeys(1000);
            ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                log.info("# - {} ({})", objectSummary.getKey(), objectSummary.getSize());
            }

//            // 创建文件夹
//            final String keySuffixWithSlash = "MyObjectKey/";
//            ossClient.putObject(bucketName, keySuffixWithSlash, new ByteArrayInputStream(new byte[0]));


            // log.info("删除文件");
            // ossClient.deleteObject(bucketName, fileKey);
        } catch (OSSException oe) {
            // 捕获一个OSSException，这意味着您的请求已发送到OSS，但由于某种原因以错误响应被拒绝。
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            // 捕获了ClientException，这意味着客户机在尝试与OSS通信时遇到了严重的内部问题，例如无法访问网络。
            System.out.println("Error Message: " + ce.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    private static File createSampleFile() throws IOException {
        File file = File.createTempFile("oss-java-sdk-", ".txt");
        file.deleteOnExit();
        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("0123456789011234567890\n");
        writer.close();
        return file;
    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        log.info("----------------------------------------------------------------------------------------》");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            log.info(line);
        }
        reader.close();
        log.info("《----------------------------------------------------------------------------------------");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void t02() {
        RateLimiter rateLimiter = RateLimiter.create(1024 * 8);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("上传文件");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileKey, new File("D:\\ServiceSoftware\\Tomcat\\apache-tomcat-8.5.30-windows-x64.zip"));
        putObjectRequest.setProgressListener(progressEvent -> {
            if (ProgressEventType.TRANSFER_STARTED_EVENT == progressEvent.getEventType()) {
                log.info("上传开始");
            } else if (ProgressEventType.TRANSFER_COMPLETED_EVENT == progressEvent.getEventType()) {
                log.info("上传完成");
            } else if (ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT == progressEvent.getEventType()) {
                double sleepTime = rateLimiter.acquire((int) progressEvent.getBytes());
                log.info("##### progressEvent ====== [{}]  休眠时间:[{}]秒", progressEvent.getBytes(), sleepTime);
            }
        });
        PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
        log.info("[阿里云OSS]上传文件成功 ETag={}", putObjectResult.getETag());
        ossClient.shutdown();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void t03() throws IOException {
        RateLimiter rateLimiter = RateLimiter.create(1024 * 1024);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        log.info("下载文件");
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, "test/2018/2018-12/2018-12-28/a9690d2a-c0a7-470e-be3c-e0cdeb8986c6.png");

        OSSObject ossObject = ossClient.getObject(getObjectRequest);
        log.info("文件属性 Content-Type: {}", ossObject.getObjectMetadata().getContentType());
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\lzw\\Desktop\\xmind\\1d.png"));
        try (InputStream inputStream = ossObject.getObjectContent()) {
            byte[] data = new byte[8 * 1024];
            int readByte;
            double sleepTime = 0;
            while (true) {
                readByte = inputStream.read(data);
                if (readByte < 0) {
                    break;
                }
                outputStream.write(data, 0, readByte);
                if (rateLimiter != null) {
                    sleepTime = rateLimiter.acquire(readByte);
                }
                log.info("[阿里云OSS]打开文件 读取字节数:[{}], 休眠时间:[{}]秒", readByte, sleepTime);
            }
            outputStream.flush();
        }


        outputStream.close();
        ossClient.shutdown();
    }
}

