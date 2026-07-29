package com.example.demo.controller;

import com.example.demo.entity.AuthLog;
import com.example.demo.entity.DataAuditLog;
import com.example.demo.repository.AuthLogRepository;
import com.example.demo.repository.DataAuditLogRepository;
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

    @GetMapping("/data")
    public ResponseEntity<Page<com.example.demo.dto.DataAuditLogDTO>> getDataLogs(
            @RequestParam(required = false, defaultValue = "") String tableName,
            @RequestParam(required = false, defaultValue = "") String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DataAuditLog> logs = dataAuditLogRepository
                .findByTableNameContainingIgnoreCaseAndActionContainingIgnoreCaseOrderByCreatedAtDesc(tableName, action, pageable);
        
        Page<com.example.demo.dto.DataAuditLogDTO> dtoPage = logs.map(log -> {
            String role = "Unknown";
            if (log.getUsername() != null && !log.getUsername().isEmpty()) {
                java.util.Optional<User> userOpt = userRepository.findByUsername(log.getUsername());
                if (userOpt.isPresent()) {
                    role = userOpt.get().getRole();
                }
            }
            return new com.example.demo.dto.DataAuditLogDTO(
                    log.getId(),
                    log.getUsername(),
                    role,
                    log.getAction(),
                    log.getTableName(),
                    log.getDetail(),
                    log.getCreatedAt()
            );
        });

        return ResponseEntity.ok(dtoPage);
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
