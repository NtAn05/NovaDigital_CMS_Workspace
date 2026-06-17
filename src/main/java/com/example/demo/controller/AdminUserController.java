package com.example.demo.controller;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // ── GET BY ID ────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ── CREATE ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        Map<String, Object> error = new HashMap<>();

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            error.put("message", "Tên đăng nhập không được để trống");
            return ResponseEntity.badRequest().body(error);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            error.put("message", "Tên đăng nhập đã tồn tại");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            error.put("message", "Email không được để trống");
            return ResponseEntity.badRequest().body(error);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            error.put("message", "Email đã tồn tại");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            error.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
            return ResponseEntity.badRequest().body(error);
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : "");
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setPassword(PasswordHasher.hash(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_USER");
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
            error.put("message", "Không tìm thấy user với id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        User user = optional.get();

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check email uniqueness (exclude current user)
            Optional<User> existingEmail = userRepository.findByEmail(request.getEmail().trim());
            if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                error.put("message", "Email đã được sử dụng bởi tài khoản khác");
                return ResponseEntity.badRequest().body(error);
            }
            user.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(request.getRole());
        }
        // Update enabled status if provided
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                error.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
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
        if (!userRepository.existsById(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Không tìm thấy user với id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        userRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa user thành công");
        return ResponseEntity.ok(result);
    }

    // ── HELPER ───────────────────────────────────────────
    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getRole(),
                u.isEnabled(),
                u.getLastLogin(),
                u.getCreatedAt()
        );
    }
}
