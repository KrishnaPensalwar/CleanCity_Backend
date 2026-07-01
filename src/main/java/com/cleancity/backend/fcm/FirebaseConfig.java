package com.cleancity.backend.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account.json:}")
    private String serviceAccountJson;

    @Value("${firebase.service-account.path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() throws Exception {
        InputStream credentialsStream = openCredentialsStream();
        if (credentialsStream == null) {
            log.info("Firebase credentials not configured; push notifications will be skipped");
            return;
        }

        try (InputStream stream = credentialsStream) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            log.info("Firebase initialized successfully");
        } catch (Exception ex) {
            log.error("Failed to initialize Firebase", ex);
            throw ex;
        }
    }

    private InputStream openCredentialsStream() throws Exception {
        if (StringUtils.hasText(serviceAccountJson)) {
            log.info("Loading Firebase credentials from FIREBASE_SERVICE_ACCOUNT_JSON");
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        if (StringUtils.hasText(serviceAccountPath)) {
            log.info("Loading Firebase credentials from file path");
            return new FileInputStream(serviceAccountPath);
        }
        return null;
    }
}
