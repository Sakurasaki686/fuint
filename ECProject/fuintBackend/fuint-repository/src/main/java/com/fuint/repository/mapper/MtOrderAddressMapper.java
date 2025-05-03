package com.fuint.repository.mapper;

import com.fuint.repository.model.MtOrderAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单收货地址记录表 Mapper 接口
 *
 *
 *
 */
public interface MtOrderAddressMapper extends BaseMapper<MtOrderAddress> {

    List<MtOrderAddress> getOrderAddress(@Param("orderId") Integer orderId);

}
