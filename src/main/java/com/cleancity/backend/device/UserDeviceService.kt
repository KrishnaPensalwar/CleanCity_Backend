package com.cleancity.backend.device

import com.cleancity.backend.device.dto.RegisterDeviceRequest
import com.cleancity.backend.device.dto.RegisterDeviceResponse
import com.cleancity.backend.security.services.UserDetailsImpl
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserDeviceService(
    private val repo: UserDeviceRepository
) {
    private val log = LoggerFactory.getLogger(UserDeviceService::class.java)

    @Transactional
    fun registerDevice(req: RegisterDeviceRequest, auth: Authentication): RegisterDeviceResponse {
        val principal = auth.principal as UserDetailsImpl
        val userId = principal.id.toString()

        val existing = repo.findByUserIdAndDeviceId(userId, req.deviceId)
        val now = LocalDateTime.now()
        return if (existing != null) {
            existing.fcmToken = req.fcmToken
            existing.deviceName = req.deviceName
            existing.isActive = true
            existing.updatedAt = now
            repo.save(existing)
            RegisterDeviceResponse(existing.deviceId, existing.fcmToken, existing.deviceName)
        } else {
            val entity = UserDevice(
                userId = userId,
                deviceId = req.deviceId,
                fcmToken = req.fcmToken,
                deviceName = req.deviceName,
                platform = req.platform
            )
            repo.save(entity)
            RegisterDeviceResponse(entity.deviceId, entity.fcmToken, entity.deviceName)
        }
    }

    @Transactional
    fun deleteDevice(deviceId: String, auth: Authentication) {
        val principal = auth.principal as UserDetailsImpl
        val userId = principal.id.toString()
        repo.findByUserIdAndDeviceId(userId, deviceId)?.let {
            it.isActive = false
            it.updatedAt = LocalDateTime.now()
            repo.save(it)
        }
    }

    fun findActiveTokensForUser(userId: String): List<String> {
        return repo.findAllByUserIdAndIsActiveTrue(userId).map { it.fcmToken }
    }
}
