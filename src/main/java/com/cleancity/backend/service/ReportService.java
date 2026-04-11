package com.cleancity.backend.service;

import com.cleancity.backend.dto.MLValidationResult;
import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.entity.Report;
import com.cleancity.backend.entity.ReportStatus;
import com.cleancity.backend.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final S3StorageService s3StorageService;
    private final MLValidationService mlValidationService;

    public ReportService(ReportRepository reportRepository, S3StorageService s3StorageService, MLValidationService mlValidationService) {
        this.reportRepository = reportRepository;
        this.s3StorageService = s3StorageService;
        this.mlValidationService = mlValidationService;
    }

    public ReportResponse createReport(MultipartFile image, String userId, Long timestamp, Double latitude, Double longitude, String description) throws IOException {
        validateInputs(image, timestamp, latitude, longitude);

        String imageUrl = s3StorageService.uploadFile(image);
        MLValidationResult mlResult = mlValidationService.validateImage(imageUrl);

        ReportStatus status;
        if (mlResult.getConfidence() > 0.8) {
            status = ReportStatus.APPROVED;
        } else if (mlResult.getConfidence() >= 0.5) {
            status = ReportStatus.PENDING;
        } else {
            status = ReportStatus.REJECTED;
        }

        Report report = new Report();
        report.setUserId(userId);
        report.setImageUrl(imageUrl);
        report.setTimestamp(timestamp);
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setDescription(description);
        report.setStatus(status);
        report.setConfidence(mlResult.getConfidence());
        report.setLabels(String.join(",", mlResult.getLabels()));
        
        Report savedReport = reportRepository.save(report);

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
        if (timestamp == null || timestamp > currentTime || timestamp < (currentTime - 31536000000L)) { // Not in the future, not older than 1 year
            throw new IllegalArgumentException("Invalid timestamp.");
        }
    }
}
