package com.smartwater.monitoring.network.dto;

public class WaterIngestRequest {
    public double ph;
    public double temperature;
    public double turbidity;
    public String location;   // e.g. "UM Lake"
    public String timestamp;  // ISO-8601 e.g. "2025-12-25T20:12:07"

    public WaterIngestRequest(double ph, double temperature, double turbidity, String location, String timestamp) {
        this.ph = ph;
        this.temperature = temperature;
        this.turbidity = turbidity;
        this.location = location;
        this.timestamp = timestamp;
    }
}
