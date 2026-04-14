package com.cleancity.backend.repository;

import com.cleancity.backend.entity.ReportAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportAssignmentRepository extends JpaRepository<ReportAssignment, UUID> {
}
