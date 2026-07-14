package com.example.demo.controller;

import com.example.demo.audit.entity.AuthLog;
import com.example.demo.audit.entity.DataAuditLog;
import com.example.demo.audit.repository.AuthLogRepository;
import com.example.demo.audit.repository.DataAuditLogRepository;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final DataAuditLogRepository dataAuditLogRepository;
    private final AuthLogRepository authLogRepository;
    private final UserRepository userRepository;

    public AuditLogController(DataAuditLogRepository dataAuditLogRepository, AuthLogRepository authLogRepository, UserRepository userRepository) {
        this.dataAuditLogRepository = dataAuditLogRepository;
        this.authLogRepository = authLogRepository;
        this.userRepository = userRepository;
    }

    // Old unpaginated endpoint (kept for compatibility or remove it, but better replace with paginated if needed, or leave it)
    @GetMapping("/data")
    public ResponseEntity<List<DataAuditLog>> getDataLogs() {
        List<DataAuditLog> logs = dataAuditLogRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/data-users")
    public ResponseEntity<Page<User>> getDataUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAllUsersSortedByLatestDataAudit(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/data/user/{username}")
    public ResponseEntity<Page<DataAuditLog>> getUserDataLogs(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DataAuditLog> logs = dataAuditLogRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/auth")
    public ResponseEntity<Page<AuthLog>> getAuthLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuthLog> logs = authLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ResponseEntity.ok(logs);
    }
}
