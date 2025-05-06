package com.fuint.common.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fuint.common.service.OllamaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OllamaServiceImpl implements OllamaService {

    private static final Logger logger = LoggerFactory.getLogger(OllamaServiceImpl.class);
    private final RestTemplate restTemplate;
    private final String ollamaApiUrl;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_INTRODUCTION = "本系统名为 AIssist Shop，是一个在传统系统的基础之上融合了当前非常主流的 LLM 的实体店铺会员管理和营销系统。主要功能包含 AI 对话与推荐、电子优惠券、储值卡、实体卡、集次卡（计次卡）、短信发送、储值卡、会员积分、会员等级权益体系，支付收款等会员日常营销工具。本系统适用于各类实体店铺，如零售超市、酒吧、酒店、汽车4S店、鲜花店、奶茶店、甜品店、餐饮店、农家乐等，是 AI 时代下实体店铺会员营销必备的一款利器。";

    public OllamaServiceImpl(@Value("${ollama.api.url}") String ollamaApiUrl) {
        this.ollamaApiUrl = ollamaApiUrl;
        this.restTemplate = new RestTemplate();

        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.valueOf("application/x-ndjson")
        ));
        restTemplate.getMessageConverters().add(converter);
    }

    // 无历史记录的 generateText 方法
    @Override
    public String generateText(String prompt, String model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        String modelToUse = model != null && !model.isEmpty() ? model : "qwen2.5:1.5b";
        requestBody.put("model", modelToUse);

        // 构建包含系统介绍和指令的 Prompt
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append("你现在是 AIssist Shop 系统的专属 AI 助手。\n");
        fullPrompt.append(SYSTEM_INTRODUCTION).append("\n");
        fullPrompt.append("请利用你对 AIssist Shop 功能和适用场景的了解，像一个专业的系统助手一样，自然地回答用户的问题。\n");
        fullPrompt.append("请直接给出回答，避免提及你正在参考系统介绍。\n\n");
        fullPrompt.append("用户: ").append(prompt).append("\n");
        fullPrompt.append("助手:");

        logger.debug("构建完成的完整 Prompt (无历史) 发送给 Ollama:\n---\n{}\n---", fullPrompt.toString());

        requestBody.put("prompt", fullPrompt.toString());
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        String requestUrl = this.ollamaApiUrl;

        try {
            logger.info("向 Ollama 发送无历史请求: URL={}, Model={}, Prompt Length={}", requestUrl, modelToUse, fullPrompt.length());
            Map response = restTemplate.postForObject(requestUrl, requestEntity, Map.class);
            logger.info("Ollama 返回的完整数据 keys: {}", response != null ? response.keySet() : "null");
            logger.debug("Ollama 完整响应 Body: {}", response);

            if (response != null && response.containsKey("response")) {
                Object responseObject = response.get("response");
                String rawResponse = (responseObject instanceof String) ? (String) responseObject : String.valueOf(responseObject);
                return rawResponse.trim();
            } else {
                logger.warn("Ollama 响应中未找到 'response' 字段，原始响应: {}", response);
                if (response != null && response.containsKey("error")) {
                    logger.error("Ollama 返回了错误信息: {}", response.get("error"));
                    return "AI 服务返回错误: " + response.get("error");
                }
                return "";
            }
        } catch (HttpClientErrorException e) {
            logger.error("请求 Ollama 时发生客户端错误，URL: {}, 状态码: {}, 响应体: {}", requestUrl, e.getStatusCode(), e.getResponseBodyAsString(), e);
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.NOT_FOUND && responseBody != null && responseBody.contains("\"error\"")) {
                try { Map<String, String> errorResponse = objectMapper.readValue(responseBody, Map.class); String errorMessage = errorResponse.get("error"); if (errorMessage != null && errorMessage.toLowerCase().contains("not found")) { return "抱歉，您选择的模型 \"" + modelToUse + "\" 尚未在服务器上安装或加载。请选择其他模型。"; } } catch (JsonProcessingException jsonEx) { if (responseBody.toLowerCase().contains("not found")) { return "抱歉，请求的模型 \"" + modelToUse + "\" 不存在或无法找到。"; } } return "请求的资源未找到 (404)。";
            } else if (responseBody != null && responseBody.toLowerCase().contains("failed to load model")) { return "抱歉，模型 \"" + modelToUse + "\" 加失败。请选择其他模型或联系管理员。"; }
            return "请求处理失败: HTTP " + e.getStatusCode() + " - " + (responseBody.isEmpty() ? e.getStatusText() : responseBody);
        } catch (RestClientResponseException e) {
            logger.error("请求 Ollama 时发生服务端或未知错误，URL: {}, 状态码: {}, 响应体: {}", requestUrl, e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && responseBody.toLowerCase().contains("failed to load model")) { return "抱歉，模型 \"" + modelToUse + "\" 在服务器端加载失败。请稍后再试或联系管理员。"; }
            return "与AI服务通信时发生错误: HTTP " + e.getRawStatusCode() + " - " + (responseBody.isEmpty() ? "Server Error" : responseBody);
        } catch (Exception e) {
            logger.error("处理 Ollama 请求时发生未知错误，URL: {}", requestUrl, e);
            return "处理您的请求时发生网络或未知错误: " + e.getMessage();
        }
    }


    // 带历史记录的 generateText 方法
    @Override
    public String generateText(String prompt, String model, List<Map<String, String>> history) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        String modelToUse = model != null && !model.isEmpty() ? model : "qwen2.5:1.5b";
        requestBody.put("model", modelToUse);

        // 构建带有指令和完整系统介绍的 Prompt
        StringBuilder fullPrompt = new StringBuilder();

        // 1. 设定角色并融入完整的系统知识
        fullPrompt.append("你现在是 AIssist Shop 系统的专属 AI 助手，之后必须以友好的 AI 助手的身份与用户交流。\n");
        fullPrompt.append(SYSTEM_INTRODUCTION).append("\n"); // 使用完整的系统介绍
        fullPrompt.append("请利用你对 AIssist Shop 功能和适用场景的了解，结合下面的对话历史（如果有的话。这个是你跟用户的对话历史，为了防止你遗忘，所以给你展示了出来），像一个专业的系统助手一样，自然地、连贯地回答用户的最后一个问题。\n");
        fullPrompt.append("请直接给出回答，避免提及你正在参考系统介绍或对话历史本身。\n\n");

        // 2. 分隔并添加历史对话
        fullPrompt.append("--- 对话历史开始 ---\n");
        if (history != null && !history.isEmpty()) {
            for (Map<String, String> message : history) {
                String role = message.get("role");
                String content = message.get("content");
                if ("user".equals(role)) {
                    fullPrompt.append("用户: ").append(content).append("\n");
                } else if ("assistant".equals(role)) {
                    fullPrompt.append("助手: ").append(content).append("\n");
                }
            }
        } else {
            fullPrompt.append("(当前无历史记录)\n");
        }
        fullPrompt.append("--- 对话历史结束 ---\n\n");

        // 3. 添加当前用户提问
        fullPrompt.append("用户: ").append(prompt).append("\n");

        // 4. 提示模型开始生成助手角色的回答
        fullPrompt.append("助手:");

        // 打印完整的 Prompt (保持不变)
        logger.debug("构建完成的完整 Prompt (带历史) 发送给 Ollama:\n---\n{}\n---", fullPrompt.toString());

        requestBody.put("prompt", fullPrompt.toString());
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        String requestUrl = this.ollamaApiUrl;

        try {
            logger.info("向 Ollama 发送带历史和指令的请求: URL={}, Model={}, History Size={}, Prompt Length={}", requestUrl, modelToUse, (history != null ? history.size() : 0), fullPrompt.length());
            Map response = restTemplate.postForObject(requestUrl, requestEntity, Map.class);
            logger.info("Ollama 返回的完整数据 keys: {}", response != null ? response.keySet() : "null");
            logger.debug("Ollama 完整响应 Body: {}", response);

            if (response != null && response.containsKey("response")) {
                Object responseObject = response.get("response");
                String rawResponse = (responseObject instanceof String) ? (String) responseObject : String.valueOf(responseObject);
                return rawResponse.trim();
            } else {
                logger.warn("Ollama 响应中未找到 'response' 字段，原始响应: {}", response);
                if (response != null && response.containsKey("error")) {
                    logger.error("Ollama 返回了错误信息: {}", response.get("error"));
                    return "AI 服务返回错误: " + response.get("error");
                }
                return "";
            }
        } catch (HttpClientErrorException e) {
            logger.error("请求 Ollama 时发生客户端错误，URL: {}, 状态码: {}, 响应体: {}", requestUrl, e.getStatusCode(), e.getResponseBodyAsString(), e);
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.NOT_FOUND && responseBody != null && responseBody.contains("\"error\"")) {
                try { Map<String, String> errorResponse = objectMapper.readValue(responseBody, Map.class); String errorMessage = errorResponse.get("error"); if (errorMessage != null && errorMessage.toLowerCase().contains("not found")) { return "抱歉，您选择的模型 \"" + modelToUse + "\" 尚未在服务器上安装或加载。请选择其他模型。"; } } catch (JsonProcessingException jsonEx) { if (responseBody.toLowerCase().contains("not found")) { return "抱歉，请求的模型 \"" + modelToUse + "\" 不存在或无法找到。"; } } return "请求的资源未找到 (404)。";
            } else if (responseBody != null && responseBody.toLowerCase().contains("failed to load model")) { return "抱歉，模型 \"" + modelToUse + "\" 加失败。请选择其他模型或联系管理员。"; }
            return "请求处理失败: HTTP " + e.getStatusCode() + " - " + (responseBody.isEmpty() ? e.getStatusText() : responseBody);
        } catch (RestClientResponseException e) {
            logger.error("请求 Ollama 时发生服务端或未知错误，URL: {}, 状态码: {}, 响应体: {}", requestUrl, e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && responseBody.toLowerCase().contains("failed to load model")) { return "抱歉，模型 \"" + modelToUse + "\" 在服务器端加载失败。请稍后再试或联系管理员。"; }
            return "与AI服务通信时发生错误: HTTP " + e.getRawStatusCode() + " - " + (responseBody.isEmpty() ? "Server Error" : responseBody);
        } catch (Exception e) {
            logger.error("处理 Ollama 请求时发生未知错误，URL: {}", requestUrl, e);
            return "处理您的请求时发生网络或未知错误: " + e.getMessage();
        }
    }
}