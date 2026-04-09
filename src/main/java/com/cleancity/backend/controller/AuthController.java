package com.cleancity.backend.controller;

import com.cleancity.backend.dto.*;
import com.cleancity.backend.entity.RefreshToken;
import com.cleancity.backend.entity.User;
import com.cleancity.backend.repository.UserRepository;
import com.cleancity.backend.security.jwt.JwtUtils;
import com.cleancity.backend.security.services.RefreshTokenService;
import com.cleancity.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getName(),
                             signUpRequest.getEmail(),
                             encoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User created successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        // Delete existing token if any (optional based on your session management, keeping it clean)
        refreshTokenService.deleteByUserId(userDetails.getId());
        
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                refreshToken.getToken(),
                new AuthResponse.UserDto(
                        userDetails.getId().toString(),
                        userDetails.getName(),
                        userDetails.getEmail()
                )
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        Optional<RefreshToken> tokenOpt = refreshTokenService.findByToken(requestRefreshToken);
        if (tokenOpt.isPresent()) {
            RefreshToken token = refreshTokenService.verifyExpiration(tokenOpt.get());
            User user = token.getUser();
            String jwtToken = jwtUtils.generateTokenFromEmail(user.getEmail());
            return ResponseEntity.ok(new TokenRefreshResponse(jwtToken));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Refresh token is not in database!"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody TokenRefreshRequest request) {
        Optional<RefreshToken> token = refreshTokenService.findByToken(request.getRefreshToken());
        if (token.isPresent()) {
            refreshTokenService.deleteByUserId(token.get().getUser().getId());
            return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Refresh token not found"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if(authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new AuthResponse.UserDto(
                userDetails.getId().toString(),
                userDetails.getName(),
                userDetails.getEmail()
        ));
    }
}
