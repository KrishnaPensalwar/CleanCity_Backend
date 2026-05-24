package com.cleancity.backend.auth.security;

import com.cleancity.backend.auth.domain.Account;
import com.cleancity.backend.auth.domain.AccountStatus;
import com.cleancity.backend.auth.domain.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountDetailsImpl implements UserDetails {

    private final UUID accountId;
    private final String email;
    private final String password;
    private final AccountStatus status;
    private final List<RoleType> roles;

    public AccountDetailsImpl(UUID accountId, String email, String password, AccountStatus status, List<RoleType> roles) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.status = status;
        this.roles = roles;
    }

    public static AccountDetailsImpl build(Account account) {
        List<RoleType> roleTypes = account.getRoles().stream()
                .map(r -> r.getRole())
                .collect(Collectors.toList());
        return new AccountDetailsImpl(
                account.getId(),
                account.getEmail(),
                account.getPassword(),
                account.getStatus(),
                roleTypes
        );
    }

    public UUID getAccountId() {
        return accountId;
    }

    /** @deprecated Use {@link #getAccountId()} */
    @Deprecated
    public UUID getId() {
        return accountId;
    }

    public String getEmail() {
        return email;
    }

    public List<RoleType> getRoles() {
        return roles;
    }

    public List<String> getRoleNames() {
        return roles.stream().map(RoleType::name).collect(Collectors.toList());
    }

    public boolean hasRole(RoleType roleType) {
        return roles.contains(roleType);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.toSpringAuthority()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE;
    }
}
