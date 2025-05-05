package com.fuint.common.util; // 使用你自己的工具包路径

public class StringUtil {

    /**
     * 判断字符串是否为空（null 或 ""）
     * @param str 要判断的字符串
     * @return 如果字符串为 null 或空字符串，则返回 true，否则返回 false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty(); 
        // 使用 trim().isEmpty() 可以同时判断 null, "" 以及只包含空格的字符串
    }

    /**
     * 判断字符串是否不为空
     * @param str 要判断的字符串
     * @return 如果字符串不为 null 且不为空字符串，则返回 true，否则返回 false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    // 你可以根据需要添加其他常用的字符串工具方法
}