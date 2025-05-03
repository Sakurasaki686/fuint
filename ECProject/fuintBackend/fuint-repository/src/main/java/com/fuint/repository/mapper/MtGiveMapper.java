package com.fuint.repository.mapper;

import com.fuint.repository.model.MtGive;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 转赠记录表 Mapper 接口
 *
 *
 *
 */
public interface MtGiveMapper extends BaseMapper<MtGive> {

    List<MtGive> queryForUnique(@Param("userId") Integer userId, @Param("giveUserId") Integer giveUserId, @Param("couponIds") String couponIds, @Param("createTime") Date createTime);

}
