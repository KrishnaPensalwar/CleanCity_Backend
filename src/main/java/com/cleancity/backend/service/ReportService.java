package com.cleancity.backend.service;

import com.cleancity.backend.dto.MLValidationResult;
import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.entity.Report;
import com.cleancity.backend.entity.ReportStatus;
import com.cleancity.backend.entity.User;
import com.cleancity.backend.repository.ReportRepository;
import com.cleancity.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final MLValidationService mlValidationService;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository,
            S3StorageService s3StorageService,
            MLValidationService mlValidationService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
        this.mlValidationService = mlValidationService;
    }

    public ReportResponse createReport(MultipartFile image, String userId, Long timestamp, Double latitude,
            Double longitude, String description) throws IOException {
        validateInputs(image, timestamp, latitude, longitude);

        String imageUrl = s3StorageService.uploadFile(image);
        MLValidationResult mlResult = mlValidationService.validateImage(image.getBytes());

        ReportStatus status;

        // Will implement in future .
        /*
         * 
         * if (mlResult.getConfidence() > 0.8) {
         * status = ReportStatus.APPROVED;
         * } else if (mlResult.getConfidence() >= 0.5) {
         * status = ReportStatus.PENDING;
         * } else {
         * status = ReportStatus.REJECTED;
         * }
         */
        status = ReportStatus.PENDING;

        Report report = new Report();
        report.setUserId(userId);
        report.setImageUrl(imageUrl);
        report.setTimestamp(timestamp);
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setDescription(description);
        report.setStatus(status);
        // report.setConfidence(mlResult.getConfidence());
        // report.setLabels(String.join(",", mlResult.getLabels()));

        Report savedReport = reportRepository.save(report);

        // ✅ Increment reportsFiled for user
        try {
            UUID userUuid = UUID.fromString(userId);
            userRepository.findById(userUuid).ifPresent(user -> {
                user.setReportsFiled(user.getReportsFiled() + 1);
                userRepository.save(user);
            });
        } catch (IllegalArgumentException e) {
            userRepository.findByEmail(userId).ifPresent(user -> {
                user.setReportsFiled(user.getReportsFiled() + 1);
                userRepository.save(user);
            });
        }

        return new ReportResponse(savedReport);
    }

    private void validateInputs(MultipartFile image, Long timestamp, Double latitude, Double longitude) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is missing or empty.");
        }

        String contentType = image.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed.");
        }

        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude value.");
        }

        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude value.");
        }

        long currentTime = System.currentTimeMillis();
        if (timestamp == null || timestamp > currentTime || timestamp < (currentTime - 31536000000L)) { // Not in the
                                                                                                        // future, not
                                                                                                        // older than 1
                                                                                                        // year
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
        return reports.stream().map(ReportResponse::new).collect(Collectors.toList());
    }

    public List<ReportResponse> getReportsByUser(String userId) {
        List<Report> reports = reportRepository.findByUserId(userId);
        return reports.stream().map(ReportResponse::new).collect(Collectors.toList());
    }

    public ReportResponse approveReport(@PathVariable("id") UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // ✅ Only allow approval when awaiting admin review
        if (report.getStatus() != ReportStatus.AWAITING_REVIEW) {
            throw new IllegalArgumentException("Report is not awaiting review");
        }

        // ✅ Ensure driver has uploaded completion proof
        if (report.getCompletionImageUrl() == null) {
            throw new IllegalArgumentException("Completion image is missing. Cannot approve.");
        }

        // ==============================
        // 🚧 FUTURE: IMAGE VALIDATION
        // ==============================
        /*
         * MLValidationResult result = mlValidationService.compareImages(
         * report.getImageUrl(),
         * report.getCompletionImageUrl()
         * );
         * 
         * if (result.getConfidence() < 0.6) {
         * throw new IllegalArgumentException("Images do not match. Cannot approve.");
         * }
         */

        // ✅ Manual approval
        report.setStatus(ReportStatus.APPROVED);
        report = reportRepository.save(report);

        // ✅ Reward user and increment reportsResolved
        try {
            UUID userUuid = UUID.fromString(report.getUserId());
            userRepository.findById(userUuid).ifPresent(user -> {
                user.setRewardPoints(user.getRewardPoints() + 10);
                user.setReportsResolved(user.getReportsResolved() + 1);
                userRepository.save(user);
            });
        } catch (IllegalArgumentException e) {
            userRepository.findByEmail(report.getUserId()).ifPresent(user -> {
                user.setRewardPoints(user.getRewardPoints() + 10);
                user.setReportsResolved(user.getReportsResolved() + 1);
                userRepository.save(user);
            });
        }

        return new ReportResponse(report);
    }

    public ReportResponse rejectReport(@PathVariable("id") UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        if (report.getStatus() == ReportStatus.APPROVED) {
            throw new IllegalArgumentException("Approved report cannot be rejected");
        }

        report.setStatus(ReportStatus.REJECTED);
        report = reportRepository.save(report);

        // ✅ Increment reportsResolved for user
        try {
            UUID userUuid = UUID.fromString(report.getUserId());
            userRepository.findById(userUuid).ifPresent(user -> {
                user.setReportsResolved(user.getReportsResolved() + 1);
                userRepository.save(user);
            });
        } catch (IllegalArgumentException e) {
            userRepository.findByEmail(report.getUserId()).ifPresent(user -> {
                user.setReportsResolved(user.getReportsResolved() + 1);
                userRepository.save(user);
            });
        }

        return new ReportResponse(report);
    }
}
