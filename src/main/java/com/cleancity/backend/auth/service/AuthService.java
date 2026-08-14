package com.cleancity.backend.auth.service;

import com.cleancity.backend.auth.domain.Account;
import com.cleancity.backend.auth.domain.AccountStatus;
import com.cleancity.backend.auth.domain.DriverApprovalStatus;
import com.cleancity.backend.auth.domain.RoleType;
import com.cleancity.backend.auth.dto.*;
import com.cleancity.backend.auth.repository.AccountRepository;
import com.cleancity.backend.auth.security.AccountDetailsImpl;
import com.cleancity.backend.dto.LoginRequest;
import com.cleancity.backend.dto.MessageResponse;
import com.cleancity.backend.dto.TokenRefreshResponse;
import com.cleancity.backend.dto.UserDto;
import com.cleancity.backend.entity.Driver;
import com.cleancity.backend.entity.RefreshToken;
import com.cleancity.backend.entity.User;
import com.cleancity.backend.exception.ApiException;
import com.cleancity.backend.exception.ErrorCode;
import com.cleancity.backend.repository.DriverRepository;
import com.cleancity.backend.repository.UserRepository;
import com.cleancity.backend.security.jwt.JwtUtils;
import com.cleancity.backend.security.services.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public MessageResponse registerUser(RegisterUserRequest request) {
        validateUniqueCredentials(request.getEmail(), request.getPhone());

        Account account = new Account();
        account.setEmail(normalizeEmail(request.getEmail()));
        account.setPhone(normalizePhone(request.getPhone()));
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(AccountStatus.ACTIVE);
        account.addRole(RoleType.USER);
        account = accountRepository.save(account);

        User profile = new User(account, request.getName());
        profile.setReportsFiled(0);
        profile.setReportsResolved(0);
        profile.setRewardPoints(0);
        userRepository.save(profile);

        return new MessageResponse("User registered successfully", true);
    }

    @Transactional
    public MessageResponse registerDriver(RegisterDriverRequest request) {
        validateUniqueCredentials(request.getEmail(), request.getPhone());

        Account account = new Account();
        account.setEmail(normalizeEmail(request.getEmail()));
        account.setPhone(normalizePhone(request.getPhone()));
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(AccountStatus.ACTIVE);
        account.addRole(RoleType.USER);
        account.addRole(RoleType.DRIVER);
        account = accountRepository.save(account);

        User profile = new User(account, request.getName());
        profile.setReportsFiled(0);
        profile.setReportsResolved(0);
        profile.setRewardPoints(0);
        userRepository.save(profile);

        Driver driver = new Driver();
        driver.setAccount(account);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setApprovalStatus(DriverApprovalStatus.PENDING);
        driver.setIsActive(true);
        driverRepository.save(driver);

        return new MessageResponse("Driver registered successfully", true);
    }

    @Transactional
    public MessageResponse convertToDriver(UUID accountId, ConvertToDriverRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (account.hasRole(RoleType.DRIVER)) {
            throw new ApiException(ErrorCode.ALREADY_DRIVER);
        }

        if (driverRepository.findByAccountId(accountId).isPresent()) {
            throw new ApiException(ErrorCode.ALREADY_DRIVER);
        }

        account.addRole(RoleType.DRIVER);
        accountRepository.save(account);

        Driver driver = new Driver();
        driver.setAccount(account);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setApprovalStatus(DriverApprovalStatus.PENDING);
        driver.setIsActive(true);
        driverRepository.save(driver);

        return new MessageResponse("Account upgraded to driver successfully", true);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizeEmail(request.getEmail()),
                        request.getPassword()));

        AccountDetailsImpl accountDetails = (AccountDetailsImpl) authentication.getPrincipal();
        String token = jwtUtils.generateJwtToken(authentication);

        refreshTokenService.deleteByAccountId(accountDetails.getAccountId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(accountDetails.getAccountId());

        Account account = accountRepository.findById(accountDetails.getAccountId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        UserDto profile = userRepository.findByAccountId(account.getId())
                .map(this::toUserDto)
                .orElse(null);

        return new LoginResponse(
                token,
                refreshToken.getToken(),
                accountDetails.getRoleNames(),
                toAccountDto(account),
                profile
        );
    }

    @Transactional
    public TokenRefreshResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_INVALID));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        Account account = refreshToken.getAccount();

        if (account.getStatus() != AccountStatus.ACTIVE) {
            refreshTokenService.deleteByAccountId(account.getId());
            throw new ApiException(ErrorCode.ACCOUNT_INACTIVE);
        }

        refreshTokenService.deleteByToken(refreshTokenValue);
        RefreshToken newRefresh = refreshTokenService.createRefreshToken(account.getId());
        String accessToken = jwtUtils.generateTokenFromAccount(account);

        return new TokenRefreshResponse(accessToken, newRefresh.getToken());
    }

    @Transactional
    public MessageResponse logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_INVALID));

        refreshTokenService.deleteByAccountId(refreshToken.getAccount().getId());
        return new MessageResponse("Logged out successfully", true);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        MeResponse response = new MeResponse();
        response.setAccount(toAccountDto(account));
        response.setRoles(account.getRoles().stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toList()));

        userRepository.findByAccountId(accountId)
                .ifPresent(user -> response.setUserProfile(toUserDto(user)));

        driverRepository.findByAccountId(accountId)
                .ifPresent(driver -> response.setDriverProfile(toDriverProfileDto(driver)));

        return response;
    }

    private void validateUniqueCredentials(String email, String phone) {
        String normalizedEmail = normalizeEmail(email);
        if (accountRepository.existsByEmail(normalizedEmail)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone != null && accountRepository.existsByPhone(normalizedPhone)) {
            throw new ApiException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private AccountDto toAccountDto(Account account) {
        return new AccountDto(
                account.getId().toString(),
                account.getEmail(),
                account.getPhone(),
                account.getStatus().name()
        );
    }

    private UserDto toUserDto(User user) {
        Account account = user.getAccount();
        List<String> roles = account.getRoles().stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toList());

        UserDto dto = new UserDto();
        dto.setId(account.getId().toString());
        dto.setName(user.getName());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setRoles(roles);
        dto.setRewardPoints(user.getRewardPoints());
        dto.setIsVerified(user.getIsVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setReportsFiled(user.getReportsFiled());
        dto.setReportsResolved(user.getReportsResolved());
        dto.setAddress(user.getAddress());
        dto.setProfileImage(user.getProfileImage());
        return dto;
    }

    private DriverProfileDto toDriverProfileDto(Driver driver) {
        return new DriverProfileDto(
                driver.getId().toString(),
                driver.getLicenseNumber(),
                driver.getVehicleNumber(),
                driver.getApprovalStatus().name(),
                driver.getIsActive()
        );
    }
}
