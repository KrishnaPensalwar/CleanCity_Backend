package com.cleancity.backend.device.dto;

import com.cleancity.backend.device.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterDeviceRequest {
    @NotBlank
    private String deviceId;

    @NotBlank
    private String fcmToken;

    private String deviceName;

    @NotNull
    private DevicePlatform platform;

    public RegisterDeviceRequest() {}

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public DevicePlatform getPlatform() { return platform; }
    public void setPlatform(DevicePlatform platform) { this.platform = platform; }
}
