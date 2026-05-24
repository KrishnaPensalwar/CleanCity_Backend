package com.cleancity.backend.repository;

import com.cleancity.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.account.id = :accountId")
    Optional<User> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT u FROM User u JOIN u.account a WHERE a.email = :email")
    Optional<User> findByAccountEmail(@Param("email") String email);

    java.util.List<User> findTop5ByOrderByRewardPointsDesc();

    @Query("SELECT count(u) + 1 FROM User u WHERE u.rewardPoints > :points")
    long getRankByRewardPoints(@Param("points") Integer points);
}
