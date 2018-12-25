package org.clever.storage.servlet;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.ProgressListener;

import java.text.DecimalFormat;

/**
 * 上传文件安读取进度用于限制文件上传最大速度
 * 作者：LiZW <br/>
 * 创建时间：2016/11/18 14:03 <br/>
 */
@SuppressWarnings({"UnstableApiUsage", "WeakerAccess", "unused"})
@Slf4j
public class SpeedLimitProgressListener implements ProgressListener {
    /**
     * 文件上传最大速度限制 (1024 * 1024 * 1 = 1MB)
     */
    private final static long Max_Upload_Speed = 1024 * 1024;

    /**
     * 最后一次读取的字节数
     */
    private long lastBytesRead;

    /**
     * 令牌桶算法 限流
     */
    private final RateLimiter rateLimiter;

    public SpeedLimitProgressListener() {
        lastBytesRead = 0;
        rateLimiter = RateLimiter.create(Max_Upload_Speed);
    }

    /**
     * @param maxUploadSpeed 最大上传速度限制(字节/秒)
     */
    public SpeedLimitProgressListener(double maxUploadSpeed) {
        lastBytesRead = 0;
        rateLimiter = RateLimiter.create(maxUploadSpeed);
    }

    /**
     * 更新上传文件的状态信息(只做限速，上传进度的显示需要前端JS处理显示)
     *
     * @param pBytesRead     已经上传到服务器的字节数
     * @param pContentLength 所有文件的总大小
     * @param pItems         表示第几个文件
     */
    @Override
    public void update(long pBytesRead, long pContentLength, int pItems) {
        if (rateLimiter == null) {
            return;
        }
        Long addReadBytes = pBytesRead - lastBytesRead;
        lastBytesRead = pBytesRead;
        double sleepTime = 0;
        if (addReadBytes > 0) {
            // 限速
            sleepTime = rateLimiter.acquire(addReadBytes.intValue());
        }
        if (log.isDebugEnabled()) {
            double percentage = (pBytesRead * 1.0 / pContentLength);
            DecimalFormat decimalFormat = new DecimalFormat("#.000");
            log.debug("上传进度[{}%({}/{})], 文件:[{}], 读取字节:[{}], 休眠时间:[{}]秒", decimalFormat.format(percentage * 100), pBytesRead, pContentLength, pItems, addReadBytes, sleepTime);
        }
    }
}
