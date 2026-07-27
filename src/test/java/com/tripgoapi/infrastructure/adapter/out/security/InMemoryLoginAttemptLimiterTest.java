package com.tripgoapi.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLoginAttemptLimiterTest {

    private static final String EMAIL = "jane@example.com";
    private static final String VICTIM_IP = "198.51.100.10";
    private static final String ATTACKER_IP = "203.0.113.66";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final InMemoryLoginAttemptLimiter limiter = new InMemoryLoginAttemptLimiter(clock);

    @Test
    void neverFailed_isNotBlocked() {
        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isFalse();
    }

    @Test
    void fourFailures_isStillNotBlocked() {
        for (int i = 0; i < 4; i++) {
            limiter.onLoginFailed(EMAIL, VICTIM_IP);
        }

        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isFalse();
    }

    @Test
    void fifthFailureWithinWindow_becomesBlocked() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL, VICTIM_IP);
        }

        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isTrue();
    }

    @Test
    void successResetsCounter() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL, VICTIM_IP);
        }
        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isTrue();

        limiter.onLoginSucceeded(EMAIL, VICTIM_IP);

        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isFalse();
    }

    @Test
    void blockExpiresAfterWindowElapses() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL, VICTIM_IP);
        }
        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isTrue();

        clock.advance(Duration.ofMinutes(15).plusSeconds(1));

        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isFalse();
    }

    @Test
    void failureAfterWindowExpiry_startsAFreshWindow_insteadOfImmediatelyBlocking() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL, VICTIM_IP);
        }
        clock.advance(Duration.ofMinutes(16));

        limiter.onLoginFailed(EMAIL, VICTIM_IP);

        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isFalse();
    }

    @Test
    void emailIsNormalized_caseAndWhitespaceInsensitive() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed("  JANE@Example.com  ", VICTIM_IP);
        }

        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isTrue();
    }

    @Test
    void differentEmails_areTrackedIndependently() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL, VICTIM_IP);
        }

        assertThat(limiter.isBlocked("someone-else@example.com", VICTIM_IP)).isFalse();
    }

    @Test
    void attackerSpammingVictimEmailFromDifferentIp_doesNotBlockVictimsOwnIp() {
        // The vulnerability this design fixes: keying the limiter by email alone would let a
        // stranger who doesn't know the password lock the victim out for 15 minutes just by
        // failing logins with the victim's email. Keying by (email, ip) means the attacker's
        // failures only ever accumulate against (victimEmail, attackerIp).
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL, ATTACKER_IP);
        }

        assertThat(limiter.isBlocked(EMAIL, ATTACKER_IP)).isTrue();
        assertThat(limiter.isBlocked(EMAIL, VICTIM_IP)).isFalse();
    }

    @Test
    void sameIpFailingManyDifferentEmails_tripsTheIpOnlyCap() {
        // Complementary attack: credential stuffing / account spraying from one source IP,
        // spread thin enough across emails that no single (email, ip) pair hits its own cap.
        for (int i = 0; i < 20; i++) {
            limiter.onLoginFailed("user" + i + "@example.com", ATTACKER_IP);
        }

        assertThat(limiter.isBlocked("brand-new-user@example.com", ATTACKER_IP)).isTrue();
    }

    @Test
    void ipOnlyCap_doesNotAffectOtherIps() {
        for (int i = 0; i < 20; i++) {
            limiter.onLoginFailed("user" + i + "@example.com", ATTACKER_IP);
        }

        assertThat(limiter.isBlocked("brand-new-user@example.com", VICTIM_IP)).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
