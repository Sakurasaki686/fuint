package com.fuint.repository.mapper;

import com.fuint.repository.model.MtAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员地址 Mapper 接口
 *
 * 
 * 
 */
public interface MtAddressMapper extends BaseMapper<MtAddress> {
    int setDefault(@Param("userId") Integer userId, @Param("addressId") Integer addressId);
}
