package com.example.demo.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * ============================================================
 * JwtTokenProvider - Component sinh và giải mã JWT Token
 * ============================================================
 * Chịu trách nhiệm tạo chuỗi Token mã hóa HMAC-SHA256 và giải mã
 * trích xuất Username cũng như kiểm tra tính hợp lệ của Token.
 */
@Component
public class JwtTokenProvider {

    // Đọc secret key từ file application.properties (Đảm bảo độ dài tối thiểu 256-bit)
    @Value("${app.jwt.secret:NovaDigitalSecretKeyForJWTTokenGeneration2026WithMinimum256BitsLength}")
    private String jwtSecretString;

    private final long jwtExpirationMs = 86400000; // Thời gian sống của Token: 24 giờ (86,400,000 miligiây)
    private Key jwtSecretKey;

    /**
     * Tự động chạy ngay sau khi Spring khởi tạo Bean này (@PostConstruct)
     * Chuyển chuỗi Secret String thành đối tượng Key chuẩn HMAC-SHA256
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecretString.getBytes(StandardCharsets.UTF_8);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Sinh Token từ đối tượng Authentication của Spring Security khi đăng nhập thành công
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    /**
     * Sinh Token trực tiếp từ Username (Dùng cho luồng OAuth2 Google hoặc link xác nhận Email)
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Xây dựng chuỗi JWT theo chuẩn: Subject (Username), IssuedAt (Ngày tạo), Expiration (Hạn dùng), Signature (Ký số HS256)
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Giải mã Token JWT và lấy Username được lưu trữ bên trong Claim Subject
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey) // Cung cấp Secret Key để giải mã và kiểm tra chữ ký
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // Trả về Username
    }

    /**
     * Kiểm tra tính hợp lệ của Token (Chữ ký đúng, chưa hết hạn, đúng định dạng)
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true; // Token hợp lệ
        } catch (JwtException | IllegalArgumentException ex) {
            // Bắt các lỗi: Hết hạn (ExpiredJwtException), Sai chữ ký (SignatureException), Sai định dạng (MalformedJwtException)
            System.err.println("Lỗi xác thực Token JWT: " + ex.getMessage());
        }
        return false; // Token không hợp lệ
    }
}