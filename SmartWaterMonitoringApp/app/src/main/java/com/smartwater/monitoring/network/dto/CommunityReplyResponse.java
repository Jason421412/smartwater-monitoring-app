package com.smartwater.monitoring.network.dto;

/**
 * Response DTO for community reply
 */
public class CommunityReplyResponse {
    private Long id;
    private Long postId;
    private String authorName;
    private String authorEmail;
    private String content;
    private Boolean expertReply;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getExpertReply() { return expertReply; }
    public void setExpertReply(Boolean expertReply) { this.expertReply = expertReply; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
