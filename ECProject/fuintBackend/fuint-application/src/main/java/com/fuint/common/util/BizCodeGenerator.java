package com.fuint.common.util;

import com.fuint.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 业务Code生成器
 *
 * 
 * 
 */
public class BizCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(BizCodeGenerator.class);

    private static String DATE_FORMAT="yyyyMMddHHmmssSSS";

    /**
     * 获取指定前缀的Code字符串
     * @param preString 前缀字符串
     * @return
     */
    public synchronized static String getPreCodeString(String preString) {
        StringBuffer result = new StringBuffer();
        if(StringUtil.isNotEmpty(preString)){
            result.append(preString);
        }

        String dateStr =  new SimpleDateFormat(DATE_FORMAT).format(new Date());
        result.append(dateStr);

        String randomStr = SeqUtil.getRandomNumber(2);
        result.append(randomStr);
        return result.toString();
    }


    /**
     * 生成6位数字短信验证码
     * @param
     * @return
     */
    public synchronized static String getVerifyCode() {
        String verifyCode = getFixLengthString(6);
        return verifyCode;
    }

    /**
     * 返回长度为【strLength】的随机数，在前面补0
     * @return
     */
    private static String getFixLengthString(int strLength) {
        Random rm = new Random();
        // 获得随机数
        double process = (1 + rm.nextDouble()) * Math.pow(10, strLength);
        // 将获得的获得随机数转化为字符串
        String codeStr = String.valueOf(process);
        // 返回固定的长度的随机数
        return codeStr.substring(1, strLength + 1);
    }
}
