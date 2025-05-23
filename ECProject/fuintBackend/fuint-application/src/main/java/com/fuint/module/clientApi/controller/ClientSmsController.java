package com.fuint.module.clientApi.controller;

import com.fuint.common.service.CaptchaService;
import com.fuint.common.service.MerchantService;
import com.fuint.common.service.SendSmsService;
import com.fuint.common.service.VerifyCodeService;
import com.fuint.common.util.BizCodeGenerator;
import com.fuint.common.util.PhoneFormatCheckUtils;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtVerifyCode;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * 手机短信controller
 *
 *
 *
 */
@Api(tags="会员端-手机短信相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/clientApi/sms")
public class ClientSmsController extends BaseController {

    /**
     * 系统环境变量
     * */
    private Environment env;

    /**
     * 验证码服务接口
     */
    private VerifyCodeService verifyCodeService;

    /**
     * 短信发送接口
     */
    private SendSmsService sendSmsService;

    /**
     * 图形验证码
     * */
    private CaptchaService captchaService;

    /**
     * 商户服务接口
     */
    private MerchantService merchantService;

    /**
     * 发送验证码短信
     */
    @ApiOperation(value = "发送验证码短信")
    @RequestMapping(value = "/sendVerifyCode", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject sendVerifyCode(HttpServletRequest request, @RequestBody Map<String, Object> param) throws Exception {
        String mobile = param.get("mobile") == null ? "" : param.get("mobile").toString();
        String captchaCode = param.get("captchaCode") == null ? "" : param.get("captchaCode").toString();
        String uuid = param.get("uuid") == null ? "" : param.get("uuid").toString();
        if (StringUtil.isEmpty(captchaCode)) {
            return getFailureResult(201,"图形验证码不能为空");
        }

        HttpSession session = request.getSession();
        boolean captchaVerify;
        if (StringUtil.isNotEmpty(uuid)) {
            captchaVerify = captchaService.checkCodeByUuid(captchaCode, uuid);
        } else {
            captchaVerify = captchaService.checkCode(captchaCode, session);
        }
        if (!captchaVerify) {
            return getFailureResult(201,"图形验证码有误");
        }

        // 验证码时间间隔
        String second = env.getProperty("SMS.PERIOD");
        if (null != second && second.length() > 0) {
            Integer.parseInt(second);
        }

        if (StringUtil.isEmpty(mobile)) {
            return getFailureResult(201,"手机号码不能为空");
        } else {
            if (!PhoneFormatCheckUtils.isChinaPhoneLegal(mobile)) {
                return getFailureResult(201,"手机号码格式不正确");
            }
        }

        // 插入验证码表
        String verifyCode= BizCodeGenerator.getVerifyCode();
        MtVerifyCode mtVerifyCode = verifyCodeService.addVerifyCode(mobile, verifyCode,60);
        if (null == mtVerifyCode) {
            return getFailureResult(201,"验证码发送失败");
        } else if(mtVerifyCode.getVerifyCode().equals("1") && mtVerifyCode.getId() == null){
            return getFailureResult(201,"验证码发送间隔太短,请稍后再试！");
        }

        // 发送短信
        Map<Boolean,List<String>> result;
        List<String> mobileList = new ArrayList<>();
        mobileList.add(mobile);

        String merchantNo = request.getHeader("merchantNo") == null ? "" : request.getHeader("merchantNo");
        Integer merchantId = merchantService.getMerchantId(merchantNo);

        // 短信模板
        Map<String, String> params = new HashMap<>();
        params.put("code", verifyCode);
        result = sendSmsService.sendSms(merchantId,"login-code", mobileList, params);
        return getSuccessResult(result);
    }
}
