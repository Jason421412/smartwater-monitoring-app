package com.smartwater.monitoring.network.dto;

/**
 * Response DTO for Bluetooth device information
 */
public class BluetoothDeviceResponse {
    private Long id;
    private String deviceName;
    private String macAddress;
    private String lastStatus;
    private String lastStatusMessage;
    private String lastConnectedAt;
    private String lastDataReceivedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    public void setLastStatusMessage(String lastStatusMessage) {
        this.lastStatusMessage = lastStatusMessage;
    }

    public String getLastConnectedAt() {
        return lastConnectedAt;
    }

    public void setLastConnectedAt(String lastConnectedAt) {
        this.lastConnectedAt = lastConnectedAt;
    }

    public String getLastDataReceivedAt() {
        return lastDataReceivedAt;
    }

    public void setLastDataReceivedAt(String lastDataReceivedAt) {
        this.lastDataReceivedAt = lastDataReceivedAt;
    }
}
