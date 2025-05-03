package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 充值请求参数
 */
@Data
public class RechargeParam implements Serializable {

    @ApiModelProperty(value="充值金额", name="rechargeAmount")
    private String rechargeAmount;

    @ApiModelProperty(value="自定义充值金额", name="customAmount")
    private String customAmount;

}
