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

    public String generateOtp(String email) {
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(2); // OTP valid for 5 minutes
        otpCache.put(email, new OtpDetails(otp, expiryTime));
        return otp;
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
