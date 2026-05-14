package com.cleancity.backend.device

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_devices", indexes = [Index(name = "idx_user_device_userid", columnList = "user_id")])
data class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "device_id", nullable = false)
    val deviceId: String,

    @Column(name = "fcm_token", nullable = false)
    var fcmToken: String,

    @Column(name = "device_name")
    var deviceName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    val platform: DevicePlatform,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
