package com.cleancity.backend.device;

import com.cleancity.backend.device.dto.RegisterDeviceRequest;
import com.cleancity.backend.device.dto.RegisterDeviceResponse;
import com.cleancity.backend.auth.security.AccountDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDeviceService {
    private static final Logger log = LoggerFactory.getLogger(UserDeviceService.class);

    private final UserDeviceRepository repo;

    public UserDeviceService(UserDeviceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public RegisterDeviceResponse registerDevice(RegisterDeviceRequest req, Authentication auth) {
        AccountDetailsImpl principal = (AccountDetailsImpl) auth.getPrincipal();
        String userId = principal.getAccountId().toString();

        LocalDateTime now = LocalDateTime.now();

        UserDevice existingByToken = repo.findByUserIdAndFcmToken(userId, req.getFcmToken()).orElse(null);
        if (existingByToken != null) {
            existingByToken.setDeviceName(req.getDeviceName());
            existingByToken.setDeviceId(req.getDeviceId());
            existingByToken.setActive(true);
            existingByToken.setUpdatedAt(now);
            UserDevice saved = repo.save(existingByToken);
            return new RegisterDeviceResponse("Device registered successfully", saved.getDeviceId(), saved.getFcmToken(), saved.getDeviceName());
        }

        UserDevice existing = repo.findByUserIdAndDeviceId(userId, req.getDeviceId()).orElse(null);

        if (existing != null) {
            existing.setFcmToken(req.getFcmToken());
            existing.setDeviceName(req.getDeviceName());
            existing.setActive(true);
            existing.setUpdatedAt(now);
            UserDevice saved = repo.save(existing);
            return new RegisterDeviceResponse("Device registered successfully", saved.getDeviceId(), saved.getFcmToken(), saved.getDeviceName());
        } else {
            UserDevice entity = new UserDevice(
                userId,
                req.getDeviceId(),
                req.getFcmToken(),
                req.getDeviceName(),
                req.getPlatform()
            );
            UserDevice saved = repo.save(entity);
            return new RegisterDeviceResponse("Device registered successfully", saved.getDeviceId(), saved.getFcmToken(), saved.getDeviceName());
        }
    }

    @Transactional
    public void deleteDevice(String deviceId, Authentication auth) {
        AccountDetailsImpl principal = (AccountDetailsImpl) auth.getPrincipal();
        String userId = principal.getAccountId().toString();

        repo.findByUserIdAndDeviceId(userId, deviceId).ifPresent(device -> {
            device.setActive(false);
            device.setUpdatedAt(LocalDateTime.now());
            repo.save(device);
        });
    }

    public List<String> findActiveTokensForUser(String userId) {
        return repo.findAllByUserIdAndIsActiveTrue(userId).stream()
                .map(UserDevice::getFcmToken)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeTokenIfExists(String userId, String token) {
        repo.findAllByUserIdAndIsActiveTrue(userId).forEach(device -> {
            if (device.getFcmToken().equals(token)) {
                device.setActive(false);
                device.setUpdatedAt(LocalDateTime.now());
                repo.save(device);
            }
        });
    }
}
