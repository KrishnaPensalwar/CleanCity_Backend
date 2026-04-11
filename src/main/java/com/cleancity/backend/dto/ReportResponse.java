package com.cleancity.backend.dto;

import com.cleancity.backend.entity.Report;
import com.cleancity.backend.entity.ReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportResponse {
    private UUID id;
    private String userId;
    private String imageUrl;
    private Long timestamp;
    private Double latitude;
    private Double longitude;
    private ReportStatus status;
    // private Double confidence;
    // private String labels;
    private String description;
    private LocalDateTime createdAt;

    public ReportResponse(Report report) {
        this.id = report.getId();
        this.userId = report.getUserId();
        this.imageUrl = report.getImageUrl();
        this.timestamp = report.getTimestamp();
        this.latitude = report.getLatitude();
        this.longitude = report.getLongitude();
        this.status = report.getStatus();
        // this.confidence = report.getConfidence();
        // this.labels = report.getLabels();
        this.description = report.getDescription();
        this.createdAt = report.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    // public Double getConfidence() {
    //     return confidence;
    // }

    // public void setConfidence(Double confidence) {
    //     this.confidence = confidence;
    // }

    // public String getLabels() {
    //     return labels;
    // }

    // public void setLabels(String labels) {
    //     this.labels = labels;
    // }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
