package com.fuint.common.dto;

public class OllamaRequest {
    private String prompt;

    // 需要 getter 和 setter 方法，以便 Spring 的 Jackson 库能正确映射
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    // 可以添加无参构造函数（通常是好习惯）
    public OllamaRequest() {}
}