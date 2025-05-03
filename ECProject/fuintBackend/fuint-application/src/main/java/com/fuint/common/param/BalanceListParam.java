package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 余额明细列表请求参数
 */
@Data
public class BalanceListParam extends PageParam implements Serializable {

    @ApiModelProperty(value="会员ID", name="userId")
    private String userId;

}
