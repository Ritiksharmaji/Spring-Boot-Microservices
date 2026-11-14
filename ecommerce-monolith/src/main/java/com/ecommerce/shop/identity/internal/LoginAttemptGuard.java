package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.shared.BusinessException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple brute-force protection: 5 failed logins per email lock it for 15 minutes (per instance). */
@Component
class LoginAttemptGuard {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant firstFailure) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    void checkAllowed(String email) {
        Attempts a = attempts.get(key(email));
        if (a == null) {
            return;
        }
        if (Instant.now().isAfter(a.firstFailure().plus(LOCK))) {
            attempts.remove(key(email));
        } else if (a.failures() >= MAX_FAILURES) {
            throw BusinessException.tooManyRequests("Too many failed logins; try again in 15 minutes");
        }
    }

    void failed(String email) {
        attempts.merge(key(email), new Attempts(1, Instant.now()),
                (old, n) -> new Attempts(old.failures() + 1, old.firstFailure()));
    }

    void succeeded(String email) {
        attempts.remove(key(email));
    }

    private String key(String email) {
        return email.trim().toLowerCase();
    }
}
