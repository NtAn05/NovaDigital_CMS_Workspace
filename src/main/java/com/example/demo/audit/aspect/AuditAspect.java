package com.example.demo.audit.aspect;

import com.example.demo.audit.annotation.Auditable;
import com.example.demo.audit.event.AuthActionEvent;
import com.example.demo.audit.event.DataPayloadEvent;
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
 * AuditAspect — Lớp Chặn & Thu Thập (Interceptor Layer)
 * ============================================================
 *
 * TRÁCH NHIỆM DUY NHẤT:
 *   1. Chặn method được đánh dấu @Auditable.
 *   2. Trích xuất Username, IP từ ThreadLocal TRƯỚC publishEvent().
 *   3. Serialize đối số đầu tiên của method thành JSON payload.
 *   4. Bắn Event (Fire-and-Forget) — KHÔNG lưu DB trực tiếp.
 *
 * GIẢI QUYẾT CONTEXT LOSS:
 *   SecurityContextHolder & RequestContextHolder dùng ThreadLocal.
 *   Chúng chỉ tồn tại trong HTTP Thread hiện tại.
 *   → PHẢI trích xuất trước khi publishEvent() bắn sang Async Thread.
 *   → Sau khi Event được tạo, Async Thread chỉ đọc từ Event object
 *     (không cần ThreadLocal nữa → an toàn 100%).
 *
 * CHIẾN LƯỢC GHI LOG:
 *   Lưu REQUEST PAYLOAD (JSON của argument đầu tiên) thay vì Object Diff.
 *   Payload được lọc bỏ các field nhạy cảm trước khi serialize.
 * ============================================================
 */
@Aspect
@Component
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper              objectMapper;

    /** Danh sách field nhạy cảm sẽ bị che (masking) trong payload */
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
     * Pointcut: Chặn TẤT CẢ method được gắn @Auditable.
     * @Around = chạy code VỪA TRƯỚC vừa SAU method gốc.
     */
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint,
                        Auditable auditable) throws Throwable {

        // ═══════════════════════════════════════════════════
        // BƯỚC 1: TRÍCH XUẤT CONTEXT (BẮT BUỘC TRƯỚC PROCEED)
        // Lý do: Sau proceed(), transaction có thể commit, Spring
        // có thể clear SecurityContext trong một số cấu hình.
        // ═══════════════════════════════════════════════════

        String username  = extractUsername();   // Từ SecurityContextHolder (ThreadLocal)
        String ipAddress = extractIpAddress();  // Từ RequestContextHolder  (ThreadLocal)
        String action    = auditable.action();
        String tableName = auditable.table();

        // ═══════════════════════════════════════════════════
        // BƯỚC 2: SERIALIZE PAYLOAD TRƯỚC KHI PROCEED VÀ CHỤP TRẠNG THÁI CŨ
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
        // BƯỚC 3: GỌI METHOD THỰC TẾ
        // ═══════════════════════════════════════════════════

        boolean isSuccess    = true;
        String  errorMessage = null;
        Object  result       = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            isSuccess    = false;
            errorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            throw ex; // Ném lại để caller xử lý bình thường
        } finally {
            // ═══════════════════════════════════════════════
            // BƯỚC 4: BẮN EVENT (LUÔN CHẠY, DÙ SUCCESS/FAIL)
            // ═══════════════════════════════════════════════
            if (auditable.isAuth()) {
                publishAuthEvent(action, username, ipAddress, isSuccess, errorMessage);
            } else {
                String diffDetail = null;
                if (isSuccess) {
                    Object newState = result != null ? result : (args != null && args.length > 0 ? args[0] : null);
                    if ("CREATE".equalsIgnoreCase(action)) {
                        diffDetail = com.example.demo.audit.util.JsonDiffUtils.getDiff(null, newState);
                    } else if ("DELETE".equalsIgnoreCase(action)) {
                        diffDetail = com.example.demo.audit.util.JsonDiffUtils.getDiff(oldState, null);
                    } else {
                        diffDetail = com.example.demo.audit.util.JsonDiffUtils.getDiff(oldState, newState);
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
        // User-Agent vẫn còn trong HTTP Thread lúc này (trong finally)
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
     * Serialize argument đầu tiên của method thành chuỗi JSON.
     * Tự động lọc bỏ các field nhạy cảm (password, token...).
     *
     * @return Chuỗi JSON, hoặc mô tả đơn giản nếu không serialize được
     */
    private String serializePayload(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            return "(no payload)";
        }

        try {
            // Serialize object thành JSON tree để có thể xóa field nhạy cảm
            ObjectNode node = objectMapper.valueToTree(args[0]);

            // Xóa bỏ các field nhạy cảm trước khi lưu
            SENSITIVE_FIELDS.forEach(node::remove);

            String json = objectMapper.writeValueAsString(node);

            // Giới hạn độ dài payload để tiết kiệm bộ nhớ DB
            return json.length() > 1000 ? json.substring(0, 997) + "..." : json;

        } catch (Exception e) {
            // Fallback an toàn nếu object không serialize được (ví dụ: primitive, Long ID)
            return "arg[0]=" + args[0].toString();
        }
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE: TRÍCH XUẤT CONTEXT TỪ THREADLOCAL
    // ─────────────────────────────────────────────────────

    /**
     * Trích xuất tên người dùng từ Spring Security.
     * PHẢI gọi từ HTTP Thread — SecurityContextHolder dùng ThreadLocal.
     */
    private String extractUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null
                    && auth.isAuthenticated()
                    && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
                return auth.getName();
            }
        } catch (Exception ignored) { /* Fallback an toàn */ }
        return "SYSTEM";
    }

    /**
     * Trích xuất IP thực của client.
     * Hỗ trợ hệ thống đứng sau Reverse Proxy qua X-Forwarded-For.
     * PHẢI gọi từ HTTP Thread — RequestContextHolder dùng ThreadLocal.
     */
    private String extractIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "INTERNAL";

            HttpServletRequest request = attrs.getRequest();

            // X-Forwarded-For: "clientIP, proxy1, proxy2" → lấy IP đầu tiên
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
     * Trích xuất User-Agent header.
     * PHẢI gọi từ HTTP Thread — dùng riêng cho Auth Event.
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
