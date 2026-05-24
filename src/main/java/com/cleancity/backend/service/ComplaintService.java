package com.cleancity.backend.service;

import com.cleancity.backend.auth.security.AccountDetailsImpl;
import com.cleancity.backend.dto.ComplaintDetailsResponse;
import com.cleancity.backend.entity.Report;
import com.cleancity.backend.exception.ApiException;
import com.cleancity.backend.exception.ErrorCode;
import com.cleancity.backend.repository.ReportRepository;
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
            throw new ApiException(ErrorCode.INVALID_COMPLAINT_ID);
        }

        Report report = reportRepository.findById(complaintId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMPLAINT_NOT_FOUND));

        AccountDetailsImpl account = (AccountDetailsImpl) auth.getPrincipal();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

        if (!isAdmin && !account.getAccountId().toString().equals(report.getUserId())) {
            throw new ApiException(ErrorCode.NOT_AUTHORIZED);
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

        return dto;
    }
}
