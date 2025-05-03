package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 收获地址详情请求参数
 */
@Data
public class AddressDetailParam implements Serializable {

    @ApiModelProperty(value="收获地址ID", name="addressId")
    private String addressId;

}
