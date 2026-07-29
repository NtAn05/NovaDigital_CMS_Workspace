package com.example.demo.dto;

import java.time.LocalDateTime;

public class DataAuditLogDTO {
    private Long id;
    private String username;
    private String role;
    private String action;
    private String tableName;
    private String detail;
    private LocalDateTime createdAt;

    public DataAuditLogDTO() {}

    public DataAuditLogDTO(Long id, String username, String role, String action, String tableName, String detail, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.action = action;
        this.tableName = tableName;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
