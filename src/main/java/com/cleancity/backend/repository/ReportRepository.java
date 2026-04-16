package com.cleancity.backend.repository;

import com.cleancity.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cleancity.backend.entity.ReportStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByStatus(ReportStatus status);
    List<Report> findByUserId(String userId);

    @Modifying
    @Query(value = "UPDATE reports SET assigned_driver_id = :driverId, assigned_at = now(), status = 'ASSIGNED', updated_at = now() WHERE id = :reportId AND status = 'PENDING' AND assigned_driver_id IS NULL", nativeQuery = true)
    int assignIfPending(@Param("reportId") UUID reportId, @Param("driverId") UUID driverId);

    @Query(value = "SELECT id, latitude, longitude, (6371000 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lon)) + sin(radians(:lat)) * sin(radians(latitude)))) AS distance_m FROM reports WHERE status = 'PENDING' AND latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon ORDER BY distance_m LIMIT :limit", nativeQuery = true)
    List<Object[]> findNearbyPending(@Param("lat") double lat, @Param("lon") double lon, @Param("minLat") double minLat, @Param("maxLat") double maxLat, @Param("minLon") double minLon, @Param("maxLon") double maxLon, @Param("limit") int limit);
}
