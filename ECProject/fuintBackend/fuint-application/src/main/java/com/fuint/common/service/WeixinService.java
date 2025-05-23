package com.fuint.common.service;

import com.alibaba.fastjson.JSONObject;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtUser;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Date;

/**
 * 微信相关业务接口
 *
 * 
 * 
 */
public interface WeixinService {

    String getAccessToken(Integer merchantId, boolean useCache) throws BusinessCheckException ;

    ResponseObject createPrepayOrder(MtUser userInfo, MtOrder orderInfo, Integer payAmount, String authCode, Integer giveAmount, String ip, String platform) throws BusinessCheckException;

    Map<String,String> processResXml(HttpServletRequest request);

    void processRespXml(HttpServletResponse response, boolean flag);

    JSONObject getWxProfile(Integer merchantId, String code) throws BusinessCheckException;

    JSONObject getWxOpenId(Integer merchantId, String code) throws BusinessCheckException;

    String getPhoneNumber(String encryptedData, String session_key, String iv);

    Boolean sendSubscribeMessage(Integer merchantId, Integer userId, String toUserOpenId, String key, String page, Map<String,Object> params, Date sendTime) throws BusinessCheckException;

    Boolean doSendSubscribeMessage(Integer merchantId, String reqDataJsonStr);

    Map<String, String> queryPaidOrder(Integer storeId, String transactionId, String orderSn);

    Boolean doRefund(Integer storeId, String orderSn, BigDecimal totalAmount, BigDecimal refundAmount, String platform) throws BusinessCheckException;

    String createStoreQrCode(Integer merchantId, Integer storeId, Integer width);

}