package com.fuint.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.aliyun.oss.OSS;
import com.fuint.common.bean.WxPayBean;
import com.fuint.common.dto.OrderDto;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.enums.*;
import com.fuint.common.http.HttpRESTDataClient;
import com.fuint.common.service.*;
import com.fuint.common.util.AliyunOssUtil;
import com.fuint.common.util.RedisUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.*;
import com.fuint.utils.QRCodeUtil;
import com.fuint.utils.StringUtil;
import com.ijpay.core.enums.SignType;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.WxPayApiConfig;
import com.ijpay.wxpay.WxPayApiConfigKit;
import com.ijpay.wxpay.model.MicroPayModel;
import com.ijpay.wxpay.model.OrderQueryModel;
import com.ijpay.wxpay.model.RefundModel;
import com.ijpay.wxpay.model.UnifiedOrderModel;
import lombok.AllArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.env.Environment;
import weixin.popular.util.JsonUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.*;

/**
 * 微信相关接口
 *
 *
 * 
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class WeixinServiceImpl implements WeixinService {

    private static final Logger logger = LoggerFactory.getLogger(WeixinServiceImpl.class);

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 系统设置服务接口
     * */
    private SettingService settingService;

    /**
     * 系统消息服务接口
     * */
    private MessageService messageService;

    /**
     * 店铺服务接口
     * */
    private StoreService storeService;

    /**
     * 支付服务接口
     * */
    private PaymentService paymentService;

    /**
     * 商户服务接口
     * */
    private MerchantService merchantService;

    private Environment env;

    WxPayBean wxPayBean;

    private static final String CALL_BACK_URL = "/clientApi/pay/weixinCallback";

    private static final String REFUND_NOTIFY_URL = "/clientApi/pay/weixinRefundNotify";

    private static final String FUINT_ACCESS_TOKEN_PRE = "FUINT_ACCESS_TOKEN";

    /**
     * 获取微信accessToken
     *
     * @param merchantId 商户ID
     * @param useCache 是否读取缓存
     * @return
     * */
    @Override
    public String getAccessToken(Integer merchantId, boolean useCache) throws BusinessCheckException {
        String wxAppId = env.getProperty("wxpay.appId");
        String wxAppSecret = env.getProperty("wxpay.appSecret");
        String tokenKey = FUINT_ACCESS_TOKEN_PRE;
        if (merchantId != null && merchantId > 0) {
            MtMerchant mtMerchant = merchantService.queryMerchantById(merchantId);
            if (mtMerchant != null && StringUtil.isNotEmpty(mtMerchant.getWxAppId()) && StringUtil.isNotEmpty(mtMerchant.getWxAppSecret())) {
                wxAppId = mtMerchant.getWxAppId();
                wxAppSecret = mtMerchant.getWxAppSecret();
                tokenKey = tokenKey + merchantId;
            }
        }

        String wxTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
        String url = String.format(wxTokenUrl, wxAppId, wxAppSecret);
        String token = "";

        if (useCache) {
            token = RedisUtil.get(tokenKey);
        }

        if (token == null || StringUtil.isEmpty(token)) {
            try {
                String response = HttpRESTDataClient.requestGet(url);
                JSONObject json = (JSONObject) JSONObject.parse(response);
                if (!json.containsKey("errcode")) {
                    RedisUtil.set(tokenKey, json.get("access_token"), 7200);
                    token = (String) json.get("access_token");
                } else {
                    logger.error("获取微信accessToken出错：" + json.get("errmsg"));
                }
            } catch (Exception e) {
                logger.error("获取微信accessToken异常：" + e.getMessage());
            }
        }

        return token;
    }

    /**
     * 创建支付订单
     *
     * @param userInfo
     * @param orderInfo
     * @param payAmount
     * @param authCode
     * @param giveAmount
     * @param ip
     * @param platform
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseObject createPrepayOrder(MtUser userInfo, MtOrder orderInfo, Integer payAmount, String authCode, Integer giveAmount, String ip, String platform) throws BusinessCheckException {
        logger.info("WeixinService createPrepayOrder inParams userInfo={} payAmount={} giveAmount={} goodsInfo={}", userInfo, payAmount, giveAmount, orderInfo);

        String goodsInfo = orderInfo.getOrderSn();
        if (orderInfo.getType().equals(OrderTypeEnum.PRESTORE.getKey())) {
            goodsInfo = OrderTypeEnum.PRESTORE.getValue();
        }

        // 1. 调用微信接口生成预支付订单
        Map<String, String> reqData = new HashMap<>();
        reqData.put("body", goodsInfo);
        reqData.put("out_trade_no", orderInfo.getOrderSn());
        reqData.put("device_info", "");
        reqData.put("fee_type", "CNY");
        reqData.put("total_fee", payAmount.toString());
        reqData.put("spbill_create_ip", ip);

        // JSAPI支付
        if (orderInfo.getPayType().equals(PayTypeEnum.JSAPI.getKey())) {
            reqData.put("trade_type", PayTypeEnum.JSAPI.getKey());
            reqData.put("openid", userInfo.getOpenId() == null ? "" : userInfo.getOpenId());
        }

        // 刷卡支付
        if (StringUtil.isNotEmpty(authCode)) {
            reqData.put("auth_code", authCode);
        }

        // 更新支付金额
        BigDecimal payAmount1 = new BigDecimal(payAmount).divide(new BigDecimal("100"));
        OrderDto reqDto = new OrderDto();
        reqDto.setId(orderInfo.getId());
        reqDto.setPayAmount(payAmount1);
        reqDto.setPayType(orderInfo.getPayType());
        orderService.updateOrder(reqDto);

        Map<String, String> respData;
        if (reqData.get("auth_code") != null && StringUtil.isNotEmpty(reqData.get("auth_code"))) {
            respData = microPay(orderInfo.getStoreId(), reqData, ip, platform);
        } else {
            respData = jsapiPay(orderInfo.getStoreId(), reqData, ip, platform);
        }

        if (respData == null) {
            logger.error("微信支付接口调用异常......");
            return new ResponseObject(3000, "微信支付接口调用异常", null);
        }

        // 2.更新预支付订单号
        if (respData.get("result_code").equals("SUCCESS")) {
            if (respData.get("trade_type").equals(PayTypeEnum.JSAPI.getKey())) {
                String prepayId = respData.get("prepay_id");
                getApiConfig(orderInfo.getStoreId(), platform);
                WxPayApiConfig wxPayApiConfig = WxPayApiConfigKit.getWxPayApiConfig();
                respData = WxPayKit.miniAppPrepayIdCreateSign(wxPayApiConfig.getAppId(), prepayId, wxPayApiConfig.getPartnerKey(), SignType.MD5);
                String jsonStr = JSON.toJSONString(respData);
                logger.info("小程序支付的参数:" + jsonStr);
            }
        } else {
            logger.error("微信支付接口返回状态失败......" + respData.toString() + "...reason");
            return new ResponseObject(3000, "微信支付失败:" + (respData.get("err_code_des") != null ? respData.get("err_code_des") : "未知错误"), null);
        }

        ResponseObject responseObject = new ResponseObject(200, "微信支付接口返回成功", respData);
        logger.info("WXService createPrepayOrder outParams {}", responseObject.toString());

        return responseObject;
    }

    public Map<String, String> processResXml(HttpServletRequest request) {
        try {
            String xmlMsg = HttpKit.readData(request);
            logger.info("支付通知=" + xmlMsg);
            Map<String, String> result = WxPayKit.xmlToMap(xmlMsg);
            String returnCode = result.get("return_code");
            getApiConfig(0, PlatformTypeEnum.MP_WEIXIN.getCode());
            if (WxPayKit.verifyNotify(result, WxPayApiConfigKit.getWxPayApiConfig().getPartnerKey(), SignType.MD5)) {
                if (WxPayKit.codeIsOk(returnCode)) {
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void processRespXml(HttpServletResponse response, boolean flag){
        Map<String,String> respData = new HashMap<>();
        if (flag) {
            respData.put("return_code", "SUCCESS");
            respData.put("return_msg", "OK");
        }else{
            respData.put("return_code", "FAIL");
            respData.put("return_msg", "FAIL");
        }
        OutputStream outputStream = null;
        try {
            String respXml = WxPayKit.toXml(respData);
            outputStream = response.getOutputStream();
            outputStream.write(respXml.getBytes("UTF-8"));
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 获取微信个人信息
     *
     * @param merchantId
     * @param code
     * @return
     * */
    @Override
    public JSONObject getWxProfile(Integer merchantId, String code) throws BusinessCheckException {
        String wxAppId = env.getProperty("wxpay.appId");
        String wxAppSecret = env.getProperty("wxpay.appSecret");

        if (merchantId != null && merchantId > 0) {
            MtMerchant mtMerchant = merchantService.queryMerchantById(merchantId);
            if (mtMerchant != null && StringUtil.isNotEmpty(mtMerchant.getWxAppId()) && StringUtil.isNotEmpty(mtMerchant.getWxAppSecret())) {
                wxAppId = mtMerchant.getWxAppId();
                wxAppSecret = mtMerchant.getWxAppSecret();
            }
        }

        String wxAccessUrl = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
        String url = String.format(wxAccessUrl, wxAppId, wxAppSecret, code);
        try {
            String response = HttpRESTDataClient.requestGet(url);
            JSONObject json = (JSONObject) JSONObject.parse(response);
            if (!json.containsKey("errcode")) {
                return json;
            } else {
                logger.error("获取微信getWxProfile出错：code = " + json.containsKey("errcode") + ",msg="+ json.get("errmsg"));
            }
        } catch (Exception e) {
            logger.error("获取微信getWxProfile异常：" + e.getMessage());
        }

        return null;
    }

    /**
     * 获取公众号openId
     *
     * @param merchantId
     * @param code
     * @return
     * */
    @Override
    public JSONObject getWxOpenId(Integer merchantId, String code) throws BusinessCheckException {
        String wxAppId = env.getProperty("weixin.official.appId");
        String wxAppSecret = env.getProperty("weixin.official.appSecret");

        if (merchantId != null && merchantId > 0) {
            MtMerchant mtMerchant = merchantService.queryMerchantById(merchantId);
            if (mtMerchant != null && StringUtil.isNotEmpty(mtMerchant.getWxOfficialAppId()) && StringUtil.isNotEmpty(mtMerchant.getWxOfficialAppSecret())) {
                wxAppId = mtMerchant.getWxOfficialAppId();
                wxAppSecret = mtMerchant.getWxOfficialAppSecret();
            }
        }

        String wxAccessUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(wxAccessUrl, wxAppId, wxAppSecret, code);
        try {
            String response = HttpRESTDataClient.requestGet(url);
            JSONObject json = (JSONObject) JSONObject.parse(response);
            if (!json.containsKey("errcode")) {
                return json;
            } else {
                logger.error("获取openId出错：code = " + json.containsKey("errcode") + ",msg="+ json.get("errmsg"));
            }
        } catch (Exception e) {
            logger.error("获取微信openId异常：" + e.getMessage());
        }

        return null;
    }

    /**
     * 获取微信绑定手机号
     *
     * @param encryptedData
     * @param sessionKey
     * @param iv
     * @return
     * */
    @Override
    public String getPhoneNumber(String encryptedData, String sessionKey, String iv) {
        // 被加密的数据
        byte[] dataByte = Base64.getDecoder().decode(encryptedData);
        // 加密秘钥
        byte[] keyByte = Base64.getDecoder().decode(sessionKey);
        // 偏移量
        byte[] ivByte = Base64.getDecoder().decode(iv);
        try {
            // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);// 初始化
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, "UTF-8");
                JSONObject object = JSONObject.parseObject(result);
                return object.getString("phoneNumber");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 发送小程序订阅消息
     *
     * @param merchantId
     * @param userId
     * @param toUserOpenId
     * @param key
     * @param page
     * @param params
     * @param sendTime
     * @return
     * */
    @Override
    public Boolean sendSubscribeMessage(Integer merchantId, Integer userId, String toUserOpenId, String key, String page, Map<String,Object> params, Date sendTime) throws BusinessCheckException {
        if (StringUtil.isEmpty(toUserOpenId) || StringUtil.isEmpty(key) || userId < 1) {
            return false;
        }

        MtSetting mtSetting = settingService.querySettingByName(merchantId, key);
        if (mtSetting == null) {
            return false;
        }

        JSONObject jsonObject = null;
        String templateId = "";
        JSONArray paramArray = null;
        try {
            if (mtSetting != null && mtSetting.getValue().indexOf('}') > 0) {
                jsonObject = JSONObject.parseObject(mtSetting.getValue());
            }
            if (jsonObject != null) {
                templateId = jsonObject.get("templateId").toString();
                paramArray = (JSONArray) JSONObject.parse(jsonObject.get("params").toString());
            }
        } catch (Exception e) {
            logger.info("WeixinService sendSubscribeMessage parse setting error={}", mtSetting);
        }

        if (StringUtil.isEmpty(templateId) || paramArray.size() < 1) {
            logger.info("WeixinService sendSubscribeMessage setting error={}", mtSetting);
            return false;
        }

        JSONObject jsonData = new JSONObject();
        jsonData.put("touser", toUserOpenId); // 接收者的openid
        jsonData.put("template_id", templateId);

        if (StringUtil.isEmpty(page)) {
            page = "pages/index/index";
        }
        jsonData.put("page", page);

        // 组装参数
        JSONObject data = new JSONObject();
        for (int i = 0; i < paramArray.size(); i++) {
             JSONObject para = paramArray.getJSONObject(i);
             String value = para.get("value").toString().replaceAll("\\{", "").replaceAll(".DATA}}", "");
             String paraKey = para.get("key").toString();
             String paraValue = params.get(paraKey).toString();
             JSONObject arg = new JSONObject();
             arg.put("value", paraValue);
             data.put(value, arg);
        }
        jsonData.put("data", data);

        String reqDataJsonStr = JSON.toJSONString(jsonData);

        // 存储到消息表里，后续通过定时任务发送
        MtMessage mtMessage = new MtMessage();
        mtMessage.setMerchantId(merchantId);
        mtMessage.setUserId(userId);
        mtMessage.setType(MessageEnum.SUB_MSG.getKey());
        mtMessage.setTitle(WxMessageEnum.getValue(key));
        mtMessage.setContent(WxMessageEnum.getValue(key));
        mtMessage.setIsRead(YesOrNoEnum.NO.getKey());
        mtMessage.setIsSend(YesOrNoEnum.NO.getKey());
        mtMessage.setSendTime(sendTime);
        mtMessage.setStatus(StatusEnum.ENABLED.getKey());
        mtMessage.setParams(reqDataJsonStr);
        messageService.addMessage(mtMessage);

        return true;
    }

    /**
     * 发送订阅消息
     *
     * @param merchantId
     * @param reqDataJsonStr
     * @return
     * */
    @Override
    public Boolean doSendSubscribeMessage(Integer merchantId, String reqDataJsonStr) {
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + getAccessToken(merchantId, true);
            String response = HttpRESTDataClient.requestPost(url, "application/json; charset=utf-8", reqDataJsonStr);
            logger.info("WeixinService sendSubscribeMessage response={}", response);
            JSONObject json = (JSONObject) JSONObject.parse(response);
            if (json.get("errcode").toString().equals("40001")) {
                getAccessToken(merchantId, false);
                logger.error("发送订阅消息出错error1：" + json.get("errcode").toString());
                return false;
            } else if (!json.get("errcode").toString().equals("0")) {
                logger.error("发送订阅消息出错error2：" + json.get("errcode").toString());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error("发送订阅消息出错：" + e.getMessage());
        }

        return true;
    }

    /**
     * 查询支付订单
     *
     * @param storeId
     * @param transactionId
     * @param orderSn
     * @return
     * */
    @Override
    public Map<String, String> queryPaidOrder(Integer storeId, String transactionId, String orderSn) {
        try {
            getApiConfig(storeId, PlatformTypeEnum.MP_WEIXIN.getCode());
            WxPayApiConfig wxPayApiConfig = WxPayApiConfigKit.getWxPayApiConfig();
            Map<String, String> params = OrderQueryModel.builder()
                    .appid(wxPayApiConfig.getAppId())
                    .mch_id(wxPayApiConfig.getMchId())
                    .transaction_id(transactionId)
                    .out_trade_no(orderSn)
                    .nonce_str(WxPayKit.generateStr())
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);
            logger.info("请求参数：{}", WxPayKit.toXml(params));
            String query = WxPayApi.orderQuery(params);
            Map<String, String> result = WxPayKit.xmlToMap(query);
            logger.info("查询结果: {}", result);
            if (result.get("result_code").equals("FAIL")) {
                result.put("trade_state", "FAIL");
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 刷卡支付
     *
     * @param storeId
     * @param reqData
     * @param ip
     * @param platform
     * @return
     * */
    private Map<String, String> microPay(Integer storeId, Map<String, String> reqData, String ip, String platform) {
        try {
            String orderSn = reqData.get("out_trade_no");

            logger.info("调用微信刷卡支付下单接口入参{}", JsonUtil.toJSONString(reqData));
            logger.info("请求平台：{}, 订单号：{}", platform, orderSn);

            // 支付配置
            getApiConfig(storeId, platform);
            WxPayApiConfig wxPayApiConfig = WxPayApiConfigKit.getWxPayApiConfig();
            Map<String, String> params = MicroPayModel.builder()
                    .appid(wxPayApiConfig.getAppId())
                    .mch_id(wxPayApiConfig.getMchId())
                    .nonce_str(WxPayKit.generateStr())
                    .body(reqData.get("body"))
                    .attach(reqData.get("body"))
                    .out_trade_no(orderSn)
                    .total_fee(reqData.get("total_fee"))
                    .spbill_create_ip(ip)
                    .auth_code(reqData.get("auth_code"))
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);
            String xmlResult = WxPayApi.microPay(false, params);

            // 同步返回结果
            logger.info("xmlResult:" + xmlResult);
            Map<String, String> respMap = WxPayKit.xmlToMap(xmlResult);
            String returnCode = respMap.get("return_code");
            String returnMsg = respMap.get("return_msg");
            if (!WxPayKit.codeIsOk(returnCode)) {
                // 通讯失败
                Map<String, String> payResult = null;
                String errCode = respMap.get("err_code");
                if (StringUtil.isNotEmpty(errCode)) {
                    // 用户支付中，需要输入密码
                    if (errCode.equals("USERPAYING")) {
                        // 等待10秒后查询订单
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        payResult = queryPaidOrder(storeId, respMap.get("transaction_id"), orderSn);
                    }
                }
                if (payResult == null || !payResult.get("trade_state").equals("SUCCESS")) {
                    logger.info("提交刷卡支付失败>>" + xmlResult);
                    return respMap;
                }
            }

            String resultCode = respMap.get("result_code");
            if (!WxPayKit.codeIsOk(resultCode)) {
                logger.info("支付失败>>" + xmlResult);
                logger.error(returnMsg);
                return respMap;
            }

            // 支付成功
            logger.info("刷卡支付返回>>" + respMap.toString());

            if (StringUtil.isNotEmpty(orderSn)) {
                UserOrderDto orderInfo = orderService.getOrderByOrderSn(orderSn);
                if (orderInfo != null) {
                    if (!orderInfo.getStatus().equals(OrderStatusEnum.DELETED.getKey())) {
                        paymentService.paymentCallback(orderInfo);
                    }
                }
            }

            return respMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 小程序、公众号支付
     *
     * @param storeId
     * @param reqData
     * @param ip
     * @param platform
     * @return
     * */
    private Map<String, String> jsapiPay(Integer storeId, Map<String, String> reqData, String ip, String platform) {
        try {
            logger.info("调用微信支付下单接口入参{}", JsonUtil.toJSONString(reqData));
            logger.info("请求平台：{}", platform);
            // 支付配置
            getApiConfig(storeId, platform);
            WxPayApiConfig wxPayApiConfig = WxPayApiConfigKit.getWxPayApiConfig();
            Map<String, String> params = UnifiedOrderModel
                    .builder()
                    .appid(wxPayApiConfig.getAppId())
                    .mch_id(wxPayApiConfig.getMchId())
                    .nonce_str(WxPayKit.generateStr())
                    .body(reqData.get("body"))
                    .attach(reqData.get("body"))
                    .out_trade_no(reqData.get("out_trade_no"))
                    .total_fee(reqData.get("total_fee"))
                    .spbill_create_ip(ip)
                    .notify_url(wxPayApiConfig.getDomain() + CALL_BACK_URL)
                    .trade_type(reqData.get("trade_type"))
                    .openid(reqData.get("openid"))
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);
            String xmlResult = WxPayApi.pushOrder(false, params);

            logger.info(xmlResult);
            Map<String, String> result = WxPayKit.xmlToMap(xmlResult);

            String returnCode = result.get("return_code");
            String returnMsg = result.get("return_msg");
            if (!WxPayKit.codeIsOk(returnCode)) {
                logger.error(returnMsg);
            }
            String resultCode = result.get("result_code");
            if (!WxPayKit.codeIsOk(resultCode)) {
                logger.error(returnMsg);
            }

            logger.info("调用微信支付下单接口返回{}", JsonUtil.toJSONString(result));
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 发起退款
     * @param storeId
     * @param orderSn
     * @param totalAmount
     * @param refundAmount
     * @param platform
     * @return
     * */
    public Boolean doRefund(Integer storeId, String orderSn, BigDecimal totalAmount, BigDecimal refundAmount, String platform) throws BusinessCheckException {
        try {
            logger.info("WeixinService.doRefund orderSn = {}, totalFee = {}, refundFee = {}", orderSn, totalAmount, refundAmount);
            if (StringUtil.isEmpty(orderSn)) {
                throw new BusinessCheckException("退款订单号不能为空...");
            }

            BigDecimal totalFee = totalAmount.multiply(new BigDecimal("100"));
            BigDecimal refundFee = refundAmount.multiply(new BigDecimal("100"));
            Integer totalFeeInt = totalFee.intValue();
            Integer refundFeeInt = refundFee.intValue();

            // 支付配置
            getApiConfig(storeId, platform);
            WxPayApiConfig wxPayApiConfig = WxPayApiConfigKit.getWxPayApiConfig();
            Map<String, String> params = RefundModel.builder()
                    .appid(wxPayApiConfig.getAppId())
                    .mch_id(wxPayApiConfig.getMchId())
                    .nonce_str(WxPayKit.generateStr())
                    .transaction_id("")
                    .out_trade_no(orderSn)
                    .out_refund_no(orderSn)
                    .total_fee(totalFeeInt.toString())
                    .refund_fee(refundFeeInt.toString())
                    .notify_url(wxPayApiConfig.getDomain() + REFUND_NOTIFY_URL)
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);
            String refundStr = WxPayApi.orderRefund(false, params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
            logger.info("WeixinService doRefund params: {}", params);
            logger.info("WeixinService doRefund return: {}", refundStr);
            Map<String, String> result = WxPayKit.xmlToMap(refundStr);
            String returnCode = result.get("return_code");
            String returnMsg = result.get("return_msg");
            if (!WxPayKit.codeIsOk(returnCode)) {
                logger.error(returnMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new BusinessCheckException("WeixinService.doRefund 微信退款失败：" + e.getMessage());
        }
    }

    /***
     * 生成店铺二维码
     *
     * @param merchantId
     * @param storeId
     * @param width
     * @return
     * */
    public String createStoreQrCode(Integer merchantId, Integer storeId, Integer width) {
        try {
            String accessToken = getAccessToken(merchantId, true);
            String url = "https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=" + accessToken;
            String reqDataJsonStr = "";

            Map<String, Object> reqData = new HashMap<>();
            reqData.put("access_token", accessToken);
            reqData.put("path", "pages/index/index?storeId=" + storeId);
            reqData.put("width", width);
            reqDataJsonStr = JsonUtil.toJSONString(reqData);

            byte[] bytes = HttpRESTDataClient.requestPost(url, reqDataJsonStr);
            logger.info("WechatService createStoreQrCode response success");

            String pathRoot = env.getProperty("images.root");
            String baseImage = env.getProperty("images.path");
            String filePath = "storeQr" + storeId + ".png";
            String path = pathRoot + baseImage + filePath;
            QRCodeUtil.saveQrCodeToLocal(bytes, path);

            // 上传阿里云oss
            String mode = env.getProperty("aliyun.oss.mode");
            if (mode.equals("1")) { // 检查是否开启上传
                String endpoint = env.getProperty("aliyun.oss.endpoint");
                String accessKeyId = env.getProperty("aliyun.oss.accessKeyId");
                String accessKeySecret = env.getProperty("aliyun.oss.accessKeySecret");
                String bucketName = env.getProperty("aliyun.oss.bucketName");
                String folder = env.getProperty("aliyun.oss.folder");
                OSS ossClient = AliyunOssUtil.getOSSClient(accessKeyId, accessKeySecret, endpoint);
                File ossFile = new File(path);
                return AliyunOssUtil.upload(ossClient, ossFile, bucketName, folder);
            } else {
                return baseImage + filePath;
            }
        } catch (Exception e) {
            logger.error("生成店铺二维码出错：" + e.getMessage());
        }
        return "";
    }

    /**
     * 获取支付配置
     *
     * @param storeId
     * @param platform
     * @return
     * */
    private WxPayApiConfig getApiConfig(Integer storeId, String platform) throws BusinessCheckException {
        WxPayApiConfig apiConfig;
        MtStore mtStore = storeService.queryStoreById(storeId);
        String mchId = wxPayBean.getMchId();
        String apiV2 = wxPayBean.getApiV2();
        String certPath = wxPayBean.getCertPath();
        if (mtStore != null && StringUtil.isNotEmpty(mtStore.getWxApiV2()) && StringUtil.isNotEmpty(mtStore.getWxMchId())) {
            mchId = mtStore.getWxMchId();
            apiV2 = mtStore.getWxApiV2();
            certPath = mtStore.getWxCertPath();
        }
        apiConfig = WxPayApiConfig.builder()
                   .appId(wxPayBean.getAppId())
                   .mchId(mchId)
                   .partnerKey(apiV2)
                   .certPath(certPath)
                   .domain(wxPayBean.getDomain())
                   .build();
        // 微信内h5公众号支付
        if (platform.equals(PlatformTypeEnum.H5.getCode())) {
            String wxAppId = env.getProperty("weixin.official.appId");
            String wxAppSecret = env.getProperty("weixin.official.appSecret");

            if (mtStore != null) {
                MtMerchant mtMerchant = merchantService.queryMerchantById(mtStore.getMerchantId());
                if (mtMerchant != null && StringUtil.isNotEmpty(mtMerchant.getWxOfficialAppId()) && StringUtil.isNotEmpty(mtMerchant.getWxOfficialAppSecret())) {
                    wxAppId = mtMerchant.getWxOfficialAppId();
                    wxAppSecret = mtMerchant.getWxOfficialAppSecret();
                }
            }

            if (StringUtil.isNotEmpty(wxAppId) && StringUtil.isNotEmpty(wxAppSecret)) {
                apiConfig.setAppId(wxAppId);
                apiConfig.setApiKey(wxAppSecret);
            }
        }
        WxPayApiConfigKit.setThreadLocalWxPayApiConfig(apiConfig);
        logger.info("微信支付参数：{}", JsonUtil.toJSONString(apiConfig));
        return apiConfig;
    }
}
