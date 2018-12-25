package org.clever.storage.config;

import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.extension.plugins.SqlExplainInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.storage.servlet.SpeedLimitMultipartResolver;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 10:37 <br/>
 */
@SuppressWarnings("UnnecessaryLocalVariable")
@Configuration
@Slf4j
public class BeanConfiguration {

    /**
     * 分页插件
     */
    @Bean
    protected PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
//        paginationInterceptor.setSqlParser()
//        paginationInterceptor.setDialectClazz()
        paginationInterceptor.setOverflow(false);
//        paginationInterceptor.setProperties();
        return paginationInterceptor;
    }

    /**
     * 乐观锁插件<br />
     * 取出记录时，获取当前version <br />
     * 更新时，带上这个version <br />
     * 执行更新时， set version = yourVersion+1 where version = yourVersion <br />
     * 如果version不对，就更新失败 <br />
     */
    @Bean
    protected OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

//    /**
//     * 逻辑删除<br />
//     */
//    @Bean
//    public ISqlInjector sqlInjector() {
//        return new LogicSqlInjector();
//    }

    /**
     * SQL执行效率插件
     */
    @Bean
    @Profile({"dev", "test"})
    protected PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        performanceInterceptor.setFormat(false);
        performanceInterceptor.setWriteInLog(false);
        performanceInterceptor.setMaxTime(1000);
        return performanceInterceptor;
    }

    /**
     * 执行分析插件<br />
     * SQL 执行分析拦截器【 目前只支持 MYSQL-5.6.3 以上版本 】
     * 作用是分析 处理 DELETE UPDATE 语句
     * 防止小白或者恶意 delete update 全表操作！
     */
    @Bean
    @Profile({"dev", "test"})
    protected SqlExplainInterceptor sqlExplainInterceptor() {
        SqlExplainInterceptor sqlExplainInterceptor = new SqlExplainInterceptor();
//        sqlExplainInterceptor.stopProceed
        return sqlExplainInterceptor;
    }

    @Bean("multipartResolver")
    protected CommonsMultipartResolver commonsMultipartResolver(GlobalConfig globalConfig, MultipartProperties multipartProperties) {
        MultipartConfigElement multipartConfigElement = multipartProperties.createMultipartConfig();
        SpeedLimitMultipartResolver speedLimitMultipartResolver = new SpeedLimitMultipartResolver(globalConfig.getSpeedLimit());
        speedLimitMultipartResolver.setDefaultEncoding("UTF-8");
        speedLimitMultipartResolver.setMaxInMemorySize(multipartConfigElement.getFileSizeThreshold());
        speedLimitMultipartResolver.setMaxUploadSize(multipartConfigElement.getMaxRequestSize());
        speedLimitMultipartResolver.setMaxUploadSizePerFile(multipartConfigElement.getMaxFileSize());
        speedLimitMultipartResolver.setPreserveFilename(false);
        speedLimitMultipartResolver.setResolveLazily(true);
        try {
            File file = new File(multipartConfigElement.getLocation());
            if (file.exists() && file.isFile()) {
                throw new IllegalArgumentException("文件上传临时路径=[" + multipartConfigElement.getLocation() + "]不能是文件");
            }
            if (!file.exists() && file.mkdirs()) {
                log.info("[本地服务器]创建文件夹：" + multipartConfigElement.getLocation());
            }
            speedLimitMultipartResolver.setUploadTempDir(new FileSystemResource(multipartConfigElement.getLocation()));
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
        return speedLimitMultipartResolver;
    }
}
