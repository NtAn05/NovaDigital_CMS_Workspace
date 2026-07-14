package com.example.demo.audit.service;

import com.example.demo.audit.event.AuthActionEvent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AuditService: Service trung tâm để ghi log.
 * Gọi trực tiếp từ Controller để tránh mọi vấn đề AOP Proxy.
 */
@Service
public class AuditService {

    private final ApplicationEventPublisher eventPublisher;

    public AuditService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Ghi log hành động Auth (Login / Logout).
     * Gọi method này trong Controller ngay sau khi login/logout thành công.
     *
     * @param action   Ví dụ: "LOGIN", "LOGOUT"
     * @param username Tên người dùng thực hiện hành động
     */
    public void logAuthAction(String action, String username) {
        try {
            HttpServletRequest request = getHttpServletRequest();
            String ipAddress = resolveClientIp(request);
            String userAgent = (request != null) ? request.getHeader("User-Agent") : "Unknown";

            AuthActionEvent event = new AuthActionEvent(this, action, username, ipAddress, userAgent, true, null);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // Lỗi ghi log không được phép ảnh hưởng luồng chính
            e.printStackTrace();
        }
    }

    /**
     * Tiện ích: Lấy username hiện tại từ SecurityContext.
     * Dùng khi không có sẵn username (ví dụ: khi logout).
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
        // Ưu tiên header X-Forwarded-For (khi đứng sau Nginx/Proxy)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
