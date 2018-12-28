package org.clever.storage.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.PatternConstant;
import org.clever.common.model.request.BaseRequest;
import org.clever.common.validation.ValidIntegerStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-25 19:50 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadFileReq extends BaseRequest {

    @ApiModelProperty("是否公开可以访问(0不是，1是)")
    @ValidIntegerStatus({0, 1})
    private Integer publicRead;

    @ApiModelProperty("是否公开可以修改(0不是，1是)")
    @ValidIntegerStatus({0, 1})
    private Integer publicWrite;

    @ApiModelProperty("文件来源")
    @NotBlank
    @Pattern(regexp = PatternConstant.Name_Pattern + "{3,64}")
    private String fileSource;
}
