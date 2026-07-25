package com.example.demo.aspect;

import com.example.demo.annotation.Auditable;
import com.example.demo.event.AuthActionEvent;
import com.example.demo.event.DataPayloadEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ============================================================
 * AuditAspect — Interceptor Layer
 * ============================================================
 *
 * SOLE RESPONSIBILITY:
 *   1. Intercept methods annotated with @Auditable.
 *   2. Extract Username, IP from ThreadLocal BEFORE publishEvent().
 *   3. Serialize the first argument of the method into a JSON payload.
 *   4. Publish Event (Fire-and-Forget) — DO NOT save directly to DB.
 *
 * CONTEXT LOSS RESOLUTION:
 *   SecurityContextHolder & RequestContextHolder use ThreadLocal.
 *   They only exist in the current HTTP Thread.
 *   → MUST extract before publishEvent() sends to Async Thread.
 *   → After Event creation, Async Thread only reads from Event object
 *     (no longer needs ThreadLocal → 100% safe).
 *
 * LOGGING STRATEGY:
 *   Save REQUEST PAYLOAD (JSON of first argument) instead of Object Diff.
 *   Payload has sensitive fields filtered out before serialization.
 * ============================================================
 */
@Aspect
@Component
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper              objectMapper;

    /** List of sensitive fields that will be masked in payload */
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(
            Arrays.asList("password", "token", "secret", "accessToken", "refreshToken", "otp")
    );

    public AuditAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.objectMapper   = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(
                com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    /**
     * Pointcut: Intercept ALL methods annotated with @Auditable.
     * @Around = run code BOTH BEFORE and AFTER the original method.
     */
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint,
                        Auditable auditable) throws Throwable {

        // ═══════════════════════════════════════════════════
        // STEP 1: EXTRACT CONTEXT (REQUIRED BEFORE PROCEED)
        // Reason: After proceed(), transaction may commit and Spring
        // may clear SecurityContext in some configurations.
        // ═══════════════════════════════════════════════════

        String username  = extractUsername();   // From SecurityContextHolder (ThreadLocal)
        String ipAddress = extractIpAddress();  // From RequestContextHolder  (ThreadLocal)
        String action    = auditable.action();
        String tableName = auditable.table();

        // ═══════════════════════════════════════════════════
        // STEP 2: SERIALIZE PAYLOAD BEFORE PROCEED AND CAPTURE OLD STATE
        // ═══════════════════════════════════════════════════
        String requestPayload = serializePayload(joinPoint.getArgs());
        Object oldState = null;
        Object[] args = joinPoint.getArgs();
        if (!auditable.isAuth() && args != null && args.length > 0 && args[0] != null) {
            try {
                String json = objectMapper.writeValueAsString(args[0]);
                oldState = objectMapper.readValue(json, java.util.Map.class);
            } catch (Exception e) {
                oldState = args[0];
            }
        }

        // ═══════════════════════════════════════════════════
        // STEP 3: CALL ACTUAL METHOD
        // ═══════════════════════════════════════════════════

        boolean isSuccess    = true;
        String  errorMessage = null;
        Object  result       = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            isSuccess    = false;
            errorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            throw ex; // Re-throw for caller to handle as normal
        } finally {
            // ═══════════════════════════════════════════════
            // STEP 4: PUBLISH EVENT (ALWAYS RUNS, WHETHER SUCCESS OR FAIL)
            // ═══════════════════════════════════════════════
            if (auditable.isAuth()) {
                publishAuthEvent(action, username, ipAddress, isSuccess, errorMessage);
            } else {
                String diffDetail = null;
                if (isSuccess) {
                    Object newState = result != null ? result : (args != null && args.length > 0 ? args[0] : null);
                    if ("CREATE".equalsIgnoreCase(action)) {
                        diffDetail = com.example.demo.utils.JsonDiffUtils.getDiff(null, newState);
                    } else if ("DELETE".equalsIgnoreCase(action)) {
                        diffDetail = com.example.demo.utils.JsonDiffUtils.getDiff(oldState, null);
                    } else {
                        diffDetail = com.example.demo.utils.JsonDiffUtils.getDiff(oldState, newState);
                    }
                } else {
                    diffDetail = requestPayload;
                }
                publishDataEvent(action, tableName, username, ipAddress,
                                 diffDetail != null ? diffDetail : "[]", isSuccess, errorMessage);
            }
        }

        return result;
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE: PUBLISH METHODS
    // ─────────────────────────────────────────────────────

    private void publishAuthEvent(String action, String username, String ipAddress,
                                  boolean isSuccess, String errorMessage) {
        // User-Agent is still in HTTP Thread at this time (in finally)
        String userAgent = extractUserAgent();

        AuthActionEvent event = new AuthActionEvent(
                this, action, username, ipAddress, userAgent, isSuccess, errorMessage
        );
        eventPublisher.publishEvent(event); // Fire-and-Forget → Async Thread
    }

    private void publishDataEvent(String action, String tableName,
                                  String username, String ipAddress,
                                  String requestPayload, boolean isSuccess, String errorMessage) {
        DataPayloadEvent event = new DataPayloadEvent(
                this, action, tableName, username, ipAddress,
                requestPayload, isSuccess, errorMessage
        );
        eventPublisher.publishEvent(event); // Fire-and-Forget → Async Thread
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE: SERIALIZE PAYLOAD
    // ─────────────────────────────────────────────────────

    /**
     * Serialize the first argument of the method to a JSON string.
     * Automatically filters out sensitive fields (password, token...).
     *
     * @return JSON string, or simple description if cannot be serialized
     */
    private String serializePayload(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            return "(no payload)";
        }

        try {
            // Serialize object to JSON tree to allow sensitive field removal
            ObjectNode node = objectMapper.valueToTree(args[0]);

            // Remove sensitive fields before saving
            SENSITIVE_FIELDS.forEach(node::remove);

            String json = objectMapper.writeValueAsString(node);

            // Truncate payload length to save DB memory
            return json.length() > 1000 ? json.substring(0, 997) + "..." : json;

        } catch (Exception e) {
            // Safe fallback if object cannot be serialized (e.g. primitive, Long ID)
            return "arg[0]=" + args[0].toString();
        }
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE: EXTRACT CONTEXT FROM THREADLOCAL
    // ─────────────────────────────────────────────────────

    /**
     * Extract username from Spring Security.
     * MUST be called from HTTP Thread — SecurityContextHolder uses ThreadLocal.
     */
    private String extractUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null
                    && auth.isAuthenticated()
                    && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
                return auth.getName();
            }
        } catch (Exception ignored) { /* Safe fallback */ }
        return "SYSTEM";
    }

    /**
     * Extract actual client IP.
     * Supports systems behind Reverse Proxy via X-Forwarded-For.
     * MUST be called from HTTP Thread — RequestContextHolder uses ThreadLocal.
     */
    private String extractIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "INTERNAL";

            HttpServletRequest request = attrs.getRequest();

            // X-Forwarded-For: "clientIP, proxy1, proxy2" → take first IP
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ignored) {
            return "UNKNOWN";
        }
    }

    /**
     * Extract User-Agent header.
     * MUST be called from HTTP Thread — used specifically for Auth Event.
     */
    private String extractUserAgent() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "Unknown";
            String ua = attrs.getRequest().getHeader("User-Agent");
            return ua != null ? ua : "Unknown";
        } catch (Exception ignored) {
            return "Unknown";
        }
    }
}
