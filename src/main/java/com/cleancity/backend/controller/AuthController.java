package com.cleancity.backend.controller;

import com.cleancity.backend.auth.dto.*;
import com.cleancity.backend.auth.security.AccountDetailsImpl;
import com.cleancity.backend.auth.service.AuthService;
import com.cleancity.backend.dto.LoginRequest;
import com.cleancity.backend.dto.MessageResponse;
import com.cleancity.backend.dto.SignupRequest;
import com.cleancity.backend.dto.TokenRefreshRequest;
import com.cleancity.backend.dto.TokenRefreshResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** @deprecated Use {@link #registerUser(RegisterUserRequest)} or {@link #registerDriver(RegisterDriverRequest)} */
    @Deprecated
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> legacySignup(@Valid @RequestBody SignupRequest signUpRequest) {
        String role = signUpRequest.getRole();
        if (role != null && (role.equalsIgnoreCase("DRIVER") || role.equalsIgnoreCase("ROLE_DRIVER"))) {
            RegisterDriverRequest request = new RegisterDriverRequest();
            request.setName(signUpRequest.getName());
            request.setEmail(signUpRequest.getEmail());
            request.setPassword(signUpRequest.getPassword());
            return ResponseEntity.ok(authService.registerDriver(request));
        }

        RegisterUserRequest request = new RegisterUserRequest();
        request.setName(signUpRequest.getName());
        request.setEmail(signUpRequest.getEmail());
        request.setPassword(signUpRequest.getPassword());
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/register/user")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/register/driver")
    public ResponseEntity<MessageResponse> registerDriver(@Valid @RequestBody RegisterDriverRequest request) {
        return ResponseEntity.ok(authService.registerDriver(request));
    }

    @PostMapping("/convert-to-driver")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> convertToDriver(
            @Valid @RequestBody ConvertToDriverRequest request,
            Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(authService.convertToDriver(account.getAccountId(), request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.logout(request.getRefreshToken()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeResponse> getCurrentUser(Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(authService.getMe(account.getAccountId()));
    }
}
