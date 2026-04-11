package com.cleancity.backend.service;

import com.cleancity.backend.dto.MLValidationResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class MLValidationService {

    private final Random random = new Random();

    /**
     * Mocks calling an ML endpoint to validate the uploaded image.
     */
    public MLValidationResult validateImage(String imageUrl) {
        // Random confidence score between 0.2 and 1.0
        double confidence = 0.2 + (0.8 * random.nextDouble());
        
        // Mock labels
        List<String> mockLabels;
        if (confidence > 0.8) {
            mockLabels = Arrays.asList("pothole", "severe", "road_damage");
        } else if (confidence > 0.5) {
            mockLabels = Arrays.asList("crack", "minor_damage");
        } else {
            mockLabels = Arrays.asList("unclear", "no_damage");
        }

        return new MLValidationResult(Math.round(confidence * 100.0) / 100.0, mockLabels);
    }
}
