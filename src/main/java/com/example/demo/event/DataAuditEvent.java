package com.example.demo.event;

/**
 * Event cho các hành động thay đổi dữ liệu (Object Diff strategy - legacy).
 * Giữ lại để tương thích ngược với code cũ đang dùng chiến lược Object Diff.
 * Kiến trúc mới dùng DataPayloadEvent thay thế.
 */
public class DataAuditEvent extends BaseAuditEvent {

    private final String detail; // JSON Diff string hoặc readable summary

    /**
     * Constructor tương thích ngược (legacy) - 5 tham số.
     * Mặc định: isSuccess = true, errorMessage = null.
     */
    public DataAuditEvent(Object source, String username, String action,
                          String tableName, String detail) {
        super(source, action, tableName, username, null, true, null);
        this.detail = detail;
    }

    /**
     * Constructor đầy đủ mới - 7 tham số.
     */
    public DataAuditEvent(Object source, String username, String action,
                          String tableName, String detail,
                          boolean isSuccess, String errorMessage) {
        super(source, action, tableName, username, null, isSuccess, errorMessage);
        this.detail = detail;
    }

    public String getDetail() { return detail; }
}
