package com.cleancity.backend.auth.repository;

import com.cleancity.backend.auth.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
