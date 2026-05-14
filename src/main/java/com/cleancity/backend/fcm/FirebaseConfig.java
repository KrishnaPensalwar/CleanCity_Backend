package com.cleancity.backend.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service.account.path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() throws Exception {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.info("Firebase service account path not provided, skipping Firebase init");
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            log.info("Firebase initialized from {}", serviceAccountPath);
        } catch (Exception ex) {
            log.error("Failed to initialize Firebase", ex);
            throw ex;
        }
    }
}
