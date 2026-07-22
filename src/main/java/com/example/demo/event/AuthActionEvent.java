package com.example.demo.event;

/**
 * Event for authentication actions:
 * LOGIN, LOGOUT, LOGIN_FAILED, CHANGE_PASSWORD.
 *
 * Adds userAgent property so Admin knows which device/browser the user is using
 * → Useful for detecting anomalous access (e.g., same account but completely different userAgent).
 *
 * isSuccess = false used for: LOGIN_FAILED (wrong password, account locked...)
 * isSuccess = true  used for: successful LOGIN, LOGOUT, successful CHANGE_PASSWORD.
 */
public class AuthActionEvent extends BaseAuditEvent {

    /**
     * Value from "User-Agent" header.
     * Example: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124..."
     *
     * Extracted IN HTTP THREAD within Aspect before publishEvent()
     * to prevent Context Loss when switching to Async Thread.
     */
    private final String userAgent;

    /**
     * @param source       Object publishing event (usually Aspect instance)
     * @param action       "LOGIN" | "LOGOUT" | "LOGIN_FAILED" | "CHANGE_PASSWORD"
     * @param username     Username (retrieved from SecurityContextHolder)
     * @param ipAddress    Actual client IP
     * @param userAgent    User-Agent string from HTTP header
     * @param isSuccess    true if the action succeeded
     * @param errorMessage Error description if isSuccess = false, null if successful
     */
    public AuthActionEvent(Object  source,
                           String  action,
                           String  username,
                           String  ipAddress,
                           String  userAgent,
                           boolean isSuccess,
                           String  errorMessage) {
        super(source, action, null, username, ipAddress, isSuccess, errorMessage);
        this.userAgent = userAgent;
    }

    public String getUserAgent() { return userAgent; }
}
