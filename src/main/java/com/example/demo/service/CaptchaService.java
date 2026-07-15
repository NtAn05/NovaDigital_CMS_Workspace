package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static class CaptchaData {
        int answer;
        long expiryTime;

        CaptchaData(int answer, long expiryTime) {
            this.answer = answer;
            this.expiryTime = expiryTime;
        }
    }

    // Stores captcha tokens and their answers/expiry (5 minutes expiry)
    private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
    private static final long CAPTCHA_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes

    public Map<String, Object> generateCaptcha() {
        int num1 = (int) (Math.random() * 9) + 1; // 1-9
        int num2 = (int) (Math.random() * 9) + 1; // 1-9
        int answer = num1 + num2;
        String question = num1 + " + " + num2;

        String token = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + CAPTCHA_EXPIRY_MS;
        captchaStore.put(token, new CaptchaData(answer, expiry));

        // Periodically clean up expired captchas
        cleanupExpired();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("question", question);
        return result;
    }

    public boolean validateCaptcha(String token, Integer answer) {
        if (token == null || answer == null) {
            return false;
        }
        CaptchaData data = captchaStore.remove(token); // Use once
        if (data == null) {
            return false;
        }
        return System.currentTimeMillis() <= data.expiryTime && data.answer == answer;
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }
}
