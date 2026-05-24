package com.cleancity.backend.auth.dto;

public class DriverProfileDto {

    private String id;
    private String licenseNumber;
    private String vehicleNumber;
    private String approvalStatus;
    private Boolean isActive;

    public DriverProfileDto() {}

    public DriverProfileDto(String id, String licenseNumber, String vehicleNumber, String approvalStatus, Boolean isActive) {
        this.id = id;
        this.licenseNumber = licenseNumber;
        this.vehicleNumber = vehicleNumber;
        this.approvalStatus = approvalStatus;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
