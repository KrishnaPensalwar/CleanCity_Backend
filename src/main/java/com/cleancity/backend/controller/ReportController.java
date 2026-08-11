package com.cleancity.backend.controller;

import com.cleancity.backend.auth.security.AccountDetailsImpl;
import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.service.ReportService;
import com.cleancity.backend.entity.ReportStatus;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<ReportResponse> uploadReport(
            @ModelAttribute com.cleancity.backend.dto.ReportRequestDto reportRequest,
            Authentication authentication) throws java.io.IOException {

        // Always bind the report to the authenticated account — never trust client-supplied userId (IDOR).
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        String accountId = account.getAccountId().toString();

        ReportResponse response = reportService.createReport(
                reportRequest.getImage(),
                accountId,
                reportRequest.getTimestamp(),
                reportRequest.getLatitude(),
                reportRequest.getLongitude(),
                reportRequest.getDescription());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @RequestParam(value = "status", required = false) ReportStatus status) {
        return ResponseEntity.ok(reportService.getAllReports(status));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReportResponse>> getMyReports(Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(reportService.getReportsByUser(account.getAccountId().toString()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> approveReport(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        AccountDetailsImpl admin = (AccountDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(reportService.approveReport(id, admin.getAccountId()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> rejectReport(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        AccountDetailsImpl admin = (AccountDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(reportService.rejectReport(id, admin.getAccountId()));
    }
}
