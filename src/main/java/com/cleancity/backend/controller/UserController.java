package com.cleancity.backend.controller;

import com.cleancity.backend.auth.security.AccountDetailsImpl;
import com.cleancity.backend.dto.CityRankResponse;
import com.cleancity.backend.dto.UpdateProfileRequest;
import com.cleancity.backend.dto.UserDto;
import com.cleancity.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CityRankResponse> getCityRank(Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        CityRankResponse rankResponse = userService.getCityRank(account.getAccountId());
        return ResponseEntity.ok(rankResponse);
    }

    /**
     * Update the authenticated user's profile (name, address, phone, profileImage).
     * Reward points and report stats are not writable via this endpoint.
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(account.getAccountId(), request));
    }
}
