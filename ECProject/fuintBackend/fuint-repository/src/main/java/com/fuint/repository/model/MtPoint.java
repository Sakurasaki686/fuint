package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员积分记录表
 *
 * 
 *
 */
@Getter
@Setter
@TableName("mt_point")
@ApiModel(value = "MtPoint对象", description = "会员积分记录表")
public class MtPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("所属店铺ID")
    private Integer storeId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("订单号")
    private String orderSn;

    @ApiModelProperty("积分变化数量")
    private Integer amount;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("备注说明")
    private String description;

    @ApiModelProperty("最后操作人")
    private String operator;

    @ApiModelProperty("状态，A正常；D作废")
    private String status;

}
