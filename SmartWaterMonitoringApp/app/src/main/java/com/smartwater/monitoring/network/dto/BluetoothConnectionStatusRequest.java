package com.smartwater.monitoring.network.dto;

/**
 * Request DTO for updating Bluetooth connection status
 */
public class BluetoothConnectionStatusRequest {
    private String macAddress;
    private String status; // "CONNECTED", "DISCONNECTED", "FAILED"
    private String message;

    public BluetoothConnectionStatusRequest(String macAddress, String status, String message) {
        this.macAddress = macAddress;
        this.status = status;
        this.message = message;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
