package com.smartwater.monitoring.network.dto;

/**
 * Request DTO for creating a reply
 */
public class CommunityReplyRequest {
    private String content;
    private Boolean expertReply;

    public CommunityReplyRequest() {
    }

    public CommunityReplyRequest(String content, Boolean expertReply) {
        this.content = content;
        this.expertReply = expertReply;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getExpertReply() {
        return expertReply;
    }

    public void setExpertReply(Boolean expertReply) {
        this.expertReply = expertReply;
    }
}
