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
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MLValidationService {

    // Keywords that indicate a "valid" clean city report target
    private static final List<String> TARGET_KEYWORDS = Arrays.asList(
            "garbage", "trash", "waste", "litter", "debris", "pothole",
            "asphalt", "road", "street", "damage", "crack", "rubble", "mud");

    public MLValidationResult validateImage(byte[] imageBytes) {
        List<String> detectedLabels = new ArrayList<>();
        double highestConfidence = 0.0;

        System.out.println("\n============ AI VALIDATION START ============");
        ImageAnnotatorSettings settings = null;
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            String keyPath = dotenv.get("GOOGLE_APPLICATION_CREDENTIALS");
            System.out.println("[AI-AUTH] GOOGLE_APPLICATION_CREDENTIALS path found: " + keyPath);
            
            if (keyPath != null && !keyPath.isEmpty()) {
                try (FileInputStream fis = new FileInputStream(keyPath)) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(fis);
                    settings = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(() -> credentials).build();
                    System.out.println("[AI-AUTH] JSON Credentials successfully loaded into ImageAnnotatorSettings.");
                }
            } else {
                System.out.println("[AI-AUTH] No path specified in .env! Attempting to connect implicitly...");
            }
        } catch (Exception e) {
            System.err.println("[AI-AUTH-ERROR] Failed to explicitly load Google Credentials: " + e.getMessage());
        }

        try (ImageAnnotatorClient vision = (settings != null) ? ImageAnnotatorClient.create(settings) : ImageAnnotatorClient.create()) {
            System.out.println("[AI-VISION] Client Successfully Opened. Sending image...");
            ByteString imgBytes = ByteString.copyFrom(imageBytes);

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.err.println("[AI-VISION-ERROR] API responded with error: " + res.getError().getMessage());
                    return new MLValidationResult(0.0, detectedLabels);
                }

                System.out.println("[AI-VISION] SUCCESS! Labels detected: " + res.getLabelAnnotationsCount());
                
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    String label = annotation.getDescription().toLowerCase();
                    detectedLabels.add(label);
                    System.out.println("  --> Detected Label: '" + label + "' (Score: " + annotation.getScore() + ")");

                    // Check if it matches our targets
                    for (String keyword : TARGET_KEYWORDS) {
                        if (label.contains(keyword)) {
                            System.out.println("      🎯 MATCH FOUND: '" + keyword + "' inside '" + label + "'!");
                            double score = annotation.getScore();
                            if (score > highestConfidence) {
                                highestConfidence = score;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AI-CRASH] Exception calling Google Cloud Vision: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[AI-RESULT] Final Highest Confidence Score matching our criteria: " + highestConfidence);
        System.out.println("============ AI VALIDATION END ============\n");
        return new MLValidationResult(Math.round(highestConfidence * 100.0) / 100.0, detectedLabels);
    }
}
