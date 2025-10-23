package com.codfish71.attendance.model;

/**
 * Application roles. Using ROLE_ prefix strings because the rest of the codebase expects authorities
 * in the form "ROLE_SOMETHING". Keep this enum as the canonical source of role names.
 */
public enum Role {
    ROLE_PROJECT_MANAGER,
    ROLE_SITE_LEAD,
    ROLE_COORDINATOR,
    ROLE_HR,
    ROLE_ADMIN;

    /**
     * Return the authority string used by Spring Security (same as enum name).
     */
    public String getAuthority() {
        return name();
    }

    /**
     * Convenience lookup from authority string.
     */
    public static Role fromAuthority(String authority) {
        if (authority == null) return null;
        for (Role r : values()) {
            if (r.name().equals(authority)) return r;
        }
        throw new IllegalArgumentException("Unknown role: " + authority);
    }
}