package com.cleancity.backend.controller;

import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.service.ReportService;
import com.cleancity.backend.entity.ReportStatus;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadReport(@ModelAttribute com.cleancity.backend.dto.ReportRequestDto reportRequest) {
        try {
            // Verify JWT
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Valid JWT is required");
            }

            ReportResponse response = reportService.createReport(
                    reportRequest.getImage(),
                    reportRequest.getUserId(),
                    reportRequest.getTimestamp(),
                    reportRequest.getLatitude(),
                    reportRequest.getLongitude(),
                    reportRequest.getDescription());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing report: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReports(@RequestParam(value = "status", required = false) ReportStatus status) {
        try {
            List<ReportResponse> reports = reportService.getAllReports(status);
            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching reports: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyReports(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof com.cleancity.backend.security.services.UserDetailsImpl)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        com.cleancity.backend.security.services.UserDetailsImpl user = (com.cleancity.backend.security.services.UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(reportService.getReportsByUser(user.getId().toString()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveReport(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(reportService.approveReport(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error approving report: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectReport(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(reportService.rejectReport(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error rejecting report: " + e.getMessage());
        }
    }
}
