package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员分组
 *
 * 
 * 
 */
@Getter
@Setter
@TableName("mt_user_group")
@ApiModel(value = "MtUserGroup对象", description = "会员分组")
public class MtUserGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("分组ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("分组名称")
    private String name;

    @ApiModelProperty("所属商户ID")
    private Integer merchantId;

    @ApiModelProperty("默认店铺")
    private Integer storeId;

    @ApiModelProperty("父ID")
    private Integer parentId;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("状态，A：激活；N：禁用；D：删除")
    private String status;

    @ApiModelProperty("备注信息")
    private String description;

    @ApiModelProperty("最后操作人")
    private String operator;
}
