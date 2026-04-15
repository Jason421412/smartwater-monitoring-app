package com.smartwater.monitoring.network.dto;

/**
 * Request DTO for creating a community post
 */
public class CommunityPostRequest {
    private String content;
    private String photoUrl;
    private String location;
    private Double ph;
    private Double temperature;
    private Double turbidity;
    private String type; // "report", "update", etc.

    public CommunityPostRequest() {
    }

    public CommunityPostRequest(String content, String location) {
        this.content = content;
        this.location = location;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getPh() {
        return ph;
    }

    public void setPh(Double ph) {
        this.ph = ph;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTurbidity() {
        return turbidity;
    }

    public void setTurbidity(Double turbidity) {
        this.turbidity = turbidity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
