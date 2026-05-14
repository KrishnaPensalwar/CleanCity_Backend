package com.cleancity.backend.device

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, Long> {
    fun findByUserIdAndDeviceId(userId: String, deviceId: String): UserDevice?
    fun findAllByUserIdAndIsActiveTrue(userId: String): List<UserDevice>
    fun deleteByUserIdAndDeviceId(userId: String, deviceId: String)
}
