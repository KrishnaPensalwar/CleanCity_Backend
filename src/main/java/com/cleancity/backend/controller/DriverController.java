package com.cleancity.backend.controller;

import com.cleancity.backend.security.services.UserDetailsImpl;
import com.cleancity.backend.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // =========================
    // 🚗 DRIVER MANAGEMENT APIs (ADMIN)
    // =========================

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActiveDrivers() {
        return ResponseEntity.ok(driverService.getActiveDrivers());
    }

    @GetMapping("/zone")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDriversByZone(@RequestParam String zone) {
        return ResponseEntity.ok(driverService.getDriversByZone(zone));
    }

    @GetMapping("/top")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTopDrivers() {
        return ResponseEntity.ok(driverService.getTopDrivers());
    }

    // =========================
    // 📍 REPORT APIs (DRIVER + ADMIN)
    // =========================

    @GetMapping("/reports/nearby")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5000") int radiusMeters,
            @RequestParam(defaultValue = "50") int limit) {

        return ResponseEntity.ok(
                driverService.findNearby(lat, lon, radiusMeters, limit)
        );
    }

    @PostMapping("/reports/{id}/assign")
@PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
public ResponseEntity<?> assign(
        @PathVariable("id") UUID id,
        @RequestBody(required = false) Map<String, String> body,
        Authentication authentication) {

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        String note = body == null ? null : body.get("note");

        try {
            return ResponseEntity.ok(
                    driverService.assignReport(id, user.getId(), note)
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/reports/assigned")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> assigned(Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(
                driverService.getAssigned(user.getId())
        );
    }
@PostMapping("/reports/{id}/completion-photo")
@PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
public ResponseEntity<?> uploadCompletionPhoto(
        @PathVariable("id") UUID id,
        @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
        Authentication authentication) {

    UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

    try {
        return ResponseEntity.ok(
                driverService.uploadCompletionPhoto(id, user.getId(), image)
        );
    } catch (SecurityException se) {
        return ResponseEntity.status(403)
                .body(Map.of("message", se.getMessage()));
    } catch (IllegalArgumentException ie) {
        return ResponseEntity.status(400)
                .body(Map.of("message", ie.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500)
                .body(Map.of("message", e.getMessage()));
    }
}

    @GetMapping("/reports/profile")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> profile(Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(
                driverService.getDriverDto(user.getEmail(), user.getId())
        );
    }

    @PostMapping("/reports/{id}/complete")
public ResponseEntity<?> complete(
        @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        String action = (String) body.getOrDefault("action", "APPROVED");
        String notes = (String) body.getOrDefault("notes", null);

        try {
            if ("APPROVED".equalsIgnoreCase(action)) {
                return ResponseEntity.status(403).body(
                        Map.of("message",
                                "Drivers cannot approve reports. Request admin approval.")
                );
            }

            return ResponseEntity.ok(
                    driverService.completeReport(id, user.getId(), action, notes)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}