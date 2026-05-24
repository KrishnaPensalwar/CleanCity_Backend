package com.cleancity.backend.entity;

import com.cleancity.backend.auth.domain.Account;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(name = "profile_image", length = 1000)
    private String profileImage;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reward_points", nullable = false)
    private Integer rewardPoints = 0;

    @Column(name = "reports_filed", nullable = false)
    private Integer reportsFiled = 0;

    @Column(name = "reports_resolved", nullable = false)
    private Integer reportsResolved = 0;

    public User() {}

    public User(Account account, String name) {
        this.account = account;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public UUID getAccountId() {
        return account != null ? account.getId() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public Integer getReportsFiled() {
        return reportsFiled;
    }

    public void setReportsFiled(Integer reportsFiled) {
        this.reportsFiled = reportsFiled;
    }

    public Integer getReportsResolved() {
        return reportsResolved;
    }

    public void setReportsResolved(Integer reportsResolved) {
        this.reportsResolved = reportsResolved;
    }
}
