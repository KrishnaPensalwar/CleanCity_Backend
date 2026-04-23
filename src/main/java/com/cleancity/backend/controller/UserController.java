package com.cleancity.backend.controller;

import com.cleancity.backend.dto.CityRankResponse;
import com.cleancity.backend.security.services.UserDetailsImpl;
import com.cleancity.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/rank")
    public ResponseEntity<CityRankResponse> getCityRank(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        CityRankResponse rankResponse = userService.getCityRank(userDetails.getId());
        return ResponseEntity.ok(rankResponse);
    }
}
