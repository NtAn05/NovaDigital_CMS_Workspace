package com.example.demo.controller;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.audit.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private com.example.demo.service.OtpService otpService;

    @Autowired
    private com.example.demo.service.EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User registeredUser = userService.registerUser(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("username", registeredUser.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            User authenticatedUser = userService.getUserByUsernameOrEmail(request.getUsernameOrEmail());

            // ── Audit Log: Ghi nhận đăng nhập thành công ──
            auditService.logAuthAction("LOGIN", authenticatedUser.getUsername());

            AuthResponse response = new AuthResponse(
                    token,
                    authenticatedUser.getUsername(),
                    authenticatedUser.getFullName(),
                    authenticatedUser.getRole(),
                    authenticatedUser.getEmail(),
                    authenticatedUser.getAvatarUrl());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Incorrect username/email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.isBlank()) {
            response.put("success", false);
            response.put("message", "Email không được để trống.");
            return ResponseEntity.badRequest().body(response);
        }

        java.util.Optional<User> optionalUser = userRepository.findByEmail(email.trim());
        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email không tồn tại trên hệ thống.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String otp = otpService.generateOtp(email.trim());

        try {
            emailService.sendOtpEmail(email.trim(), otp);
        } catch (Exception e) {
            System.err.println("Gửi mail OTP thất bại: " + e.getMessage());
            System.out.println("========================================");
            System.out.println("FALLBACK PASSWORD RESET OTP FOR " + email + ": " + otp);
            System.out.println("========================================");
        }

        response.put("success", true);
        response.put("message", "Mã OTP đã được gửi về email của bạn.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        Map<String, Object> response = new HashMap<>();

        if (email == null || otp == null || email.isBlank() || otp.isBlank()) {
            response.put("success", false);
            response.put("message", "Email và mã OTP không được để trống.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isValid = otpService.verifyOtp(email.trim(), otp.trim());
        if (isValid) {
            response.put("success", true);
            response.put("message", "Mã OTP hợp lệ.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Mã OTP không đúng hoặc đã hết hạn.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");
        Map<String, Object> response = new HashMap<>();

        if (email == null || otp == null || newPassword == null ||
                email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            response.put("success", false);
            response.put("message", "Vui lòng cung cấp đầy đủ thông tin.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isValid = otpService.verifyOtp(email.trim(), otp.trim());
        if (!isValid) {
            response.put("success", false);
            response.put("message", "Xác thực OTP thất bại. Mật khẩu không thể thay đổi.");
            return ResponseEntity.badRequest().body(response);
        }

        java.util.Optional<User> optionalUser = userRepository.findByEmail(email.trim());
        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "Không tìm thấy người dùng.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = optionalUser.get();
        user.setPassword(com.example.demo.service.PasswordHasher.hash(newPassword));
        userRepository.save(user);

        otpService.clearOtp(email.trim());

        response.put("success", true);
        response.put("message", "Mật khẩu đã được cập nhật thành công.");
        return ResponseEntity.ok(response);
    }
}
