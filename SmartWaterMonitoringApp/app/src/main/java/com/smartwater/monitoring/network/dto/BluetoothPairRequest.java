package com.smartwater.monitoring.network.dto;

/**
 * Request DTO for pairing a Bluetooth device
 */
public class BluetoothPairRequest {
    private String deviceName;
    private String macAddress;

    public BluetoothPairRequest(String deviceName, String macAddress) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
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
}
