package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Captcha dạng chuỗi ký tự ngẫu nhiên (chữ + số) - khách phải gõ lại đúng chuỗi hiển thị.
 * Không dùng dịch vụ ngoài (không cần site key/secret key như Google reCAPTCHA),
 * tự sinh và tự xác thực ở server, mỗi token dùng được đúng 1 lần.
 */
@Service
public class CaptchaService {

    // Bỏ các ký tự dễ gây nhầm lẫn khi đọc: 0/O, 1/I/L, ...
    private static final String CHAR_POOL = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final long CAPTCHA_EXPIRY_MS = 5 * 60 * 1000; // 5 phút

    private final SecureRandom random = new SecureRandom();

    private static class CaptchaData {
        final String code;
        final long expiryTime;

        CaptchaData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    // Lưu token -> mã captcha thật + thời điểm hết hạn
    private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();

    /** Sinh 1 chuỗi captcha mới, trả về token (giữ bí mật đáp án) + code (hiển thị cho người dùng đọc). */
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
        result.put("code", code); // FE hiển thị dạng ảnh/text cách điệu cho khách đọc và gõ lại
        return result;
    }

    /**
     * Xác thực captcha. Token chỉ dùng được đúng 1 lần (dù đúng hay sai đều bị xoá
     * sau khi verify) để tránh brute-force đoán nhiều lần trên cùng 1 token.
     * So sánh không phân biệt hoa/thường để đỡ khó chịu cho người dùng khi gõ lại.
     */
    public boolean validateCaptcha(String token, String userInput) {
        if (token == null || userInput == null) return false;

        CaptchaData data = captchaStore.remove(token); // dùng 1 lần rồi xoá
        if (data == null) return false; // token không tồn tại / đã dùng / đã bị dọn do hết hạn
        if (System.currentTimeMillis() > data.expiryTime) return false; // hết hạn

        return data.code.equalsIgnoreCase(userInput.trim());
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }
}