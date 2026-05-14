package com.cleancity.backend.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct
import java.io.FileInputStream

@Configuration
class FirebaseConfig(
    @Value("${firebase.service.account.path:}")
    private val serviceAccountPath: String
) {
    private val log = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @PostConstruct
    fun init() {
        if (serviceAccountPath.isBlank()) {
            log.info("Firebase service account path not provided, skipping Firebase init")
            return
        }

        try {
            FileInputStream(serviceAccountPath).use { serviceAccount ->
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                FirebaseApp.initializeApp(options)
                log.info("Firebase initialized from $serviceAccountPath")
            }
        } catch (ex: Exception) {
            log.error("Failed to initialize Firebase", ex)
            throw ex
        }
    }
}
