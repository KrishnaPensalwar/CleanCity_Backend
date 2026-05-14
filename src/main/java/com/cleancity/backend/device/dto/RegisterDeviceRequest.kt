package com.cleancity.backend.device.dto

import com.cleancity.backend.device.DevicePlatform
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RegisterDeviceRequest(
    @field:NotBlank
    val deviceId: String,

    @field:NotBlank
    val fcmToken: String,

    val deviceName: String? = null,

    @field:NotNull
    val platform: DevicePlatform
)
