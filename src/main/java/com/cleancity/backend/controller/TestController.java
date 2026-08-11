package com.cleancity.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Dev-only smoke endpoint. Disabled in production (requires authentication via security config,
 * and returns 404 unless app.expose-test-endpoint=true).
 */
@RestController
public class TestController {

    @org.springframework.beans.factory.annotation.Value("${app.expose-test-endpoint:false}")
    private boolean exposeTestEndpoint;

    @GetMapping("/test")
    public Map<String, String> test() {
        if (!exposeTestEndpoint) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return Map.of("message", "working");
    }
}
