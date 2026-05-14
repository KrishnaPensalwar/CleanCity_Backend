package com.cleancity.backend.device

import org.springframework.stereotype.Component

@Component
class UserDeviceRepositoryExtensions(private val repo: UserDeviceRepository) {
    fun removeByUserIdAndToken(userId: String, token: String) {
        repo.findAllByUserIdAndIsActiveTrue(userId).forEach {
            if (it.fcmToken == token) {
                it.isActive = false
                it.updatedAt = java.time.LocalDateTime.now()
                repo.save(it)
            }
        }
    }
}
