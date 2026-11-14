package com.ecommerce.shop.shared.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Reads the caller's identity from the validated JWT (sub = user id). */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static long id(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
