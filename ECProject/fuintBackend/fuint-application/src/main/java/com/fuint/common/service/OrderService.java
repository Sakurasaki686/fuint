package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.dto.OrderDto;
import com.fuint.common.param.OrderListParam;
import com.fuint.common.param.SettlementParam;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtCart;
import com.fuint.repository.model.MtOrder;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单业务接口
 *
 * 
 * 
 */
public interface OrderService extends IService<MtOrder> {

    /**
     * 获取用户的订单
     * @param  orderListParam
     * @throws BusinessCheckException
     * */
    PaginationResponse getUserOrderList(OrderListParam orderListParam) throws BusinessCheckException;

    /**
     * 创建订单
     *
     * @param  reqDto
     * @throws BusinessCheckException
     */
    MtOrder saveOrder(OrderDto reqDto) throws BusinessCheckException;

    /**
     * 订单提交结算
     * */
    Map<String, Object> doSettle(HttpServletRequest request, SettlementParam settlementParam) throws BusinessCheckException;

    /**
     * 获取订单详情
     *
     * @param  id
     * @throws BusinessCheckException
     */
    MtOrder getOrderInfo(Integer id) throws BusinessCheckException;

    /**
     * 根据ID获取订单
     *
     * @param  id
     * @throws BusinessCheckException
     */
    UserOrderDto getOrderById(Integer id) throws BusinessCheckException;

    /**
     * 根据ID获取订单
     *
     * @param  id
     * @throws BusinessCheckException
     */
    UserOrderDto getMyOrderById(Integer id) throws BusinessCheckException;

    /**
     * 取消订单
     * @param  id 订单ID
     * @return
     * */
    MtOrder cancelOrder(Integer id, String remark) throws BusinessCheckException;

    /**
     * 根据订单ID删除
     *
     * @param  id       ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     */
    void deleteOrder(Integer id, String operator) throws BusinessCheckException;

    /**
     * 根据订单号获取订单
     *
     * @param  orderSn
     * @throws BusinessCheckException
     */
    UserOrderDto getOrderByOrderSn(String orderSn) throws BusinessCheckException;

    /**
     * 更新订单
     * @param  reqDto
     * @throws BusinessCheckException
     * */
    MtOrder updateOrder(OrderDto reqDto) throws BusinessCheckException;

    /**
     * 更新订单
     * @param mtOrder
     * @return
     * @throws BusinessCheckException
     * */
    MtOrder updateOrder(MtOrder mtOrder) throws BusinessCheckException;

    /**
     * 把订单置为已支付
     * @param orderId
     * @param payAmount
     * @return
     * */
    Boolean setOrderPayed(Integer orderId, BigDecimal payAmount) throws BusinessCheckException;

    /**
     * 根据条件搜索订单
     * */
    List<MtOrder> getOrderListByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 获取订单总数
     * */
    BigDecimal getOrderCount(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 获取订单数量
     * */
    BigDecimal getOrderCount(Integer merchantId, Integer storeId, Date beginTime, Date endTime) throws BusinessCheckException;

    /**
     * 计算购物车
     * */
    Map<String, Object> calculateCartGoods(Integer merchantId, Integer userId, List<MtCart> cartList, Integer couponId, boolean isUsePoint, String platform, String orderMode) throws BusinessCheckException;

    /**
     * 获取支付金额
     * */
    BigDecimal getPayMoney(Integer merchantId, Integer storeId, Date beginTime, Date endTime) throws BusinessCheckException;

    /**
     * 获取支付人数
     * */
    Integer getPayUserCount(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 获取支付金额
     * */
    BigDecimal getPayMoney(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 获取会员支付金额
     * */
    BigDecimal getUserPayMoney(Integer userId) throws BusinessCheckException;

    /**
     * 获取会员订单数
     * */
    Integer getUserPayOrderCount(Integer userId) throws BusinessCheckException;
}
