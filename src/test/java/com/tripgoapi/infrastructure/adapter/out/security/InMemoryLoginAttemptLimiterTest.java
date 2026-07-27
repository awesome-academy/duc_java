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

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final InMemoryLoginAttemptLimiter limiter = new InMemoryLoginAttemptLimiter(clock);

    @Test
    void neverFailed_isNotBlocked() {
        assertThat(limiter.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void fourFailures_isStillNotBlocked() {
        for (int i = 0; i < 4; i++) {
            limiter.onLoginFailed(EMAIL);
        }

        assertThat(limiter.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void fifthFailureWithinWindow_becomesBlocked() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL);
        }

        assertThat(limiter.isBlocked(EMAIL)).isTrue();
    }

    @Test
    void successResetsCounter() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL);
        }
        assertThat(limiter.isBlocked(EMAIL)).isTrue();

        limiter.onLoginSucceeded(EMAIL);

        assertThat(limiter.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void blockExpiresAfterWindowElapses() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL);
        }
        assertThat(limiter.isBlocked(EMAIL)).isTrue();

        clock.advance(Duration.ofMinutes(15).plusSeconds(1));

        assertThat(limiter.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void failureAfterWindowExpiry_startsAFreshWindow_insteadOfImmediatelyBlocking() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL);
        }
        clock.advance(Duration.ofMinutes(16));

        limiter.onLoginFailed(EMAIL);

        assertThat(limiter.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void emailIsNormalized_caseAndWhitespaceInsensitive() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed("  JANE@Example.com  ");
        }

        assertThat(limiter.isBlocked(EMAIL)).isTrue();
    }

    @Test
    void differentEmails_areTrackedIndependently() {
        for (int i = 0; i < 5; i++) {
            limiter.onLoginFailed(EMAIL);
        }

        assertThat(limiter.isBlocked("someone-else@example.com")).isFalse();
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
