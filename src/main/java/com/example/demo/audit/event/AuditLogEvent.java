package com.example.demo.audit.event;

import org.springframework.context.ApplicationEvent;

public class AuditLogEvent extends ApplicationEvent {

    private final String username;
    private final String action;
    private final String tableName;
    private final String detail; // JSON Diff string

    public AuditLogEvent(Object source, String username, String action, String tableName, String detail) {
        super(source);
        this.username = username;
        this.action = action;
        this.tableName = tableName;
        this.detail = detail;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getTableName() {
        return tableName;
    }

    public String getDetail() {
        return detail;
    }
}
