package com.cleancity.backend.dto;

public class DriverDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String zone;
    private Integer totalTasks;
    private Integer completionPercentage;
    private Double rating;
    private Integer streakDays;
    private String vehicleRegNumber;
    private String vehicleType;
    private String shiftTime;
    private Boolean isActive;

    public DriverDto() {}

    public DriverDto(String id, String name, String email, String role, String zone, Integer totalTasks, Integer completionPercentage, Double rating, Integer streakDays, String vehicleRegNumber, String vehicleType, String shiftTime, Boolean isActive) {
        this.id = id; this.name = name; this.email = email; this.role = role; this.zone = zone; this.totalTasks = totalTasks; this.completionPercentage = completionPercentage; this.rating = rating; this.streakDays = streakDays; this.vehicleRegNumber = vehicleRegNumber; this.vehicleType = vehicleType; this.shiftTime = shiftTime; this.isActive = isActive;
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer totalTasks) { this.totalTasks = totalTasks; }
    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
    public String getVehicleRegNumber() { return vehicleRegNumber; }
    public void setVehicleRegNumber(String vehicleRegNumber) { this.vehicleRegNumber = vehicleRegNumber; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getShiftTime() { return shiftTime; }
    public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
