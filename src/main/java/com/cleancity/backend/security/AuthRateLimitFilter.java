package com.cleancity.backend.security;

import com.cleancity.backend.exception.ErrorCode;
import com.cleancity.backend.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for public auth endpoints to slow brute-force attacks.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MS = 60_000L;

    private final ObjectMapper objectMapper;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/signup")
                || path.startsWith("/auth/refresh"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientKey(request) + ":" + request.getRequestURI();
        long now = System.currentTimeMillis();

        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startMs >= WINDOW_MS) {
                return new Window(now);
            }
            return existing;
        });

        if (window.count.incrementAndGet() > MAX_REQUESTS) {
            ErrorCode code = ErrorCode.RATE_LIMITED;
            response.setStatus(code.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(
                    response.getOutputStream(),
                    ErrorResponse.of(code, code.getMessage(), request.getRequestURI()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static final class Window {
        private final long startMs;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long startMs) {
            this.startMs = startMs;
        }
    }
}
