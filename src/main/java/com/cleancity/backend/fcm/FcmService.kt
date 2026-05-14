package com.cleancity.backend.fcm

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FcmService {
    private val log = LoggerFactory.getLogger(FcmService::class.java)

    fun sendNotification(token: String, title: String, body: String, data: Map<String, String> = emptyMap()) {
        try {
            val notification = Notification.builder().setTitle(title).setBody(body).build()

            val message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build()
                )
                .build()

            val response = FirebaseMessaging.getInstance().send(message)
            log.info("Sent FCM message: $response to token: ${token.take(8)}...")
        } catch (ex: Exception) {
            log.error("Error sending FCM notification", ex)
            throw ex
        }
    }
}
