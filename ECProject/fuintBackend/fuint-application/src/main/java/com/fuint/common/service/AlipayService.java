package com.fuint.common.service;

import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtUser;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝相关业务接口
 *
 *
 *
 */
public interface AlipayService {

    ResponseObject createPrepayOrder(MtUser userInfo, MtOrder orderInfo, Integer payAmount, String authCode, Integer giveAmount, String ip, String platform) throws BusinessCheckException;

    Boolean checkCallBack(Map<String, String> params) throws Exception;

    Map<String, String> queryPaidOrder(Integer storeId, String tradeNo, String orderSn) throws BusinessCheckException;

    Boolean doRefund(Integer storeId, String orderSn, BigDecimal totalAmount, BigDecimal refundAmount, String platform) throws BusinessCheckException;

}