package com.cleancity.backend.auth.repository;

import com.cleancity.backend.auth.domain.AccountRole;
import com.cleancity.backend.auth.domain.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, UUID> {

    List<AccountRole> findByAccountId(UUID accountId);

    boolean existsByAccountIdAndRole(UUID accountId, RoleType role);
}
