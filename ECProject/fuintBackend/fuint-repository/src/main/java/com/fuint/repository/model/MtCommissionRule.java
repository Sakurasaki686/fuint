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
 * 分佣提成规则表
 *
 *
 *
 */
@Getter
@Setter
@TableName("mt_commission_rule")
@ApiModel(value = "MtCommissionRule对象", description = "分佣提成规则表")
public class MtCommissionRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("规则名称")
    private String name;

    @ApiModelProperty("方案类型,goods:商品销售；coupon：卡券销售；recharge：会员充值")
    private String type;

    @ApiModelProperty("分佣对象,member:会员分销；staff：员工提成")
    private String target;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("店铺ID")
    private Integer storeId;

    @ApiModelProperty("适用店铺ID,逗号隔开")
    private String storeIds;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("备注")
    private String description;

    @ApiModelProperty("最后操作人")
    private String operator;

    @ApiModelProperty("状态")
    private String status;

}
