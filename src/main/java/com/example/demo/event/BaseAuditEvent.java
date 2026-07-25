package com.example.demo.event;

import org.springframework.context.ApplicationEvent;
import java.time.Instant;

/**
 * ============================================================
 * Abstract superclass for the entire Audit Event system.
 * ============================================================
 *
 * DESIGN: All Events inherit from this class to ensure
 * all audit records contain a complete set of common fields.
 *
 * ABOUT isSuccess & errorMessage FIELDS:
 * -----------------------------------------
 * Purpose: Serve Error Tracking.
 *
 * REAL-WORLD EXAMPLES:
 *   - When a user enters the wrong password 5 times → action = "LOGIN",
 *     isSuccess = false, errorMessage = "Bad credentials (5th attempt)"
 *   - When database write fails (deadlock...) → action = "CREATE_CONTACT",
 *     isSuccess = false, errorMessage = "DataAccessException: Deadlock found"
 *
 * WHY IS THIS IMPORTANT?
 *   1. SECURITY: Detect Brute Force attacks via statistics on
 *      isSuccess=false events from the same username/IP.
 *   2. DEBUGGING: Admins immediately see which events failed to investigate.
 *   3. COMPLIANCE: Security standards (ISO 27001, PCI-DSS)
 *      require recording FAILED actions, not just successes.
 *   4. ALERTING: Monitoring systems can easily filter isSuccess=false
 *      to send automatic alerts via email/Slack.
 * ============================================================
 */
public abstract class BaseAuditEvent extends ApplicationEvent {

    /** Action name. E.g., "LOGIN", "CREATE_CONTACT", "UPDATE_PROJECT" */
    private final String action;

    /** Target table/resource name. Null if Auth action. */
    private final String tableName;

    /** Username performing the action (extracted from SecurityContextHolder). */
    private final String username;

    /** Actual client IP (extracted from RequestContextHolder, supports X-Forwarded-For). */
    private final String ipAddress;

    /**
     * Time when the event occurred — captured in HTTP Thread BEFORE publishEvent().
     * Reason: If recorded in Async Thread, it will be delayed a few ms due to ThreadPool queue.
     */
    private final Instant timestamp;

    /**
     * Result of the action: true = success, false = failure.
     *
     * REASONS FOR SAVING THIS FIELD:
     * → Allows Admins to filter failed actions separately for investigation.
     * → Detect anomalies: Multiple isSuccess=false in a short time
     *   from the same IP = sign of attack.
     * → Meet Compliance requirements: Record both errors and successes completely.
     */
    private final boolean isSuccess;

    /**
     * Error message if isSuccess = false. Null when successful.
     *
     * REASONS FOR SAVING ERRORTYPE INSTEAD OF FULL STACKTRACE:
     * → Full stacktrace may contain sensitive infrastructure information.
     * → Only saving Exception name + concise message is sufficient for investigation.
     * → Example: "BadCredentialsException: Invalid password"
     */
    private final String errorMessage;

    /**
     * Full Constructor — used when execution result is known (after proceed()).
     */
    protected BaseAuditEvent(Object  source,
                              String  action,
                              String  tableName,
                              String  username,
                              String  ipAddress,
                              boolean isSuccess,
                              String  errorMessage) {
        super(source);
        this.action       = action;
        this.tableName    = tableName;
        this.username     = username;
        this.ipAddress    = ipAddress;
        this.timestamp    = Instant.now();
        this.isSuccess    = isSuccess;
        this.errorMessage = errorMessage;
    }

    // ── Getters ──────────────────────────────────────────────────

    public String  getAction()            { return action; }
    public String  getTableName()         { return tableName; }
    public String  getUsername()          { return username; }
    public String  getIpAddress()         { return ipAddress; }
    /** Use getAuditTimestamp() instead of getTimestamp() because ApplicationEvent already has a final getTimestamp() */
    public Instant getAuditTimestamp()    { return timestamp; }
    public boolean isSuccess()            { return isSuccess; }
    public String  getErrorMessage()      { return errorMessage; }
}
