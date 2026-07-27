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
 *
 * <p>Keyed by (email, source IP) rather than email alone: a limiter keyed only by email lets
 * anyone lock a victim's account out for the block window just by spamming failed logins with
 * the victim's email address, without knowing the password. Keying by the pair means an
 * attacker's failures only ever accumulate against (victimEmail, attackerIp) — the victim's own
 * (victimEmail, victimIp) bucket is untouched. A separate, higher-threshold IP-only counter
 * catches the complementary attack: one IP spraying many different email addresses.
 */
@Component
public class InMemoryLoginAttemptLimiter implements LoginAttemptLimiterPort {

    private static final int MAX_ATTEMPTS_PER_EMAIL_AND_IP = 5;
    private static final int MAX_ATTEMPTS_PER_IP = 20;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Clock clock;
    private final ConcurrentHashMap<String, Attempts> attemptsByEmailAndIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Attempts> attemptsByIp = new ConcurrentHashMap<>();

    public InMemoryLoginAttemptLimiter() {
        this(Clock.systemUTC());
    }

    InMemoryLoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean isBlocked(String email, String ipAddress) {
        return isBlocked(attemptsByEmailAndIp, compositeKey(email, ipAddress), MAX_ATTEMPTS_PER_EMAIL_AND_IP)
                || isBlocked(attemptsByIp, normalizeIp(ipAddress), MAX_ATTEMPTS_PER_IP);
    }

    @Override
    public void onLoginFailed(String email, String ipAddress) {
        recordFailure(attemptsByEmailAndIp, compositeKey(email, ipAddress));
        recordFailure(attemptsByIp, normalizeIp(ipAddress));
    }

    @Override
    public void onLoginSucceeded(String email, String ipAddress) {
        // Only the account+IP bucket is cleared — a successful login doesn't vouch for the IP
        // as a whole, which may still be attacking other accounts.
        attemptsByEmailAndIp.remove(compositeKey(email, ipAddress));
    }

    private boolean isBlocked(ConcurrentHashMap<String, Attempts> store, String key, int maxAttempts) {
        Attempts attempts = store.get(key);
        return attempts != null && !isExpired(attempts) && attempts.count() >= maxAttempts;
    }

    private void recordFailure(ConcurrentHashMap<String, Attempts> store, String key) {
        store.compute(key, (k, existing) -> {
            Instant now = clock.instant();
            if (existing == null || isExpired(existing)) {
                return new Attempts(1, now);
            }
            return new Attempts(existing.count() + 1, existing.windowStart());
        });
    }

    private boolean isExpired(Attempts attempts) {
        return Duration.between(attempts.windowStart(), clock.instant()).compareTo(WINDOW) > 0;
    }

    private String compositeKey(String email, String ipAddress) {
        return normalize(email) + "|" + normalizeIp(ipAddress);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String ipAddress) {
        return ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress.trim();
    }

    private record Attempts(int count, Instant windowStart) {
    }
}
