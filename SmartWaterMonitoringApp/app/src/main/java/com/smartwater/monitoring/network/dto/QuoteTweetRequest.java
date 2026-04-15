package com.smartwater.monitoring.network.dto;

/**
 * Request DTO for quote tweeting
 */
public class QuoteTweetRequest {
    private String content;

    public QuoteTweetRequest() {}

    public QuoteTweetRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
