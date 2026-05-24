package com.cleancity.backend.device;

import com.cleancity.backend.device.dto.RegisterDeviceRequest;
import com.cleancity.backend.device.dto.RegisterDeviceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/devices")
public class UserDeviceController {
    private final UserDeviceService service;

    public UserDeviceController(UserDeviceService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterDeviceResponse> register(@Validated @RequestBody RegisterDeviceRequest req, Authentication auth) {
        RegisterDeviceResponse res = service.registerDevice(req, auth);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> delete(@PathVariable String deviceId, Authentication auth) {
        service.deleteDevice(deviceId, auth);
        return ResponseEntity.noContent().build();
    }
}
