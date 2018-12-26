package org.clever.storage.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.response.BaseResponse;
import org.clever.storage.entity.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-25 12:42 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadFilesRes extends BaseResponse {

    @ApiModelProperty("是否成功")
    private Boolean success = false;

    @ApiModelProperty("成功列表")
    private List<FileInfo> successList = new ArrayList<>();

    @ApiModelProperty("上传失败列表")
    private List<FileInfo> failList = new ArrayList<>();

    public Boolean getSuccess() {
        if (failList == null) {
            success = true;
        } else {
            success = failList.size() <= 0;
        }
        return success;
    }
}
