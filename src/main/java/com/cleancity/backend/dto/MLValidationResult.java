package com.cleancity.backend.dto;

import java.util.List;

public class MLValidationResult {
    private Double confidence;
    private List<String> labels;

    public MLValidationResult() {}

    public MLValidationResult(Double confidence, List<String> labels) {
        this.confidence = confidence;
        this.labels = labels;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
