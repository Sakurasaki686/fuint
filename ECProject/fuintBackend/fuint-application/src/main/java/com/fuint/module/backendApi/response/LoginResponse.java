package com.fuint.module.backendApi.response;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 后台登录返回信息
 *
 *
 */
@Data
public class LoginResponse implements Serializable {
    private boolean isLogin;
    private Date tokenCreatedTime;
    private Date tokenExpiryTime;
    private String token;
}
