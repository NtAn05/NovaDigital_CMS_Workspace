package com.example.demo.service;

import com.example.demo.event.AuthActionEvent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AuditService: Central service for log recording.
 * Called directly from Controllers to avoid AOP Proxy issues.
 */
@Service
public class AuditService {

    private final ApplicationEventPublisher eventPublisher;

    public AuditService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Records Auth action logs (Login / Logout).
     * Call this method in Controller immediately after successful login/logout.
     *
     * @param action   Example: "LOGIN", "LOGOUT"
     * @param username Username of user performing the action
     */
    public void logAuthAction(String action, String username) {
        try {
            HttpServletRequest request = getHttpServletRequest();
            String ipAddress = resolveClientIp(request);
            String userAgent = (request != null) ? request.getHeader("User-Agent") : "Unknown";

            AuthActionEvent event = new AuthActionEvent(this, action, username, ipAddress, userAgent, true, null);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // Logging error must not affect the main execution flow
            e.printStackTrace();
        }
    }

    /**
     * Utility: Get current username from SecurityContext.
     * Used when username is not readily available (e.g., during logout).
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // ignore
        }
        return "UNKNOWN";
    }

    // ─── Private Helpers ───────────────────────────────────────────────────────

    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return (attributes != null) ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return "Unknown";
        // Prioritize X-Forwarded-For header (when behind Nginx/Proxy)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For may contain multiple IPs, take the first IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
