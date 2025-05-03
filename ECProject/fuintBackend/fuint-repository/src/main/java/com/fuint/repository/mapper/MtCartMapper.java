package com.fuint.repository.mapper;

import com.fuint.repository.model.MtCart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车 Mapper 接口
 *
 *
 * 
 */
public interface MtCartMapper extends BaseMapper<MtCart> {

    void deleteCartItem(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId, @Param("skuId") Integer skuId);

    void clearCart(@Param("userId") Integer userId);

    void deleteCartByHangNo(@Param("hangNo") String hangNo);

}
