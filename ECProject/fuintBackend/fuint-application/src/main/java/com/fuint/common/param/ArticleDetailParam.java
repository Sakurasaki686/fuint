package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 文章详情请求参数
 */
@Data
public class ArticleDetailParam implements Serializable {

    @ApiModelProperty(value="文章ID", name="articleId")
    private String articleId;

}
