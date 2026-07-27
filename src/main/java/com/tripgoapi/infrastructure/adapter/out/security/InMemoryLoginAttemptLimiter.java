package com.tripgoapi.infrastructure.adapter.out.security;

import com.tripgoapi.application.port.out.LoginAttemptLimiterPort;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single-instance, in-memory login throttle. Good enough for the current single-node
 * deployment; if the app ever runs behind multiple instances this needs to move to a
 * shared store (e.g. Redis) since counters here are not shared across JVMs.
 */
@Component
public class InMemoryLoginAttemptLimiter implements LoginAttemptLimiterPort {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Clock clock;
    private final ConcurrentHashMap<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();

    public InMemoryLoginAttemptLimiter() {
        this(Clock.systemUTC());
    }

    InMemoryLoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean isBlocked(String email) {
        Attempts attempts = attemptsByEmail.get(normalize(email));
        return attempts != null && !isExpired(attempts) && attempts.count() >= MAX_ATTEMPTS;
    }

    @Override
    public void onLoginFailed(String email) {
        attemptsByEmail.compute(normalize(email), (key, existing) -> {
            Instant now = clock.instant();
            if (existing == null || isExpired(existing)) {
                return new Attempts(1, now);
            }
            return new Attempts(existing.count() + 1, existing.windowStart());
        });
    }

    @Override
    public void onLoginSucceeded(String email) {
        attemptsByEmail.remove(normalize(email));
    }

    private boolean isExpired(Attempts attempts) {
        return Duration.between(attempts.windowStart(), clock.instant()).compareTo(WINDOW) > 0;
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record Attempts(int count, Instant windowStart) {
    }
}
