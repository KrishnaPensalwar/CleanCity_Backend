package com.cleancity.backend.dto;

import java.time.LocalDateTime;

public class UserDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private Integer rewardPoints;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer reportsFiled;
    private Integer reportsResolved;

    public UserDto() {}

    public UserDto(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UserDto(String id, String name, String email, String role, Integer rewardPoints, Boolean isVerified,
                   LocalDateTime createdAt, LocalDateTime updatedAt, Integer reportsFiled, Integer reportsResolved) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.rewardPoints = rewardPoints;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.reportsFiled = reportsFiled;
        this.reportsResolved = reportsResolved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getReportsFiled() {
        return reportsFiled;
    }

    public void setReportsFiled(Integer reportsFiled) {
        this.reportsFiled = reportsFiled;
    }

    public Integer getReportsResolved() {
        return reportsResolved;
    }

    public void setReportsResolved(Integer reportsResolved) {
        this.reportsResolved = reportsResolved;
    }
}
