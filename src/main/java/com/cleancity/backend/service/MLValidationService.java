package com.cleancity.backend.service;

import com.cleancity.backend.dto.MLValidationResult;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MLValidationService {

    private static final Logger log = LoggerFactory.getLogger(MLValidationService.class);

    private static final List<String> TARGET_KEYWORDS = Arrays.asList(
            "garbage", "trash", "waste", "litter", "debris", "pothole",
            "asphalt", "road", "street", "damage", "crack", "rubble", "mud");

    @Value("${google.application.credentials.json:}")
    private String googleCredentialsJson;

    @Value("${firebase.service-account.json:}")
    private String firebaseCredentialsJson;

    public MLValidationResult validateImage(byte[] imageBytes) {
        List<String> detectedLabels = new ArrayList<>();
        double highestConfidence = 0.0;

        ImageAnnotatorSettings settings = buildVisionSettings();

        try (ImageAnnotatorClient vision = settings != null
                ? ImageAnnotatorClient.create(settings)
                : ImageAnnotatorClient.create()) {

            ByteString imgBytes = ByteString.copyFrom(imageBytes);
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));

            for (AnnotateImageResponse res : response.getResponsesList()) {
                if (res.hasError()) {
                    log.warn("Google Vision API error: {}", res.getError().getMessage());
                    return new MLValidationResult(0.0, detectedLabels);
                }

                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    String label = annotation.getDescription().toLowerCase();
                    detectedLabels.add(label);

                    for (String keyword : TARGET_KEYWORDS) {
                        if (label.contains(keyword)) {
                            double score = annotation.getScore();
                            if (score > highestConfidence) {
                                highestConfidence = score;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Google Cloud Vision validation failed", e);
        }

        return new MLValidationResult(Math.round(highestConfidence * 100.0) / 100.0, detectedLabels);
    }

    private ImageAnnotatorSettings buildVisionSettings() {
        try {
            GoogleCredentials credentials = loadGoogleCredentials();
            if (credentials == null) {
                log.debug("No Google credentials configured; using application default credentials");
                return null;
            }
            return ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to load Google credentials for Vision API: {}", e.getMessage());
            return null;
        }
    }

    private GoogleCredentials loadGoogleCredentials() throws Exception {
        String json = StringUtils.hasText(googleCredentialsJson)
                ? googleCredentialsJson
                : firebaseCredentialsJson;

        if (StringUtils.hasText(json)) {
            try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (StringUtils.hasText(credentialsPath)) {
            try (InputStream stream = new FileInputStream(credentialsPath)) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        return null;
    }
}
