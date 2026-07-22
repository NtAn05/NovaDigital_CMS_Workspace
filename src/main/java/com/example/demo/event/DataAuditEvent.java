package com.example.demo.event;

/**
 * Event for data modification actions (Object Diff strategy - legacy).
 * Retained for backward compatibility with existing code using Object Diff strategy.
 * New architecture uses DataPayloadEvent instead.
 */
public class DataAuditEvent extends BaseAuditEvent {

    private final String detail; // JSON Diff string or readable summary

    /**
     * Backward-compatible constructor (legacy) - 5 parameters.
     * Default: isSuccess = true, errorMessage = null.
     */
    public DataAuditEvent(Object source, String username, String action,
                          String tableName, String detail) {
        super(source, action, tableName, username, null, true, null);
        this.detail = detail;
    }

    /**
     * New full constructor - 7 parameters.
     */
    public DataAuditEvent(Object source, String username, String action,
                          String tableName, String detail,
                          boolean isSuccess, String errorMessage) {
        super(source, action, tableName, username, null, isSuccess, errorMessage);
        this.detail = detail;
    }

    public String getDetail() { return detail; }
}
