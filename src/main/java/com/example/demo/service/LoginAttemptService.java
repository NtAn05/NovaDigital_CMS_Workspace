package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks consecutive failed login attempts per IP address.
 * After MAX_ATTEMPTS failures, captcha verification is required.
 * The counter resets on a successful login or when more than LOCKOUT_WINDOW_MS has passed.
 */
@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_WINDOW_MS = 15 * 60 * 1000; // 15 minutes

    private static class AttemptData {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }

    private final ConcurrentHashMap<String, AttemptData> attemptMap = new ConcurrentHashMap<>();

    /** Called on each failed login. Returns new total count. */
    public int recordFailure(String ip) {
        AttemptData data = attemptMap.computeIfAbsent(ip, k -> new AttemptData());

        long now = System.currentTimeMillis();
        // Reset window if more than LOCKOUT_WINDOW_MS has elapsed since first failure
        if (now - data.windowStart > LOCKOUT_WINDOW_MS) {
            data.count.set(0);
            data.windowStart = now;
        }

        return data.count.incrementAndGet();
    }

    /** Called on successful login to clear the counter for this IP. */
    public void resetAttempts(String ip) {
        attemptMap.remove(ip);
    }

    /** Returns current attempt count without modifying state. */
    public int getAttemptCount(String ip) {
        AttemptData data = attemptMap.get(ip);
        if (data == null) return 0;

        long now = System.currentTimeMillis();
        if (now - data.windowStart > LOCKOUT_WINDOW_MS) return 0;

        return data.count.get();
    }

    /** Returns true if this IP is required to pass captcha before login. */
    public boolean isCaptchaRequired(String ip) {
        return getAttemptCount(ip) >= MAX_ATTEMPTS;
    }
}
