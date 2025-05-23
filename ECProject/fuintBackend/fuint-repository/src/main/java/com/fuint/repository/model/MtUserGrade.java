package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员等级表
 *
 * 
 *
 */
@Getter
@Setter
@TableName("mt_user_grade")
@ApiModel(value = "MtUserGrade对象", description = "")
public class MtUserGrade implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("等级")
    private Integer grade;

    @ApiModelProperty("等级名称")
    private String name;

    @ApiModelProperty("升级会员等级条件描述")
    private String catchCondition;

    @ApiModelProperty("升级会员等级条件，init:默认获取;pay:付费升级；frequency:消费次数；amount:累积消费金额升级")
    private String catchType;

    @ApiModelProperty("达到升级条件的值")
    private BigDecimal catchValue;

    @ApiModelProperty("会员权益描述")
    private String userPrivilege;

    @ApiModelProperty("有效期")
    private Integer validDay;

    @ApiModelProperty("享受折扣")
    private Float discount;

    @ApiModelProperty("积分加速")
    private Float speedPoint;

    @ApiModelProperty("状态")
    private String status;

}
