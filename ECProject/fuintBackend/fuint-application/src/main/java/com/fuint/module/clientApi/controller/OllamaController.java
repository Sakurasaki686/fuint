package com.fuint.module.clientApi.controller;

import com.alibaba.fastjson.JSONObject;
import com.fuint.common.util.StringUtil;
import com.fuint.common.dto.GoodsDto;
import com.fuint.common.service.GoodsService;
import com.fuint.common.service.OllamaService;
import com.fuint.common.service.SettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ollama")
public class OllamaController {

    private static final Logger logger = LoggerFactory.getLogger(OllamaController.class);

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SettingService settingService;

    @PostMapping("/generate")
    public Map<String, Object> generateTextRouter(@RequestBody Map<String, Object> request) {
        String prompt = (String) request.get("prompt");
        String model = (String) request.get("model");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

        logger.info("Controller 收到请求: prompt='{}', model='{}', history size={}", prompt, model, (history != null ? history.size() : 0));

        // 判断是否是推荐/查找请求
        if (prompt != null && (prompt.contains("推荐") || prompt.contains("介绍点") || prompt.contains("找一下") || prompt.contains("搜一下") || prompt.contains("搜索"))) {
            logger.info("检测到推荐/查找意图，尝试处理...");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error("延迟执行时发生错误: {}", e.getMessage(), e);
            }

            String keyword = extractKeyword(prompt);
            logger.info("提取到的关键词: '{}'", keyword);

            if (!StringUtil.isEmpty(keyword)) {
                try {
                    int searchLimit = 3;
                    // **** 调用新的 Service 方法 ****
                    // 假设不需要 storeId 和 merchantId，如果需要则从请求或上下文中获取并传入
                    List<GoodsDto> recommendedGoods = goodsService.searchRecommendedGoods(keyword, searchLimit, null, null);

                    if (!recommendedGoods.isEmpty()) {
                        logger.info("根据关键词 '{}' 找到 {} 个推荐商品", keyword, recommendedGoods.size());
                        // 构建推荐响应
                        Map<String, Object> recommendationResponse = new HashMap<>();
                        recommendationResponse.put("type", "recommendation");
                        recommendationResponse.put("message", "根据您的需求，为您找到以下商品：");

                        List<Map<String, Object>> productList = new ArrayList<>();
                        String imageBasePath = settingService.getUploadBasePath();
                        logger.debug("图片基础路径 (imageBasePath): {}", imageBasePath);

                        for (GoodsDto goods : recommendedGoods) { // 直接遍历返回的列表
                            Map<String, Object> productData = new HashMap<>();
                            productData.put("id", goods.getId());
                            productData.put("name", goods.getName());
                            String imageUrl = null;
                            String goodsImagesJson = goods.getImages();

                            logger.debug("处理商品 ID: {}, 名称: '{}', 原始 Images JSON: '{}'", goods.getId(), goods.getName(), goodsImagesJson);

                            if (!StringUtil.isEmpty(goodsImagesJson)) {
                                try {
                                    List<String> images = JSONObject.parseArray(goodsImagesJson, String.class);
                                    logger.debug("JSON 解析后的 images 列表: {}", images);
                                    if (images != null && !images.isEmpty()) {
                                        String firstImageRelativePath = images.get(0);
                                        logger.debug("获取到的第一个相对图片路径: '{}'", firstImageRelativePath);
                                        if (!StringUtil.isEmpty(firstImageRelativePath)) {
                                            if (StringUtil.isEmpty(imageBasePath)) { /* ... */ imageUrl = firstImageRelativePath; }
                                            else { /* ... 拼接逻辑 ... */
                                                if (imageBasePath.endsWith("/") && firstImageRelativePath.startsWith("/")) { imageUrl = imageBasePath + firstImageRelativePath.substring(1); }
                                                else if (!imageBasePath.endsWith("/") && !firstImageRelativePath.startsWith("/")) { imageUrl = imageBasePath + "/" + firstImageRelativePath; }
                                                else { imageUrl = imageBasePath + firstImageRelativePath; }
                                            }
                                            logger.debug("拼接后的完整 imageUrl: '{}'", imageUrl);
                                        } else { logger.warn("解析出的第一个图片路径为空字符串。"); }
                                    } else { logger.warn("JSON 解析成功，但 images 列表为空。"); }
                                } catch (Exception e) { logger.error("解析商品图片 JSON 失败: ...", e); }
                            } else { logger.warn("商品 ID: {} 的 images 字段为空。", goods.getId()); }

                            if (imageUrl == null) {
                                imageUrl = "/static/images/default_product.png";
                                logger.debug("imageUrl 为 null，使用默认图片: {}", imageUrl);
                            }
                            productData.put("imageUrl", imageUrl);
                            productList.add(productData);
                        }
                        recommendationResponse.put("products", productList);
                        logger.info("准备返回推荐响应: {}", recommendationResponse);
                        return recommendationResponse;
                    } else {
                        logger.info("根据关键词 '{}' 未找到推荐商品", keyword); // 日志修改
                        Map<String, Object> notFoundResponse = new HashMap<>();
                        notFoundResponse.put("type", "text");
                        notFoundResponse.put("response", "抱歉，目前没有找到与“" + keyword + "”相关的商品。您可以看看别的。");
                        return notFoundResponse;
                    }
                } catch (Exception e) {
                    logger.error("搜索推荐商品时发生错误: keyword='{}', error={}", keyword, e.getMessage(), e); // 日志修改
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("type", "text");
                    errorResponse.put("response", "抱歉，查找商品时系统出错了，请稍后再试。");
                    return errorResponse;
                }
            } else {
                logger.warn("包含推荐意图但未能提取有效关键词: prompt='{}'", prompt);
                Map<String, Object> fallbackResponse = new HashMap<>();
                fallbackResponse.put("type", "text");
                fallbackResponse.put("response", "您想让我推荐什么呢？可以说得更具体一点吗？比如商品类型或者名称。");
                return fallbackResponse;
            }
        } // if (推荐意图) 结束

        // 如果不是推荐/查找请求，则调用 AI 服务 (保持不变)
        logger.info("非推荐/查找意图，调用 OllamaService...");
        String aiResponseText;
        if (history != null && !history.isEmpty()) {
            aiResponseText = ollamaService.generateText(prompt, model, history);
        } else {
            aiResponseText = ollamaService.generateText(prompt, model);
        }

        Map<String, Object> aiResponse = new HashMap<>();
        aiResponse.put("type", "text");
        aiResponse.put("response", aiResponseText);
        logger.info("准备返回 AI 响应: {}", aiResponse);
        return aiResponse;
    }

    // 关键词提取方法
    private String extractKeyword(String prompt) {
        // 第一步：去除常见的无意义前缀（如 "给我"、"推荐" 等）
        String keyword = prompt.replaceAll("^(给我|请给我|我想|推荐|介绍|看看|有没有)\\s*(一个|几个|某种|什么|的|好用|便宜|质量好)?\\s*", "");
        keyword = keyword.replaceAll("^(找一下|找个|搜一下|搜索|买一个|要一个)\\s*", "");

        // 第二步：去掉“的”及其前面的内容
        keyword = keyword.replaceAll(".*的\\s*", "");

        // 第三步：去掉末尾的标点符号
        keyword = keyword.replaceAll("[的了呢吗嘛啊呀？?！!。.,，]$", "").trim();

        // 第四步：如果结果为空，返回 null；否则返回提取的关键词
        return keyword.isEmpty() ? null : keyword;
    }
}