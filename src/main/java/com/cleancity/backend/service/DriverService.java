package com.cleancity.backend.service;

import com.cleancity.backend.dto.ReportResponse;
import com.cleancity.backend.entity.Driver;
import com.cleancity.backend.entity.Report;
import com.cleancity.backend.repository.ReportAssignmentRepository;
import com.cleancity.backend.repository.ReportRepository;
import com.cleancity.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DriverService {
    private final ReportRepository reportRepository;
    private final ReportAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final com.cleancity.backend.repository.DriverRepository driverRepository;
    private final com.cleancity.backend.service.S3StorageService s3StorageService;

    public DriverService(ReportRepository reportRepository, ReportAssignmentRepository assignmentRepository,
            UserRepository userRepository, com.cleancity.backend.repository.DriverRepository driverRepository,
            com.cleancity.backend.service.S3StorageService s3StorageService) {
        this.reportRepository = reportRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.s3StorageService = s3StorageService;
    }

    public List<ReportResponse> findNearby(double lat, double lon, int radiusMeters, int limit) {
        // compute bounding box
        double latDelta = radiusMeters / 111320.0;
        double lonDelta = radiusMeters / (111320.0 * Math.cos(Math.toRadians(lat)));
        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLon = lon - lonDelta;
        double maxLon = lon + lonDelta;

        List<Object[]> rows = reportRepository.findNearbyPending(lat, lon, minLat, maxLat, minLon, maxLon, limit);
        List<ReportResponse> out = new ArrayList<>();
        for (Object[] r : rows) {
            // r: id, latitude, longitude, distance_m
            UUID id = (UUID) r[0];
            Double latitude = ((Number) r[1]).doubleValue();
            Double longitude = ((Number) r[2]).doubleValue();
            Double distance = ((Number) r[3]).doubleValue();
            Report rep = reportRepository.findById(id).orElse(null);
            if (rep == null)
                continue;
            ReportResponse rr = new ReportResponse(rep);
            // add distance via reflection? ReportResponse doesn't have distance; client can
            // compute. skipping.
            out.add(rr);
        }
        return out;
    }

    @Transactional
    public ReportResponse assignReport(UUID reportId, UUID driverId, String note) {
        int updated = reportRepository.assignIfPending(reportId, driverId);
        if (updated == 0)
            throw new IllegalStateException("Report already assigned or not available");
        Report r = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        // create assignment audit
        com.cleancity.backend.entity.ReportAssignment a = new com.cleancity.backend.entity.ReportAssignment();
        a.setReportId(reportId);
        a.setAction("ASSIGNED");
        a.setActorDriverId(driverId);
        a.setNotes(note);
        assignmentRepository.save(a);
        return new ReportResponse(r);
    }

    public List<ReportResponse> getAssigned(UUID driverId) {
        List<Report> list = reportRepository.findAll();
        List<ReportResponse> out = new ArrayList<>();
        for (Report r : list) {
            if (driverId.equals(r.getAssignedDriverId()))
                out.add(new ReportResponse(r));
        }
        return out;
    }

    @Transactional
    public ReportResponse completeReport(UUID reportId, UUID driverId, String action, String notes) {
        Report r = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (r.getAssignedDriverId() == null || !r.getAssignedDriverId().equals(driverId)) {
            throw new SecurityException("You don't have permission to perform this action");
        }
        if (r.getStatus() != com.cleancity.backend.entity.ReportStatus.ASSIGNED) {
            throw new IllegalStateException("Report not in ASSIGNED state");
        }
        if ("APPROVED".equalsIgnoreCase(action)) {
            r.setStatus(com.cleancity.backend.entity.ReportStatus.APPROVED);
            r.setCompletedByDriverId(driverId);
            r.setCompletedAt(LocalDateTime.now());
            reportRepository.save(r);
            // award points to uploader
            try {
                java.util.UUID userUuid = java.util.UUID.fromString(r.getUserId());
                userRepository.findById(userUuid).ifPresent(user -> {
                    user.setRewardPoints(user.getRewardPoints() + 10);
                    userRepository.save(user);
                });
            } catch (IllegalArgumentException ex) {
                userRepository.findByEmail(r.getUserId()).ifPresent(user -> {
                    user.setRewardPoints(user.getRewardPoints() + 10);
                    userRepository.save(user);
                });
            }
        } else {
            r.setStatus(com.cleancity.backend.entity.ReportStatus.REJECTED);
            r.setCompletedByDriverId(driverId);
            r.setCompletedAt(LocalDateTime.now());
            reportRepository.save(r);
        }
        com.cleancity.backend.entity.ReportAssignment a = new com.cleancity.backend.entity.ReportAssignment();
        a.setReportId(reportId);
        a.setAction(action.toUpperCase());
        a.setActorDriverId(driverId);
        a.setNotes(notes);
        assignmentRepository.save(a);
        return new ReportResponse(r);
    }

    @Transactional
    public ReportResponse uploadCompletionPhoto(UUID reportId, UUID driverId,
            org.springframework.web.multipart.MultipartFile image) throws java.io.IOException {
        Report r = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (r.getAssignedDriverId() == null || !r.getAssignedDriverId().equals(driverId)) {
            throw new SecurityException("You don't have permission to perform this action");
        }
        if (r.getStatus() != com.cleancity.backend.entity.ReportStatus.ASSIGNED) {
            throw new IllegalStateException("Report not in ASSIGNED state");
        }

        String imageUrl = s3StorageService.uploadFile(image);
        r.setCompletionImageUrl(imageUrl);
        r.setCompletedAt(java.time.LocalDateTime.now());
        r.setCompletedByDriverId(driverId);
        // mark for admin review; admin will approve/reject after manual verification
        r.setStatus(com.cleancity.backend.entity.ReportStatus.AWAITING_REVIEW);
        reportRepository.save(r);

        com.cleancity.backend.entity.ReportAssignment a = new com.cleancity.backend.entity.ReportAssignment();
        a.setReportId(reportId);
        a.setAction("COMPLETION_UPLOADED");
        a.setActorDriverId(driverId);
        a.setNotes("completion photo uploaded, awaiting admin review");
        assignmentRepository.save(a);

        return new ReportResponse(r);
    }

    public com.cleancity.backend.dto.DriverDto getDriverDto(String email, java.util.UUID driverId) {
        com.cleancity.backend.entity.Driver driverEntity = driverRepository.findByEmail(email).orElse(null);
        if (driverEntity == null) {
            // try by driver id
            driverEntity = driverRepository.findById(driverId).orElse(null);
        }
        if (driverEntity == null)
            return null;

        // compute stats: totalTasks, completionPercentage, streakDays, rating
        // placeholder
        final com.cleancity.backend.entity.Driver finalDriver = driverEntity;
        int totalTasks = reportRepository.findAll().stream()
                .mapToInt(
                        r -> finalDriver.getId() != null && finalDriver.getId().equals(r.getAssignedDriverId()) ? 1 : 0)
                .sum();
        int completed = reportRepository.findAll().stream().mapToInt(
                r -> finalDriver.getId() != null && finalDriver.getId().equals(r.getCompletedByDriverId()) ? 1 : 0)
                .sum();
        int completionPercentage = totalTasks == 0 ? 0 : (int) ((completed * 100.0) / totalTasks);

        com.cleancity.backend.dto.DriverDto dto = new com.cleancity.backend.dto.DriverDto(
                driverEntity.getId().toString(),
                driverEntity.getName(),
                driverEntity.getEmail(),
                "ROLE_DRIVER",
                driverEntity.getZone(),
                driverEntity.getTotalTasks(),
                driverEntity.getCompletionPercentage(),
                driverEntity.getRating(),
                driverEntity.getStreakDays(),
                driverEntity.getVehicleNumber(),
                driverEntity.getVehicleType(),
                driverEntity.getShiftTime(),
                driverEntity.getIsActive());

        // override totals with computed stats if stored values absent
        if (dto.getTotalTasks() == null || dto.getTotalTasks() == 0)
            dto.setTotalTasks(totalTasks);
        if (dto.getCompletionPercentage() == null || dto.getCompletionPercentage() == 0)
            dto.setCompletionPercentage(completionPercentage);

        return dto;
    }

    // ✅ Get all drivers
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    // ✅ Only active drivers
    public List<Driver> getActiveDrivers() {
        return driverRepository.findByIsActiveTrue();
    }

    // ✅ Filter by zone
    public List<Driver> getDriversByZone(String zone) {
        return driverRepository.findByZone(zone);
    }

    // ✅ Top rated drivers
    public List<Driver> getTopDrivers() {
        return driverRepository.findTop10ByOrderByRatingDesc();
    }
}
