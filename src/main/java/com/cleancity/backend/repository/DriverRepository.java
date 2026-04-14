package com.cleancity.backend.repository;

import com.cleancity.backend.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByEmail(String email);
}
