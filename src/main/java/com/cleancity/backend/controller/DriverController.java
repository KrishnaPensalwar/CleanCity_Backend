package com.cleancity.backend.controller;

import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.security.services.UserDetailsImpl;
import com.cleancity.backend.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/driver/reports")
public class DriverController {
    private final DriverService driverService;

    public DriverController(DriverService driverService) { this.driverService = driverService; }

    @GetMapping("/nearby")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> nearby(
            @RequestParam(name = "lat") double lat,
            @RequestParam(name = "lon") double lon,
            @RequestParam(name = "radiusMeters", required = false, defaultValue = "5000") int radiusMeters,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        return ResponseEntity.ok(driverService.findNearby(lat, lon, radiusMeters, limit));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> assign(@PathVariable("id") UUID id, @RequestBody(required = false) java.util.Map<String,String> body, Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        String note = body == null ? null : body.get("note");
        try {
            return ResponseEntity.ok(driverService.assignReport(id, user.getId(), note));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> assigned(Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(driverService.getAssigned(user.getId()));
    }

    @PostMapping("/{id}/completion-photo")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadCompletionPhoto(@PathVariable("id") java.util.UUID id, @RequestParam("image") org.springframework.web.multipart.MultipartFile image, Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        try {
            return ResponseEntity.ok(driverService.uploadCompletionPhoto(id, user.getId(), image));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", se.getMessage()));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(400).body(java.util.Map.of("message", ie.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> profile(Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        com.cleancity.backend.dto.DriverDto d = driverService.getDriverDto(user.getEmail(), user.getId());
        return ResponseEntity.ok(d);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> complete(@PathVariable("id") UUID id, @RequestBody java.util.Map<String,Object> body, Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        String action = (String) body.getOrDefault("action", "APPROVED");
        String notes = (String) body.getOrDefault("notes", null);
        try {
            return ResponseEntity.ok(driverService.completeReport(id, user.getId(), action, notes));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
