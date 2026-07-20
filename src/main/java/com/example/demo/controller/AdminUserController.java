package com.example.demo.controller;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    // ── GET ALL ──────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .stream()
                .filter(u -> !isResourceAccount(u))
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // ── GET BY ID ────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .filter(u -> !isResourceAccount(u))
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ── CREATE ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        Map<String, Object> error = new HashMap<>();

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            error.put("message", "Username cannot empty");
            return ResponseEntity.badRequest().body(error);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            error.put("message", "Username already exists.");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            error.put("message", "Email cannot empty");
            return ResponseEntity.badRequest().body(error);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            error.put("message", "The email already exists. ");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            error.put("message", "The password must be at least 6 characters long.");
            return ResponseEntity.badRequest().body(error);
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : "");
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setPassword(PasswordHasher.hash(request.getPassword()));
        user.setRole(standardizeRole(request.getRole()));
        user.setEnabled(true);

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // ── UPDATE ───────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        Map<String, Object> error = new HashMap<>();

        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) {
            error.put("message", "Cannot find user with id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        User user = optional.get();
        if (isResourceAccount(user)) {
            error.put("message", "The dedicated Resource Allocation account cannot be managed from Admin.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check email uniqueness (exclude current user)
            Optional<User> existingEmail = userRepository.findByEmail(request.getEmail().trim());
            if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                error.put("message", "The email is already in use by another account.");
                return ResponseEntity.badRequest().body(error);
            }
            user.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(standardizeRole(request.getRole()));
        }
        // Update enabled status if provided
        if (request.getEnabled() != null) {
            user.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        }
        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                error.put("message", "The password must be at least 6 characters long.");
                return ResponseEntity.badRequest().body(error);
            }
            user.setPassword(PasswordHasher.hash(request.getPassword()));
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toResponse(saved));
    }

    // ── DELETE ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Cannot find user with id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        if (isResourceAccount(optional.get())) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "The dedicated Resource Allocation account cannot be deleted from Admin.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        userRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "User successfully deleted.");
        return ResponseEntity.ok(result);
    }

    // ── HELPER ───────────────────────────────────────────
    private boolean isResourceAccount(User user) {
        return user != null && "ROLE_RESOURCE".equalsIgnoreCase(user.getRole());
    }

    private String standardizeRole(String role) {
        if (role == null || role.isBlank()) return "ROLE_USER";
        String r = role.trim().toUpperCase().replace("_", " ");
        if ("TEAM MEMBER".equals(r) || "MEMBER".equals(r) || "ROLE MEMBER".equals(r)) {
            return "ROLE_MEMBER";
        }
        if ("ADMIN".equals(r) || "ROLE ADMIN".equals(r)) {
            return "ROLE_ADMIN";
        }
        if ("USER".equals(r) || "ROLE USER".equals(r)) {
            return "ROLE_USER";
        }
        return role;
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getRole(),
                u.isEnabled(),
                u.getAvatarUrl(),
                u.getLastLogin(),
                u.getCreatedAt()
        );
    }
}
