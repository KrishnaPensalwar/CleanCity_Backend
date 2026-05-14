package com.cleancity.backend.device.dto

data class RegisterDeviceResponse(
    val deviceId: String,
    val fcmToken: String,
    val deviceName: String?
)
