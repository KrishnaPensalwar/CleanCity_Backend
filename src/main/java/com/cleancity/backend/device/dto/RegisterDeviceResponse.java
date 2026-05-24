package com.cleancity.backend.device.dto;

public class RegisterDeviceResponse {
    private String message;
    private String deviceId;
    private String fcmToken;
    private String deviceName;

    public RegisterDeviceResponse() {}

    public RegisterDeviceResponse(String message, String deviceId, String fcmToken, String deviceName) {
        this.message = message;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.deviceName = deviceName;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
}
