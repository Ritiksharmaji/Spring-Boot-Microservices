package com.app_ecom.controller;

import com.app_ecom.exception.ForbiddenException;

import java.util.Arrays;

/** Helpers for the identity headers the API gateway adds from the verified JWT. */
final class CurrentUser {

    static final String USER_ID = "X-User-ID";
    static final String ROLES = "X-User-Roles";

    private CurrentUser() {
    }

    /** Defence in depth: the gateway already restricts admin paths, the service checks again. */
    static void requireAdmin(String rolesHeader) {
        boolean admin = rolesHeader != null && Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .anyMatch("ADMIN"::equalsIgnoreCase);
        if (!admin) {
            throw new ForbiddenException("Admin role required");
        }
    }
}
