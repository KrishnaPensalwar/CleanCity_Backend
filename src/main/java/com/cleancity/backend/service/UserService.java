package com.cleancity.backend.service;

import com.cleancity.backend.auth.domain.Account;
import com.cleancity.backend.auth.repository.AccountRepository;
import com.cleancity.backend.dto.CityRankResponse;
import com.cleancity.backend.dto.UpdateProfileRequest;
import com.cleancity.backend.dto.UserDto;
import com.cleancity.backend.entity.User;
import com.cleancity.backend.exception.ApiException;
import com.cleancity.backend.exception.ErrorCode;
import com.cleancity.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public UserService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public CityRankResponse getCityRank(UUID currentAccountId) {
        List<User> topUsersEntities = userRepository.findTop5ByOrderByRewardPointsDesc();

        List<CityRankResponse.UserRankDto> topUsers = topUsersEntities.stream()
                .map(u -> new CityRankResponse.UserRankDto(
                        u.getName(),
                        u.getRewardPoints(),
                        userRepository.getRankByRewardPoints(u.getRewardPoints())
                ))
                .collect(Collectors.toList());

        User currentUserEntity = userRepository.findByAccountId(currentAccountId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        long currentUserRank = userRepository.getRankByRewardPoints(currentUserEntity.getRewardPoints());

        CityRankResponse.UserRankDto currentUser = new CityRankResponse.UserRankDto(
                currentUserEntity.getName(),
                currentUserEntity.getRewardPoints(),
                currentUserRank
        );

        return new CityRankResponse(topUsers, currentUser);
    }

    @Transactional
    public UserDto updateProfile(UUID accountId, UpdateProfileRequest request) {
        if (request.getName() == null
                && request.getAddress() == null
                && request.getPhone() == null
                && request.getProfileImage() == null) {
            throw new ApiException(ErrorCode.PROFILE_UPDATE_EMPTY);
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.length() < 2) {
                throw new ApiException(ErrorCode.INVALID_PROFILE_NAME);
            }
            user.setName(name);
        }

        if (request.getAddress() != null) {
            String address = request.getAddress().trim();
            user.setAddress(address.isEmpty() ? null : address);
        }

        if (request.getProfileImage() != null) {
            String image = request.getProfileImage().trim();
            if (image.isEmpty()) {
                user.setProfileImage(null);
            } else if (!image.startsWith("https://")) {
                throw new ApiException(ErrorCode.INVALID_PROFILE_IMAGE);
            } else {
                user.setProfileImage(image);
            }
        }

        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            if (phone.isEmpty()) {
                account.setPhone(null);
            } else {
                String normalized = phone;
                if (accountRepository.findByPhone(normalized)
                        .filter(other -> !other.getId().equals(accountId))
                        .isPresent()) {
                    throw new ApiException(ErrorCode.PHONE_ALREADY_EXISTS);
                }
                account.setPhone(normalized);
            }
            accountRepository.save(account);
        }

        user = userRepository.save(user);
        return toUserDto(user, account);
    }

    private UserDto toUserDto(User user, Account account) {
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
}
