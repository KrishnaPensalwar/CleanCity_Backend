package com.cleancity.backend.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;

    public AuthResponse(String accessToken, String refreshToken, UserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

    public static class UserDto {
        private String id;
        private String name;
        private String email;
    private String role;
    private Integer rewardPoints;
    private Boolean isVerified;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

        public UserDto(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public UserDto(String id, String name, String email, String role, Integer rewardPoints, Boolean isVerified, java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.rewardPoints = rewardPoints;
            this.isVerified = isVerified;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}
