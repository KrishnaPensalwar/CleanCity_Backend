package com.cleancity.backend.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Collections;

@Service
public class FcmService {
    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    public void sendNotification(String token, String title, String body, Map<String, String> data) throws Exception {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase has not been initialized. Skipping notification.");
                return;
            }

            Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

            Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data != null ? data : Collections.emptyMap())
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build()
                )
                .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Sent FCM message: {} to token: {}...", response, token.substring(0, Math.min(token.length(), 8)));
        } catch (Exception ex) {
            log.error("Error sending FCM notification", ex);
            throw ex;
        }
    }
}
