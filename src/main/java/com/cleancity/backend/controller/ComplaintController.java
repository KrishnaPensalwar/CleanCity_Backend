package com.cleancity.backend.controller;

import com.cleancity.backend.dto.ComplaintDetailsResponse;
import com.cleancity.backend.service.ComplaintService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {
    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/{complaintId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComplaintDetailsResponse> getComplaint(@PathVariable String complaintId, Authentication authentication) {
        ComplaintDetailsResponse dto = complaintService.getComplaintDetails(complaintId, authentication);
        return ResponseEntity.ok(dto);
    }
}
