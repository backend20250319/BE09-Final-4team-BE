package com.hermes.attendanceservice.aichat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openai")
public class AiChatProperties {
    /** e.g. https://api.openai.com/v1 */
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey;
    /** e.g. gpt-4o-mini or gpt-4o */
    private String model = "gpt-4o-mini";
    private Double temperature = 0.2;
    private Integer maxTokens = 2048;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
} 