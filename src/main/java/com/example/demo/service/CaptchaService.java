package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Captcha as a random character string (letters + numbers) - user must retype the displayed string.
 * No external service used (no site key/secret key like Google reCAPTCHA),
 * auto-generated and validated on the server, each token can only be used once.
 */
@Service
public class CaptchaService {

    // Exclude easily confused characters: 0/O, 1/I/L, ...
    private static final String CHAR_POOL = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final long CAPTCHA_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes

    private final SecureRandom random = new SecureRandom();

    private static class CaptchaData {
        final String code;
        final long expiryTime;

        CaptchaData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    // Store token -> real captcha code + expiration time
    private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();

    /** Generate 1 new captcha, return token (keeps answer secret) + code (displayed for user to read). */
    public Map<String, Object> generateCaptcha() {
        cleanupExpired();

        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        String code = sb.toString();

        String token = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + CAPTCHA_EXPIRY_MS;
        captchaStore.put(token, new CaptchaData(code, expiry));

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("code", code); // FE displays as image/styled text for user to read and retype
        return result;
    }

    /**
     * Validate captcha. Token can only be used once (deleted after verify whether correct or wrong)
     * to prevent brute-force attempts on the same token.
     * Case-insensitive comparison to improve user experience when retyping.
     */
    public boolean validateCaptcha(String token, String userInput) {
        if (token == null || userInput == null) return false;

        CaptchaData data = captchaStore.remove(token); // use once then remove
        if (data == null) return false; // token does not exist / already used / cleaned up due to expiration
        if (System.currentTimeMillis() > data.expiryTime) return false; // expired

        return data.code.equalsIgnoreCase(userInput.trim());
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }
}