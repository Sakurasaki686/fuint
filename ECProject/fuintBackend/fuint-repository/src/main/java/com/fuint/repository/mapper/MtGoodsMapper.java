package com.fuint.repository.mapper;

import com.fuint.repository.bean.GoodsBean;
import com.fuint.repository.bean.GoodsTopBean;
import com.fuint.repository.model.MtGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 *  商品 Mapper 接口
 *
 *
 *
 */
public interface MtGoodsMapper extends BaseMapper<MtGoods> {

    List<MtGoods> getStoreGoodsList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("cateId") Integer cateId);

    List<MtGoods> searchStoreGoodsList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("keyword") String keyword);

    MtGoods getByGoodsNo(@Param("merchantId") Integer merchantId, @Param("goodsNo") String goodsNo);

    Boolean updateInitSale( @Param("goodsId") Integer goodsId);

    List<GoodsBean> selectGoodsList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("cateId") Integer cateId, @Param("keyword") String keyword);

    List<GoodsTopBean> getGoodsSaleTopList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<MtGoods> searchForRecommendation(@Param("keyword") String keyword,
                                          @Param("limit") int limit,
                                          @Param("storeId") Integer storeId,
                                          @Param("merchantId") Integer merchantId);
}
