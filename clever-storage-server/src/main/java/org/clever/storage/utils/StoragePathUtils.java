package org.clever.storage.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.utils.DateTimeUtils;
import org.clever.common.utils.IDCreateUtils;

import java.io.File;

/**
 * 作者：LiZW <br/>
 * 创建时间：2016/11/17 22:29 <br/>
 */
public class StoragePathUtils {

    /**
     * 根据当前时间生成文件存储路径，生成策略：basePath/yyyy/yyyy-MM/yyyy-MM-dd <br>
     *
     * @param basePath 存储文件基路径
     * @return 返回存储文件完整路径，类似：basePath + /yyyy/yyyy-MM/yyyy-MM-dd
     */
    public static String generateFilePathByDate(String basePath) {
        long dateTime = System.currentTimeMillis();
        String dateStr = DateTimeUtils.getDate(dateTime, "yyyy-MM-dd");
        String[] array = dateStr.split("-");
        return basePath +
                File.separator +
                array[0] +
                File.separator +
                array[0] + "-" + array[1] +
                File.separator +
                dateStr;
    }

    /**
     * 生成新文件名
     *
     * @param fileName 上传文件名
     */
    public static String generateNewFileName(String fileName) {
        String newName = IDCreateUtils.uuid();
        String fileExtension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isNotBlank(fileExtension)) {
            newName = newName + "." + fileExtension.toLowerCase();
        }
        return newName;
    }
}


