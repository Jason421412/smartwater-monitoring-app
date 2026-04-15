package com.smartwater.monitoring.network.dto;

/**
 * Response DTO for user profile (Twitter-style)
 */
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
    private String createdAt;

    // Twitter-like profile fields
    private String bio;
    private String profileImageUrl;
    private String headerImageUrl;
    private Integer followerCount;
    private Integer followingCount;
    private Integer postCount;
    private String role;
    private Boolean isFollowing;
    private Boolean isExpert;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getHeaderImageUrl() { return headerImageUrl; }
    public void setHeaderImageUrl(String headerImageUrl) { this.headerImageUrl = headerImageUrl; }

    public Integer getFollowerCount() { return followerCount != null ? followerCount : 0; }
    public void setFollowerCount(Integer followerCount) { this.followerCount = followerCount; }

    public Integer getFollowingCount() { return followingCount != null ? followingCount : 0; }
    public void setFollowingCount(Integer followingCount) { this.followingCount = followingCount; }

    public Integer getPostCount() { return postCount != null ? postCount : 0; }
    public void setPostCount(Integer postCount) { this.postCount = postCount; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getIsFollowing() { return isFollowing != null ? isFollowing : false; }
    public void setIsFollowing(Boolean isFollowing) { this.isFollowing = isFollowing; }

    public Boolean getIsExpert() { return isExpert != null ? isExpert : false; }
    public void setIsExpert(Boolean isExpert) { this.isExpert = isExpert; }

    // Helper method to get full name
    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }

    // Helper method to get display name (first name or full name)
    public String getDisplayName() {
        if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        }
        return getFullName();
    }
}
