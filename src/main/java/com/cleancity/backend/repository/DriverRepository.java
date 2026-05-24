package com.cleancity.backend.repository;

import com.cleancity.backend.auth.domain.DriverApprovalStatus;
import com.cleancity.backend.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    @Query("SELECT d FROM Driver d WHERE d.account.id = :accountId")
    Optional<Driver> findByAccountId(@Param("accountId") UUID accountId);

    List<Driver> findByIsActiveTrue();

    List<Driver> findByZone(String zone);

    List<Driver> findTop10ByOrderByRatingDesc();

    List<Driver> findByApprovalStatus(DriverApprovalStatus approvalStatus);
}
