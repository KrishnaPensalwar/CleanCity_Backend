package com.cleancity.backend.device.dto;

public class RegisterDeviceResponse {
    private String deviceId;
    private String fcmToken;
    private String deviceName;

    public RegisterDeviceResponse() {}

    public RegisterDeviceResponse(String deviceId, String fcmToken, String deviceName) {
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.deviceName = deviceName;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
}
