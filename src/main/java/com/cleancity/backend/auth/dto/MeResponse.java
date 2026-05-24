package com.cleancity.backend.auth.dto;

import com.cleancity.backend.dto.UserDto;

import java.util.List;

public class MeResponse {

    private AccountDto account;
    private List<String> roles;
    private UserDto userProfile;
    private DriverProfileDto driverProfile;

    public AccountDto getAccount() {
        return account;
    }

    public void setAccount(AccountDto account) {
        this.account = account;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public UserDto getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserDto userProfile) {
        this.userProfile = userProfile;
    }

    public DriverProfileDto getDriverProfile() {
        return driverProfile;
    }

    public void setDriverProfile(DriverProfileDto driverProfile) {
        this.driverProfile = driverProfile;
    }
}
