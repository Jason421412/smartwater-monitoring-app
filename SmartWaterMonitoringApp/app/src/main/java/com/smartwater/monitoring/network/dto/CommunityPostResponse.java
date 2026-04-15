package com.smartwater.monitoring.network.dto;

/**
 * Response DTO for community post (Twitter-style)
 */
public class CommunityPostResponse {
    private Long id;

    // Author info
    private Long authorId;
    private String authorName;
    private String authorEmail;
    private String authorProfileImageUrl;
    private Boolean authorIsExpert;

    private String content;
    private String photoUrl;
    private String location;
    private Double ph;
    private Double temperature;
    private Double turbidity;
    private String type;
    private String createdAt;

    // Engagement counts (mapped from backend 'likes' field)
    private Integer likes;
    private Integer likeCount; // Alias for compatibility
    private Integer replyCount;
    private Integer retweetCount;
    private Integer viewCount;
    private Integer bookmarkCount;

    // Retweet info
    private Boolean isRetweet;
    private Long originalPostId;
    private CommunityPostResponse originalPost;

    // Retweeted by info
    private String retweetedByName;
    private Long retweetedById;

    // Current user's interaction status
    private Boolean isLikedByCurrentUser;
    private Boolean isRetweetedByCurrentUser;
    private Boolean isBookmarkedByCurrentUser;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getAuthorProfileImageUrl() { return authorProfileImageUrl; }
    public void setAuthorProfileImageUrl(String authorProfileImageUrl) { this.authorProfileImageUrl = authorProfileImageUrl; }

    public Boolean getAuthorIsExpert() { return authorIsExpert; }
    public void setAuthorIsExpert(Boolean authorIsExpert) { this.authorIsExpert = authorIsExpert; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getTurbidity() { return turbidity; }
    public void setTurbidity(Double turbidity) { this.turbidity = turbidity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Engagement getters - handle both 'likes' and 'likeCount' for compatibility
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public Integer getLikeCount() { return likeCount != null ? likeCount : likes; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getReplyCount() { return replyCount; }
    public void setReplyCount(Integer replyCount) { this.replyCount = replyCount; }

    public Integer getRetweetCount() { return retweetCount != null ? retweetCount : 0; }
    public void setRetweetCount(Integer retweetCount) { this.retweetCount = retweetCount; }

    public Integer getViewCount() { return viewCount != null ? viewCount : 0; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getBookmarkCount() { return bookmarkCount != null ? bookmarkCount : 0; }
    public void setBookmarkCount(Integer bookmarkCount) { this.bookmarkCount = bookmarkCount; }

    // Retweet info
    public Boolean getIsRetweet() { return isRetweet != null ? isRetweet : false; }
    public void setIsRetweet(Boolean isRetweet) { this.isRetweet = isRetweet; }

    public Long getOriginalPostId() { return originalPostId; }
    public void setOriginalPostId(Long originalPostId) { this.originalPostId = originalPostId; }

    public CommunityPostResponse getOriginalPost() { return originalPost; }
    public void setOriginalPost(CommunityPostResponse originalPost) { this.originalPost = originalPost; }

    public String getRetweetedByName() { return retweetedByName; }
    public void setRetweetedByName(String retweetedByName) { this.retweetedByName = retweetedByName; }

    public Long getRetweetedById() { return retweetedById; }
    public void setRetweetedById(Long retweetedById) { this.retweetedById = retweetedById; }

    // User interaction status
    public Boolean getIsLikedByCurrentUser() { return isLikedByCurrentUser != null ? isLikedByCurrentUser : false; }
    public void setIsLikedByCurrentUser(Boolean isLikedByCurrentUser) { this.isLikedByCurrentUser = isLikedByCurrentUser; }

    public Boolean getIsRetweetedByCurrentUser() { return isRetweetedByCurrentUser != null ? isRetweetedByCurrentUser : false; }
    public void setIsRetweetedByCurrentUser(Boolean isRetweetedByCurrentUser) { this.isRetweetedByCurrentUser = isRetweetedByCurrentUser; }

    public Boolean getIsBookmarkedByCurrentUser() { return isBookmarkedByCurrentUser != null ? isBookmarkedByCurrentUser : false; }
    public void setIsBookmarkedByCurrentUser(Boolean isBookmarkedByCurrentUser) { this.isBookmarkedByCurrentUser = isBookmarkedByCurrentUser; }
}
