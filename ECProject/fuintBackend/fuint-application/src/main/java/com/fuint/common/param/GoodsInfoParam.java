package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 商品详情请求参数
 */
@Data
public class GoodsInfoParam implements Serializable {

    @ApiModelProperty(value="商品ID", name="goodsId")
    private String goodsId;

    @ApiModelProperty(value="skuNo", name="skuNo")
    private String skuNo;

}
