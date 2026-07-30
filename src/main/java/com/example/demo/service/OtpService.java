package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    
    private static class OtpDetails {
        String otp;
        LocalDateTime expiryTime;

        OtpDetails(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    private final Map<String, OtpDetails> otpCache = new ConcurrentHashMap<>();

    public String generateOtpCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    public void saveOtp(String email, String otp, int expirySeconds) {
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(expirySeconds);
        otpCache.put(email, new OtpDetails(otp, expiryTime));
    }

    public String generateOtp(String email, int expirySeconds) {
        String otp = generateOtpCode();
        saveOtp(email, otp, expirySeconds);
        return otp;
    }

    public String generateOtp(String email) {
        return generateOtp(email, 300); // Default 5 minutes
    }

    public boolean verifyOtp(String email, String otp) {
        OtpDetails details = otpCache.get(email);
        if (details == null) {
            return false;
        }
        if (details.expiryTime.isBefore(LocalDateTime.now())) {
            otpCache.remove(email);
            return false;
        }
        return details.otp.equals(otp);
    }

    public void clearOtp(String email) {
        otpCache.remove(email);
    }
}
