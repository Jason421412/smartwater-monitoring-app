package com.smartwater.monitoring.network.dto;

/**
 * DTO for water reading request to evaluate alerts
 */
public class WaterReadingRequest {
    private Double ph;
    private Double temperature;
    private Double turbidity;
    private String location;

    public WaterReadingRequest() {}

    public WaterReadingRequest(Double ph, Double temperature, Double turbidity, String location) {
        this.ph = ph;
        this.temperature = temperature;
        this.turbidity = turbidity;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
