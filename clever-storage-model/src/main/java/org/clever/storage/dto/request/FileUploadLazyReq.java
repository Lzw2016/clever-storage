package org.clever.storage.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.clever.common.validation.ValidIntegerStatus;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-12-26 16:18 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FileUploadLazyReq extends BaseRequest {

    @ApiModelProperty("是否公开可以访问(0不是，1是)")
    @ValidIntegerStatus({0, 1})
    private Integer publicRead;

    @ApiModelProperty("是否公开可以修改(0不是，1是)")
    @ValidIntegerStatus({0, 1})
    private Integer publicWrite;

    @ApiModelProperty("文件来源")
    @NotBlank
    @Length(max = 32)
    private String fileSource;

    @ApiModelProperty("上传文件原名称")
    @NotBlank(message = "上传文件原名称不能为空")
    private String fileName;

    @ApiModelProperty("上传文件的文件签名")
    @NotBlank(message = "上传文件的文件签名不能为空")
    private String fileDigest;

    @ApiModelProperty("文件签名类型，目前只支持MD5和SHA1两种（1：MD5；2：SHA-1；）")
    @ValidIntegerStatus(value = {1, 2}, message = "文件签名类型只能是：1(MD5)、2(SHA-1)")
    private Integer digestType;
}
