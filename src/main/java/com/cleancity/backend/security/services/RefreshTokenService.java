package com.cleancity.backend.security.services;

import com.cleancity.backend.auth.domain.Account;
import com.cleancity.backend.auth.repository.AccountRepository;
import com.cleancity.backend.entity.RefreshToken;
import com.cleancity.backend.exception.ApiException;
import com.cleancity.backend.exception.ErrorCode;
import com.cleancity.backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwtRefreshExpirationMs:604800000}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AccountRepository accountRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountRepository = accountRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    public RefreshToken createRefreshToken(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setAccount(account);
        refreshToken.setExpiry(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
        refreshToken.setToken(java.util.UUID.randomUUID().toString() + "-" + java.util.UUID.randomUUID());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiry().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new ApiException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        return token;
    }

    @Transactional
    public int deleteByAccountId(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return refreshTokenRepository.deleteByAccount(account);
    }

    /** @deprecated Use {@link #deleteByAccountId(UUID)} */
    @Deprecated
    @Transactional
    public int deleteByUserId(UUID accountId) {
        return deleteByAccountId(accountId);
    }
}
