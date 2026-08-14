package com.cleancity.backend.service;

import com.cleancity.backend.device.UserDeviceService;
import com.cleancity.backend.dto.MLValidationResult;
import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.entity.Report;
import com.cleancity.backend.entity.ReportAssignment;
import com.cleancity.backend.entity.ReportStatus;
import com.cleancity.backend.exception.ApiException;
import com.cleancity.backend.exception.ErrorCode;
import com.cleancity.backend.fcm.FcmService;
import com.cleancity.backend.repository.ReportAssignmentRepository;
import com.cleancity.backend.repository.ReportRepository;
import com.cleancity.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final MLValidationService mlValidationService;
    private final UserDeviceService userDeviceService;
    private final FcmService fcmService;

    public ReportService(
            ReportRepository reportRepository,
            ReportAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            S3StorageService s3StorageService,
            MLValidationService mlValidationService,
            UserDeviceService userDeviceService,
            FcmService fcmService
    ) {
        this.reportRepository = reportRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
        this.mlValidationService = mlValidationService;
        this.userDeviceService = userDeviceService;
        this.fcmService = fcmService;
    }

    public ReportResponse createReport(
            MultipartFile image,
            String userId,
            Long timestamp,
            Double latitude,
            Double longitude,
            String description
    ) throws IOException {

        validateInputs(image, timestamp, latitude, longitude, description);

        byte[] imageBytes = image.getBytes();
        if (!hasValidImageMagicBytes(imageBytes)) {
            throw new ApiException(ErrorCode.INVALID_IMAGE_CONTENT);
        }

        String imageUrl = s3StorageService.uploadFile(image);

        MLValidationResult mlResult =
                mlValidationService.validateImage(imageBytes);

        ReportStatus status = ReportStatus.PENDING;

        Report report = new Report();
        report.setUserId(userId);
        report.setImageUrl(imageUrl);
        report.setTimestamp(timestamp);
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setDescription(description);
        report.setStatus(status);

        Report savedReport = reportRepository.save(report);

        // increment reports filed
        try {

            UUID userUuid = UUID.fromString(userId);

            userRepository.findByAccountId(userUuid).ifPresent(user -> {

                int filed = user.getReportsFiled() != null
                        ? user.getReportsFiled()
                        : 0;

                user.setReportsFiled(filed + 1);

                userRepository.save(user);
            });

        } catch (IllegalArgumentException e) {

            userRepository.findByAccountEmail(userId).ifPresent(user -> {

                int filed = user.getReportsFiled() != null
                        ? user.getReportsFiled()
                        : 0;

                user.setReportsFiled(filed + 1);

                userRepository.save(user);
            });
        }

        return new ReportResponse(savedReport);
    }

    private void validateInputs(
            MultipartFile image,
            Long timestamp,
            Double latitude,
            Double longitude,
            String description
    ) {

        if (image == null || image.isEmpty()) {
            throw new ApiException(ErrorCode.IMAGE_REQUIRED);
        }

        if (image.getSize() > 5 * 1024 * 1024L) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = image.getContentType();

        if (contentType == null ||
                (!contentType.equalsIgnoreCase("image/jpeg")
                        && !contentType.equalsIgnoreCase("image/png"))) {
            throw new ApiException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        if (description != null && description.length() > 2000) {
            throw new ApiException(ErrorCode.DESCRIPTION_TOO_LONG);
        }

        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new ApiException(ErrorCode.INVALID_LATITUDE);
        }

        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new ApiException(ErrorCode.INVALID_LONGITUDE);
        }

        long currentTime = System.currentTimeMillis();

        if (timestamp == null ||
                timestamp > currentTime ||
                timestamp < (currentTime - 31536000000L)) {
            throw new ApiException(ErrorCode.INVALID_TIMESTAMP);
        }
    }

    private boolean hasValidImageMagicBytes(byte[] data) {
        if (data == null || data.length < 3) {
            return false;
        }
        boolean jpeg = (data[0] & 0xFF) == 0xFF
                && (data[1] & 0xFF) == 0xD8
                && (data[2] & 0xFF) == 0xFF;
        boolean png = data.length >= 8
                && (data[0] & 0xFF) == 0x89
                && data[1] == 0x50
                && data[2] == 0x4E
                && data[3] == 0x47
                && data[4] == 0x0D
                && data[5] == 0x0A
                && data[6] == 0x1A
                && data[7] == 0x0A;
        return jpeg || png;
    }

    public List<ReportResponse> getAllReports(ReportStatus status) {

        List<Report> reports;

        if (status != null) {
            reports = reportRepository.findByStatus(status);
        } else {
            reports = reportRepository.findAll();
        }

        return reports.stream()
                .map(ReportResponse::new)
                .collect(Collectors.toList());
    }

    public List<ReportResponse> getReportsByUser(String userId) {

        List<Report> reports = reportRepository.findByUserId(userId);

        return reports.stream()
                .map(ReportResponse::new)
                .collect(Collectors.toList());
    }

    public ReportResponse approveReport(UUID id, UUID adminUserId) {

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() != ReportStatus.AWAITING_REVIEW) {
            throw new ApiException(ErrorCode.REPORT_NOT_AWAITING_REVIEW);
        }

        if (report.getCompletionImageUrl() == null) {
            throw new ApiException(ErrorCode.COMPLETION_IMAGE_MISSING);
        }

        report.setStatus(ReportStatus.APPROVED);
        if (report.getCompletedAt() == null) {
            report.setCompletedAt(LocalDateTime.now());
        }

        report = reportRepository.save(report);

        ReportAssignment audit = new ReportAssignment();
        audit.setReportId(id);
        audit.setAction("APPROVED");
        audit.setActorUserId(adminUserId);
        audit.setNotes("Approved by admin");
        assignmentRepository.save(audit);

        final Report finalReport = report;

        try {

            UUID userUuid = UUID.fromString(report.getUserId());

            userRepository.findByAccountId(userUuid).ifPresent(user -> {

                int points = user.getRewardPoints() != null
                        ? user.getRewardPoints()
                        : 0;

                int resolved = user.getReportsResolved() != null
                        ? user.getReportsResolved()
                        : 0;

                user.setRewardPoints(points + 10);
                user.setReportsResolved(resolved + 1);

                userRepository.save(user);

                try {

                    userDeviceService
                            .findActiveTokensForUser(user.getAccountId().toString())
                            .forEach(token -> {

                                try {

                                    fcmService.sendNotification(
                                            token,
                                            "Complaint Approved",
                                            "Your complaint has been approved.",
                                            java.util.Map.of(
                                                    "reportId",
                                                    finalReport.getId().toString()
                                            )
                                    );

                                } catch (Exception e) {

                                    // optional invalid token handling
                                }
                            });

                } catch (Exception ex) {

                    // ignore notification errors
                }
            });

        } catch (IllegalArgumentException e) {

            userRepository.findByAccountEmail(report.getUserId()).ifPresent(user -> {

                int points = user.getRewardPoints() != null
                        ? user.getRewardPoints()
                        : 0;

                int resolved = user.getReportsResolved() != null
                        ? user.getReportsResolved()
                        : 0;

                user.setRewardPoints(points + 10);
                user.setReportsResolved(resolved + 1);

                userRepository.save(user);

                try {

                    userDeviceService
                            .findActiveTokensForUser(user.getAccountId().toString())
                            .forEach(token -> {

                                try {

                                    fcmService.sendNotification(
                                            token,
                                            "Complaint Approved",
                                            "Your complaint has been approved.",
                                            java.util.Map.of(
                                                    "reportId",
                                                    finalReport.getId().toString()
                                            )
                                    );

                                } catch (Exception ex) {

                                    // optional invalid token handling
                                }
                            });

                } catch (Exception ex) {

                    // ignore notification errors
                }
            });
        }

        return new ReportResponse(report);
    }

    public ReportResponse rejectReport(UUID id, UUID adminUserId) {

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() == ReportStatus.APPROVED) {
            throw new ApiException(ErrorCode.REPORT_ALREADY_APPROVED);
        }

        if (report.getStatus() != ReportStatus.AWAITING_REVIEW) {
            throw new ApiException(ErrorCode.REPORT_NOT_AWAITING_REVIEW);
        }

        report.setStatus(ReportStatus.REJECTED);

        report = reportRepository.save(report);

        ReportAssignment audit = new ReportAssignment();
        audit.setReportId(id);
        audit.setAction("REJECTED");
        audit.setActorUserId(adminUserId);
        audit.setNotes("Rejected by admin");
        assignmentRepository.save(audit);

        final Report finalReport = report;

        try {

            UUID userUuid = UUID.fromString(report.getUserId());

            userRepository.findByAccountId(userUuid).ifPresent(user -> {

                try {

                    userDeviceService
                            .findActiveTokensForUser(user.getAccountId().toString())
                            .forEach(token -> {

                                try {

                                    fcmService.sendNotification(
                                            token,
                                            "Complaint Rejected",
                                            "Your complaint was rejected.",
                                            java.util.Map.of(
                                                    "reportId",
                                                    finalReport.getId().toString()
                                            )
                                    );

                                } catch (Exception e) {

                                    // ignore token errors
                                }
                            });

                } catch (Exception ex) {

                    // ignore notification errors
                }
            });

        } catch (IllegalArgumentException e) {

            userRepository.findByAccountEmail(report.getUserId()).ifPresent(user -> {

                try {

                    userDeviceService
                            .findActiveTokensForUser(user.getAccountId().toString())
                            .forEach(token -> {

                                try {

                                    fcmService.sendNotification(
                                            token,
                                            "Complaint Rejected",
                                            "Your complaint was rejected.",
                                            java.util.Map.of(
                                                    "reportId",
                                                    finalReport.getId().toString()
                                            )
                                    );

                                } catch (Exception ex) {

                                    // ignore token errors
                                }
                            });

                } catch (Exception ex) {

                    // ignore notification errors
                }
            });
        }

        return new ReportResponse(report);
    }
}