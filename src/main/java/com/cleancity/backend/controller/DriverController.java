package com.cleancity.backend.controller;

import com.cleancity.backend.auth.security.AccountDetailsImpl;
import com.cleancity.backend.exception.ApiException;
import com.cleancity.backend.exception.ErrorCode;
import com.cleancity.backend.service.DriverService;
import com.cleancity.backend.dto.AssignDriverRequest;
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
            @PathVariable("id") UUID reportId,
            @RequestBody(required = false) AssignDriverRequest request,
            Authentication authentication) {

        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        UUID driverId;

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

        if (isAdmin) {
            if (request == null || request.getDriverId() == null) {
                throw new ApiException(ErrorCode.DRIVER_ID_REQUIRED);
            }
            driverId = request.getDriverId();
        } else {
            driverId = driverService.requireDriverProfileId(account.getAccountId());
        }

        return ResponseEntity.ok(driverService.assignReport(reportId, driverId));
    }

    @GetMapping("/reports/assigned")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> assigned(Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        UUID driverId = driverService.requireDriverProfileId(account.getAccountId());
        return ResponseEntity.ok(driverService.getAssigned(driverId));
    }

    @PostMapping("/reports/{id}/completion-photo")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadCompletionPhoto(
            @PathVariable("id") String idStr,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
            Authentication authentication) throws java.io.IOException {

        UUID id;
        try {
            id = UUID.fromString(idStr);
        } catch (IllegalArgumentException iae) {
            throw new ApiException(ErrorCode.INVALID_REPORT_ID);
        }

        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        UUID driverId = driverService.requireDriverProfileId(account.getAccountId());
        return ResponseEntity.ok(driverService.uploadCompletionPhoto(id, driverId, image));
    }

    @GetMapping("/reports/profile")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> profile(Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(driverService.getDriverDto(account.getAccountId()));
    }

    @PostMapping("/reports/{id}/complete")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<?> complete(
            @PathVariable("id") String idStr,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        UUID id;
        try {
            id = UUID.fromString(idStr);
        } catch (IllegalArgumentException iae) {
            throw new ApiException(ErrorCode.INVALID_REPORT_ID);
        }

        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        UUID driverId = driverService.requireDriverProfileId(account.getAccountId());

        String action = (String) body.getOrDefault("action", "APPROVED");
        String notes = (String) body.getOrDefault("notes", null);

        if ("APPROVED".equalsIgnoreCase(action)) {
            throw new ApiException(ErrorCode.DRIVER_CANNOT_APPROVE);
        }

        return ResponseEntity.ok(
                driverService.completeReport(id, driverId, action, notes)
        );
    }
}
