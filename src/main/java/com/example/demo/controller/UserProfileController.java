package com.example.demo.controller;

import com.example.demo.entity.Member;
import com.example.demo.entity.User;
import com.example.demo.repository.MemberRepository;
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

    @Autowired
    private MemberRepository memberRepository;

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
        profile.put("email",     u.getEmail());
        profile.put("phone",     u.getPhone());
        profile.put("role",      u.getRole());
        profile.put("createdAt", u.getCreatedAt());
        profile.put("lastLogin", u.getLastLogin());

        // Check if user is a member -> separate profile details
        if ("ROLE_MEMBER".equalsIgnoreCase(u.getRole()) || "Team_Member".equalsIgnoreCase(u.getRole())) {
            Optional<Member> memberOpt = memberRepository.findByUserId(u.getId());
            if (memberOpt.isPresent()) {
                Member m = memberOpt.get();
                profile.put("fullName",       m.getName());
                profile.put("avatarUrl",      m.getAvatarUrl());
                profile.put("facebookUrl",    m.getFacebookUrl());
                profile.put("githubUrl",      m.getGithubUrl());
                profile.put("linkedinUrl",    m.getLinkedinUrl());
                profile.put("skills",         m.getSkills());
                profile.put("projectsWorked", m.getProjects());
            } else {
                profile.put("fullName",       u.getFullName());
                profile.put("avatarUrl",      u.getAvatarUrl());
                profile.put("facebookUrl",    null);
                profile.put("githubUrl",      null);
                profile.put("linkedinUrl",    null);
                profile.put("skills",         null);
                profile.put("projectsWorked", null);
            }
        } else {
            profile.put("fullName",       u.getFullName());
            profile.put("avatarUrl",      u.getAvatarUrl());
            profile.put("facebookUrl",    null);
            profile.put("githubUrl",      null);
            profile.put("linkedinUrl",    null);
            profile.put("skills",         null);
            profile.put("projectsWorked", null);
        }

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

        // Separate Member details vs User account details
        if (!"ROLE_MEMBER".equalsIgnoreCase(user.getRole()) && !"Team_Member".equalsIgnoreCase(user.getRole())) {
            // Regular User profile change -> updates users table
            if (body.containsKey("fullName")) {
                String v = ((String) body.get("fullName")).trim();
                if (!v.isBlank()) user.setFullName(v);
            }
            if (body.containsKey("avatarUrl")) {
                user.setAvatarUrl((String) body.get("avatarUrl"));
            }
        } else {
            // Member profile change -> updates members table (separated from users table)
            Member m = memberRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Member memberObj = new Member();
                        memberObj.setUserId(user.getId());
                        memberObj.setRole("Team Member");
                        return memberObj;
                    });

            if (body.containsKey("fullName")) {
                String v = ((String) body.get("fullName")).trim();
                if (!v.isBlank()) m.setName(v);
            }
            if (body.containsKey("avatarUrl")) {
                m.setAvatarUrl((String) body.get("avatarUrl"));
            }
            if (body.containsKey("facebookUrl")) {
                m.setFacebookUrl((String) body.get("facebookUrl"));
            }
            if (body.containsKey("githubUrl")) {
                m.setGithubUrl((String) body.get("githubUrl"));
            }
            if (body.containsKey("linkedinUrl")) {
                m.setLinkedinUrl((String) body.get("linkedinUrl"));
            }
            if (body.containsKey("skills")) {
                m.setSkills((String) body.get("skills"));
            }
            if (body.containsKey("projectsWorked")) {
                m.setProjects((String) body.get("projectsWorked"));
            }
            memberRepository.save(m);
        }

        User savedUser = userRepository.save(user);

        // Prepare Response
        Map<String, Object> result = new HashMap<>();
        result.put("success",  true);
        result.put("message",  "Profile updated successfully");
        result.put("username",  savedUser.getUsername());
        result.put("email",    savedUser.getEmail());
        result.put("phone",    savedUser.getPhone());

        if ("ROLE_MEMBER".equalsIgnoreCase(savedUser.getRole()) || "Team_Member".equalsIgnoreCase(savedUser.getRole())) {
            Optional<Member> memberOpt = memberRepository.findByUserId(savedUser.getId());
            if (memberOpt.isPresent()) {
                Member m = memberOpt.get();
                result.put("fullName",       m.getName());
                result.put("avatarUrl",      m.getAvatarUrl());
                result.put("facebookUrl",    m.getFacebookUrl());
                result.put("githubUrl",      m.getGithubUrl());
                result.put("linkedinUrl",    m.getLinkedinUrl());
                result.put("skills",         m.getSkills());
                result.put("projectsWorked", m.getProjects());
            }
        } else {
            result.put("fullName",       savedUser.getFullName());
            result.put("avatarUrl",      savedUser.getAvatarUrl());
        }

        return ResponseEntity.ok(result);
    }

    // ── Helper ────────────────────────────────────────────
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }
}
