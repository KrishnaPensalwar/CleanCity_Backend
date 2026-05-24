package com.cleancity.backend.auth.domain;

public enum RoleType {
    USER,
    DRIVER,
    ADMIN;

    public String toSpringAuthority() {
        return "ROLE_" + name();
    }

    public static RoleType fromString(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return RoleType.valueOf(normalized);
    }
}
