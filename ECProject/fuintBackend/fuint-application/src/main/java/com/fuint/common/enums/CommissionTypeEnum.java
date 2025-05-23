package com.fuint.common.enums;

/**
 * 分佣类型枚举
 */
public enum CommissionTypeEnum {
    GOODS("goods", "商品销售"),
    COUPON("coupon", "卡券销售"),
    RECHARGE("recharge", "会员充值");

    private String key;

    private String value;

    CommissionTypeEnum(String key, String value) {
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
