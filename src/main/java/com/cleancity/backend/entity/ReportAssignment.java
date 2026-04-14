package com.cleancity.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_assignments")
public class ReportAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID reportId;
    private String action;
    private UUID actorDriverId;
    private UUID actorUserId;
    private String notes;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getReportId() { return reportId; }
    public void setReportId(UUID r) { this.reportId = r; }
    public String getAction() { return action; }
    public void setAction(String a) { this.action = a; }
    public UUID getActorDriverId() { return actorDriverId; }
    public void setActorDriverId(UUID d) { this.actorDriverId = d; }
    public UUID getActorUserId() { return actorUserId; }
    public void setActorUserId(UUID u) { this.actorUserId = u; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
}
