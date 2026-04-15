package com.smartwater.monitoring.network.dto;

/**
 * Request DTO for creating a pollution report
 */
public class CreateReportRequest {
    private String description;
    private String photoUrl;
    private String location;

    public CreateReportRequest() {
    }

    public CreateReportRequest(String description, String photoUrl, String location) {
        this.description = description;
        this.photoUrl = photoUrl;
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
