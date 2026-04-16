package com.cleancity.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    @Column(unique = true)
    private String email;
    private String phone;
    private String vehicleNumber;
    private Boolean isActive = true;

    private String zone;
    private String vehicleType;
    private String shiftTime;
    private Double rating = 0.0;
    private Integer streakDays = 0;
    private Integer totalTasks = 0;
    private Integer completionPercentage = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String v) { this.vehicleNumber = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean a) { this.isActive = a; }
    public String getZone() { return zone; }
    public void setZone(String z) { this.zone = z; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String t) { this.vehicleType = t; }
    public String getShiftTime() { return shiftTime; }
    public void setShiftTime(String s) { this.shiftTime = s; }
    public Double getRating() { return rating; }
    public void setRating(Double r) { this.rating = r; }
    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer d) { this.streakDays = d; }
    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer t) { this.totalTasks = t; }
    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer p) { this.completionPercentage = p; }
}
