package com.fuint.repository.mapper;

import com.fuint.repository.model.MtVerifyCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 短信验证码表 Mapper 接口
 *
 * 
 *
 */
public interface MtVerifyCodeMapper extends BaseMapper<MtVerifyCode> {

    MtVerifyCode queryByMobileVerifyCode(@Param("mobile") String mobile, @Param("verifyCode") String verifyCode, @Param("queryTime") Date queryTime);

    List<MtVerifyCode> queryVerifyCodeLastRecord(@Param("mobile") String mobile);

}
