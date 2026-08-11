package com.cleancity.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Partial update for the authenticated user's profile.
 * Only non-null fields are applied. Gamification fields (points, reports) cannot be changed.
 */
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    @Pattern(regexp = "^[+0-9\\-\\s()]*$", message = "Phone contains invalid characters")
    private String phone;

    @Size(max = 1000, message = "Profile image URL must be at most 1000 characters")
    @Pattern(
            regexp = "^(https://).+|",
            message = "Profile image must be an https URL"
    )
    private String profileImage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
