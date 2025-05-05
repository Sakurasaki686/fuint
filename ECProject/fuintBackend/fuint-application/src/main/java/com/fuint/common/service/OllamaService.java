package com.fuint.common.service;

import java.util.List;
import java.util.Map;

public interface OllamaService {
    String generateText(String prompt, String model);
    String generateText(String prompt, String model, List<Map<String, String>> history);
}