package com.example.demo.controller;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
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
            response.put("message", "Email cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }

        java.util.Optional<User> optionalUser = userRepository.findByEmail(email.trim());
        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email does not exist in the system.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String otp = otpService.generateOtp(email.trim());

        try {
            emailService.sendOtpEmail(email.trim(), otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            System.out.println("========================================");
            System.out.println("FALLBACK PASSWORD RESET OTP FOR " + email + ": " + otp);
            System.out.println("========================================");
        }

        response.put("success", true);
        response.put("message", "OTP code has been sent to your email.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        Map<String, Object> response = new HashMap<>();

        if (email == null || otp == null || email.isBlank() || otp.isBlank()) {
            response.put("success", false);
            response.put("message", "Email and OTP code cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isValid = otpService.verifyOtp(email.trim(), otp.trim());
        if (isValid) {
            response.put("success", true);
            response.put("message", "OTP code is valid.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "OTP code is incorrect or expired.");
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
            response.put("message", "Please provide all required information.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isValid = otpService.verifyOtp(email.trim(), otp.trim());
        if (!isValid) {
            response.put("success", false);
            response.put("message", "OTP verification failed. Password cannot be changed.");
            return ResponseEntity.badRequest().body(response);
        }

        java.util.Optional<User> optionalUser = userRepository.findByEmail(email.trim());
        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = optionalUser.get();
        user.setPassword(com.example.demo.service.PasswordHasher.hash(newPassword));
        userRepository.save(user);

        otpService.clearOtp(email.trim());

        response.put("success", true);
        response.put("message", "Password has been updated successfully.");
        return ResponseEntity.ok(response);
    }
}
