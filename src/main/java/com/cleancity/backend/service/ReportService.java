package com.cleancity.backend.service;

import com.cleancity.backend.device.UserDeviceService;
import com.cleancity.backend.dto.MLValidationResult;
import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.entity.Report;
import com.cleancity.backend.entity.ReportStatus;
import com.cleancity.backend.fcm.FcmService;
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
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final MLValidationService mlValidationService;
    private final UserDeviceService userDeviceService;
    private final FcmService fcmService;

    public ReportService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            S3StorageService s3StorageService,
            MLValidationService mlValidationService,
            UserDeviceService userDeviceService,
            FcmService fcmService
    ) {
        this.reportRepository = reportRepository;
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

        validateInputs(image, timestamp, latitude, longitude);

        String imageUrl = s3StorageService.uploadFile(image);

        MLValidationResult mlResult =
                mlValidationService.validateImage(image.getBytes());

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

            userRepository.findById(userUuid).ifPresent(user -> {

                int filed = user.getReportsFiled() != null
                        ? user.getReportsFiled()
                        : 0;

                user.setReportsFiled(filed + 1);

                userRepository.save(user);
            });

        } catch (IllegalArgumentException e) {

            userRepository.findByEmail(userId).ifPresent(user -> {

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
            Double longitude
    ) {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "Image file is missing or empty."
            );
        }

        String contentType = image.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png"))) {

            throw new IllegalArgumentException(
                    "Only JPEG and PNG images are allowed."
            );
        }

        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude value.");
        }

        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude value.");
        }

        long currentTime = System.currentTimeMillis();

        if (timestamp == null ||
                timestamp > currentTime ||
                timestamp < (currentTime - 31536000000L)) {

            throw new IllegalArgumentException("Invalid timestamp.");
        }
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

    public ReportResponse approveReport(UUID id) {

        Report report = reportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Report not found"));

        if (report.getStatus() != ReportStatus.AWAITING_REVIEW) {
            throw new IllegalArgumentException(
                    "Report is not awaiting review"
            );
        }

        if (report.getCompletionImageUrl() == null) {
            throw new IllegalArgumentException(
                    "Completion image is missing. Cannot approve."
            );
        }

        report.setStatus(ReportStatus.APPROVED);

        report = reportRepository.save(report);

        final Report finalReport = report;

        try {

            UUID userUuid = UUID.fromString(report.getUserId());

            userRepository.findById(userUuid).ifPresent(user -> {

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
                            .findActiveTokensForUser(user.getId().toString())
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

            userRepository.findByEmail(report.getUserId()).ifPresent(user -> {

                int points = user.getRewardPoints() != null
                        ? user.getRewardPoints()
                        : 0;

                int resolved = user.getReportsResolved() != null
                        ? user.getReportsResolved()
                        : 0;

                user.setRewardPoints(points + 10);
                user.setReportsResolved(resolved + 1);

                userRepository.save(user);
            });
        }

        return new ReportResponse(report);
    }

    public ReportResponse rejectReport(UUID id) {

        Report report = reportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Report not found"));

        if (report.getStatus() == ReportStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Approved report cannot be rejected"
            );
        }

        report.setStatus(ReportStatus.REJECTED);

        report = reportRepository.save(report);

        final Report finalReport = report;

        try {

            UUID userUuid = UUID.fromString(report.getUserId());

            userRepository.findById(userUuid).ifPresent(user -> {

                int resolved = user.getReportsResolved() != null
                        ? user.getReportsResolved()
                        : 0;

                user.setReportsResolved(resolved + 1);

                userRepository.save(user);

                try {

                    userDeviceService
                            .findActiveTokensForUser(user.getId().toString())
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

            userRepository.findByEmail(report.getUserId()).ifPresent(user -> {

                int resolved = user.getReportsResolved() != null
                        ? user.getReportsResolved()
                        : 0;

                user.setReportsResolved(resolved + 1);

                userRepository.save(user);
            });
        }

        return new ReportResponse(report);
    }
}