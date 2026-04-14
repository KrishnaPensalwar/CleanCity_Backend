package com.cleancity.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(name = "assigned_driver_id")
    private java.util.UUID assignedDriverId;

    @Column(name = "assigned_at")
    private java.time.LocalDateTime assignedAt;

    @Column(name = "completed_by_driver_id")
    private java.util.UUID completedByDriverId;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Version
    private Long version;

    private Double confidence;

    private String labels;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(length = 1000)
    private String description;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // ensure timestamp is set (milliseconds since epoch) when not provided by client
        if (this.timestamp == null) {
            this.timestamp = System.currentTimeMillis();
        }
        // default status to PENDING if not set
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
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

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public java.util.UUID getAssignedDriverId() { return assignedDriverId; }
    public void setAssignedDriverId(java.util.UUID id) { this.assignedDriverId = id; }
    public java.time.LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(java.time.LocalDateTime t) { this.assignedAt = t; }

    public java.util.UUID getCompletedByDriverId() { return completedByDriverId; }
    public void setCompletedByDriverId(java.util.UUID id) { this.completedByDriverId = id; }
    public java.time.LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(java.time.LocalDateTime t) { this.completedAt = t; }

    public Long getVersion() { return version; }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
