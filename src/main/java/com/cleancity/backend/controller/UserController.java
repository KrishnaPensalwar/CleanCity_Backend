package com.cleancity.backend.controller;

import com.cleancity.backend.auth.security.AccountDetailsImpl;
import com.cleancity.backend.dto.CityRankResponse;
import com.cleancity.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/rank")
    public ResponseEntity<CityRankResponse> getCityRank(Authentication authentication) {
        AccountDetailsImpl account = (AccountDetailsImpl) authentication.getPrincipal();
        CityRankResponse rankResponse = userService.getCityRank(account.getAccountId());
        return ResponseEntity.ok(rankResponse);
    }
}
