package com.cleancity.backend.auth.dto;

public class AccountDto {

    private String id;
    private String email;
    private String phone;
    private String status;

    public AccountDto() {}

    public AccountDto(String id, String email, String phone, String status) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
