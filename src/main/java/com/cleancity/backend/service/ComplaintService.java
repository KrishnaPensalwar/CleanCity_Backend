package com.cleancity.backend.service;

import com.cleancity.backend.dto.ComplaintDetailsResponse;
import com.cleancity.backend.entity.Report;
import com.cleancity.backend.repository.ReportRepository;
import com.cleancity.backend.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ComplaintService {
    private final ReportRepository reportRepository;

    public ComplaintService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ComplaintDetailsResponse getComplaintDetails(String complaintIdStr, Authentication auth) {
        UUID complaintId;
        try {
            complaintId = UUID.fromString(complaintIdStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid complaint id");
        }

        Report report = reportRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

        // Only allow if owner or admin
        if (!isAdmin && !user.getId().toString().equals(report.getUserId())) {
            throw new SecurityException("Not authorized to access this complaint");
        }

        ComplaintDetailsResponse dto = new ComplaintDetailsResponse();
        dto.setComplaintId(report.getId());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setImageUrl(report.getImageUrl());
        dto.setCompletionImageUrl(report.getCompletionImageUrl());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getCompletedAt());
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        dto.setAssignedWorker(report.getAssignedDriver() == null ? null : report.getAssignedDriver().getId().toString());

        // fields not present in entity left null (title, category, location, adminRemarks, resolutionDetails)

        return dto;
    }
}
