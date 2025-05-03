package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 删除购物车请求参数
 */
@Data
public class CartClearParam extends PageParam implements Serializable {

    @ApiModelProperty(value="购物车ID", name="cartId")
    private List<String> cartId;

    @ApiModelProperty(value="挂单编码", name="hangNo")
    private String hangNo;

    @ApiModelProperty(value="下单会员ID", name="userId")
    private Integer userId;

}
