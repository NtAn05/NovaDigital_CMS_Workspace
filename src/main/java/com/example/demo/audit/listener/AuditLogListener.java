package com.example.demo.audit.listener;

import com.example.demo.audit.entity.AuthLog;
import com.example.demo.audit.entity.DataAuditLog;
import com.example.demo.audit.event.AuthActionEvent;
import com.example.demo.audit.event.BaseAuditEvent;
import com.example.demo.audit.event.DataAuditEvent;
import com.example.demo.audit.event.DataPayloadEvent;
import com.example.demo.audit.repository.AuthLogRepository;
import com.example.demo.audit.repository.DataAuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;



/**
 * ============================================================
 * AuditLogListener — Lớp Lưu Trữ (Persistence Layer)
 * ============================================================
 *
 * NGUYÊN TẮC: Class này CHỈ làm nhiệm vụ DUY NHẤT:
 *   Nhận Event → Ánh xạ sang Entity → Gọi Repository lưu DB.
 *
 * CHẠY BẤT ĐỒNG BỘ (@Async):
 *   → Không làm chậm HTTP Request của người dùng.
 *   → Chạy trên Thread riêng từ "auditExecutor" ThreadPool.
 *
 * VỀ isSuccess trong log:
 *   → Listener lưu cả event THẤT BẠI vào DB.
 *   → Admin có thể query WHERE is_success = 0 để xem các lỗi.
 * ============================================================
 */
@Component
public class AuditLogListener {

    private final DataAuditLogRepository dataAuditLogRepository;
    private final AuthLogRepository      authLogRepository;

    public AuditLogListener(DataAuditLogRepository dataAuditLogRepository,
                            AuthLogRepository authLogRepository) {
        this.dataAuditLogRepository = dataAuditLogRepository;
        this.authLogRepository      = authLogRepository;
    }

    /**
     * Lắng nghe tất cả BaseAuditEvent (đa hình).
     * Phân nhánh xử lý theo kiểu cụ thể của Event.
     *
     * THỨ TỰ KIỂM TRA: DataPayloadEvent trước DataAuditEvent
     * vì DataPayloadEvent là kiến trúc mới hơn và ưu tiên hơn.
     */
    @EventListener
    @Async("auditExecutor")
    public void handleAuditLogEvent(BaseAuditEvent event) {
        try {
            if (event instanceof AuthActionEvent authEvent) {
                // Nhánh: Ghi Auth Log (Login, Logout, Login_Failed...)
                saveAuthLog(authEvent);

            } else if (event instanceof DataPayloadEvent payloadEvent) {
                // Nhánh: Ghi Data Log theo chiến lược lưu Request Payload
                saveDataLogFromPayload(payloadEvent);

            } else if (event instanceof DataAuditEvent dataEvent) {
                // Nhánh: Ghi Data Log theo chiến lược cũ (Object Diff)
                // Giữ lại để tương thích ngược với các @Auditable cũ còn dùng
                saveDataLogFromDiff(dataEvent);
            }
        } catch (Exception e) {
            // KHÔNG ném lại Exception — tránh làm crash Async Thread
            // Trong production nên thay bằng Logger:
            // log.error("[AuditLogListener] Failed to save audit log", e);
            e.printStackTrace();
        }
    }

    // ── PRIVATE HANDLERS ──────────────────────────────────────────

    private void saveAuthLog(AuthActionEvent event) {
        AuthLog log = new AuthLog(
                event.getUsername(),
                event.getAction(),
                event.getIpAddress(),
                event.getUserAgent()
        );
        authLogRepository.save(log);
    }

    private void saveDataLogFromPayload(DataPayloadEvent event) {
        // Xây dựng nội dung detail từ payload JSON
        // Format: "isSuccess=true | Tạo mới: {name: ...}" hoặc "isSuccess=false | Lỗi: ..."
        String detail = buildDetailFromPayload(event);

        DataAuditLog log = new DataAuditLog(
                event.getUsername(),
                event.getAction(),
                event.getTableName(),
                detail
        );
        dataAuditLogRepository.save(log);
    }

    private void saveDataLogFromDiff(DataAuditEvent event) {
        DataAuditLog log = new DataAuditLog(
                event.getUsername(),
                event.getAction(),
                event.getTableName(),
                event.getDetail()
        );
        dataAuditLogRepository.save(log);
    }

    /**
     * Tạo chuỗi mô tả ngắn gọn để lưu vào cột "detail" trong DB.
     *
     * Format mẫu:
     *   ✅ Thành công  → "Tạo mới: {name: 'A', email: 'a@b.com'}"
     *   ❌ Thất bại    → "[FAILED] BadCredentialsException: Sai mật khẩu | {email: 'a@b.com'}"
     */
    private String buildDetailFromPayload(DataPayloadEvent event) {
        String prefix = event.isSuccess() ? "" : "[FAILED] ";
        String error  = (event.getErrorMessage() != null)
                        ? event.getErrorMessage() + " | "
                        : "";
        String payload = event.getRequestPayload() != null
                         ? event.getRequestPayload()
                         : "(no payload)";

        String detail = prefix + error + payload;

        // Giới hạn độ dài để không tràn cột TEXT
        return detail.length() > 2000 ? detail.substring(0, 1997) + "..." : detail;
    }
}
