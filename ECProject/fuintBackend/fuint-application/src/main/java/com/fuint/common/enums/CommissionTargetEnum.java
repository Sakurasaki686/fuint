package com.fuint.common.enums;

/**
 * 分佣对象枚举
 */
public enum CommissionTargetEnum {
    MEMBER("member", "会员分销"),
    STAFF("staff", "员工提成");

    private String key;

    private String value;

    CommissionTargetEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
