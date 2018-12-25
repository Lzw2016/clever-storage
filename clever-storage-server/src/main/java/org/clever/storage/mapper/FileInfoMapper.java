package org.clever.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.clever.storage.entity.FileInfo;
import org.springframework.stereotype.Repository;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-25 11:42 <br/>
 */
@Repository
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
}
