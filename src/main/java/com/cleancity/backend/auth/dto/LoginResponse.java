package com.cleancity.backend.auth.dto;

import com.cleancity.backend.dto.UserDto;

import java.util.List;

public class LoginResponse {

    private String token;
    private String refreshToken;
    private List<String> roles;
    private AccountDto account;
    private UserDto profile;

    public LoginResponse() {}

    public LoginResponse(String token, String refreshToken, List<String> roles, AccountDto account, UserDto profile) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.roles = roles;
        this.account = account;
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public AccountDto getAccount() {
        return account;
    }

    public void setAccount(AccountDto account) {
        this.account = account;
    }

    public UserDto getProfile() {
        return profile;
    }

    public void setProfile(UserDto profile) {
        this.profile = profile;
    }
}
