package com.myrecipe.security;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class EmailConfirmationResendThrottle {
    private static final int EMAIL_LIMIT = 3;
    private static final int IP_LIMIT = 10;
    private static final long WINDOW_MINUTES = 15;

    private final Map<String, AttemptWindow> emailAttempts = new ConcurrentHashMap<>();
    private final Map<String, AttemptWindow> ipAttempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public EmailConfirmationResendThrottle() {
        this(Clock.systemUTC());
    }

    EmailConfirmationResendThrottle(Clock clock) {
        this.clock = clock;
    }

    public boolean isAllowed(String normalizedEmail, String requestIp) {
        Instant now = Instant.now(clock);
        String emailKey = normalizedEmail == null ? "" : normalizedEmail;
        String ipKey = requestIp == null ? "" : requestIp;

        boolean emailAllowed = incrementAndCheck(emailAttempts, emailKey, EMAIL_LIMIT, now);
        boolean ipAllowed = incrementAndCheck(ipAttempts, ipKey, IP_LIMIT, now);
        return emailAllowed && ipAllowed;
    }

    public void clear() {
        emailAttempts.clear();
        ipAttempts.clear();
    }

    private boolean incrementAndCheck(Map<String, AttemptWindow> attempts,
                                      String key,
                                      int limit,
                                      Instant now) {
        AttemptWindow window = attempts.compute(key, (ignored, current) -> {
            if (current == null || current.windowStart.plus(WINDOW_MINUTES, ChronoUnit.MINUTES).isBefore(now)) {
                return new AttemptWindow(now, 1);
            }

            current.count++;
            return current;
        });

        return window.count <= limit;
    }

    private static class AttemptWindow {
        private final Instant windowStart;
        private int count;

        private AttemptWindow(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
