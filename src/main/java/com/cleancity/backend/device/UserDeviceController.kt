package com.cleancity.backend.device

import com.cleancity.backend.device.dto.RegisterDeviceRequest
import com.cleancity.backend.device.dto.RegisterDeviceResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/devices")
class UserDeviceController(private val service: UserDeviceService) {

    @PostMapping("/register")
    fun register(@Validated @RequestBody req: RegisterDeviceRequest, auth: Authentication): ResponseEntity<RegisterDeviceResponse> {
        val res = service.registerDevice(req, auth)
        return ResponseEntity.ok(res)
    }

    @DeleteMapping("/{deviceId}")
    fun delete(@PathVariable deviceId: String, auth: Authentication): ResponseEntity<Void> {
        service.deleteDevice(deviceId, auth)
        return ResponseEntity.noContent().build()
    }
}
