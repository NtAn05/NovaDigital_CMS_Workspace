package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    // ── GET current user profile ──────────────────────────
    @GetMapping
    public ResponseEntity<?> getProfile() {
        String usernameOrEmail = getCurrentUsername();
        Optional<User> opt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        User u = opt.get();
        Map<String, Object> profile = new HashMap<>();
        profile.put("id",        u.getId());
        profile.put("username",  u.getUsername());
        profile.put("fullName",  u.getFullName());
        profile.put("email",     u.getEmail());
        profile.put("phone",     u.getPhone());
        profile.put("role",      u.getRole());
        profile.put("avatarUrl", u.getAvatarUrl());
        profile.put("createdAt", u.getCreatedAt());
        profile.put("lastLogin", u.getLastLogin());
        return ResponseEntity.ok(profile);
    }

    // ── UPDATE current user profile ───────────────────────
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        String usernameOrEmail = getCurrentUsername();
        Optional<User> opt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        User user = opt.get();

        // Username – check uniqueness
        if (body.containsKey("username")) {
            String newUsername = ((String) body.get("username")).trim();
            if (!newUsername.isBlank() && !newUsername.equals(user.getUsername())) {
                if (userRepository.findByUsername(newUsername).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Username already exists"));
                }
                user.setUsername(newUsername);
            }
        }

        // Full name
        if (body.containsKey("fullName")) {
            String v = ((String) body.get("fullName")).trim();
            if (!v.isBlank()) user.setFullName(v);
        }

        // Email – check uniqueness
        if (body.containsKey("email")) {
            String newEmail = ((String) body.get("email")).trim();
            if (!newEmail.isBlank() && !newEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Email is already in use by another account"));
                }
                user.setEmail(newEmail);
            }
        }

        // Phone
        if (body.containsKey("phone")) {
            String v = (String) body.get("phone");
            user.setPhone(v == null ? null : v.trim());
        }

        // Avatar URL (already uploaded, we just store the URL)
        if (body.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) body.get("avatarUrl"));
        }

        // Password change (optional)
        if (body.containsKey("newPassword")) {
            String np = ((String) body.get("newPassword")).trim();
            if (!np.isBlank()) {
                if (np.length() < 6) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "New password must be at least 6 characters"));
                }
                // Verify old password first
                String oldPw = body.containsKey("oldPassword") ? ((String) body.get("oldPassword")).trim() : "";
                if (!PasswordHasher.verify(oldPw, user.getPassword())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Current password is incorrect"));
                }
                user.setPassword(PasswordHasher.hash(np));
            }
        }

        User saved = userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success",  true);
        result.put("message",  "Profile updated successfully");
        result.put("username",  saved.getUsername());
        result.put("fullName", saved.getFullName());
        result.put("email",    saved.getEmail());
        result.put("phone",    saved.getPhone());
        result.put("avatarUrl", saved.getAvatarUrl());
        return ResponseEntity.ok(result);
    }

    // ── Helper ────────────────────────────────────────────
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }
}
