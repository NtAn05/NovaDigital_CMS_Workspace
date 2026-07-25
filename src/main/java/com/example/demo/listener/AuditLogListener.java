package com.example.demo.listener;

import com.example.demo.entity.AuthLog;
import com.example.demo.entity.DataAuditLog;
import com.example.demo.event.AuthActionEvent;
import com.example.demo.event.BaseAuditEvent;
import com.example.demo.event.DataAuditEvent;
import com.example.demo.event.DataPayloadEvent;
import com.example.demo.repository.AuthLogRepository;
import com.example.demo.repository.DataAuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;



/**
 * ============================================================
 * AuditLogListener — Persistence Layer
 * ============================================================
 *
 * PRINCIPLE: This class has a SINGLE responsibility:
 *   Receive Event → Map to Entity → Call Repository to save DB.
 *
 * RUN ASYNCHRONOUSLY (@Async):
 *   → Does not slow down user HTTP Requests.
 *   → Runs on dedicated thread from "auditExecutor" ThreadPool.
 *
 * ABOUT isSuccess in log:
 *   → Listener saves FAILED events to DB as well.
 *   → Admin can query WHERE is_success = 0 to view errors.
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
     * Listens for all BaseAuditEvent (polymorphic).
     * Dispatches processing based on the specific Event type.
     *
     * CHECK ORDER: DataPayloadEvent before DataAuditEvent
     * because DataPayloadEvent is a newer architecture and takes priority.
     */
    @EventListener
    @Async("auditExecutor")
    public void handleAuditLogEvent(BaseAuditEvent event) {
        try {
            if (event instanceof AuthActionEvent authEvent) {
                // Branch: Record Auth Log (Login, Logout, Login_Failed...)
                saveAuthLog(authEvent);

            } else if (event instanceof DataPayloadEvent payloadEvent) {
                // Branch: Record Data Log following Request Payload strategy
                saveDataLogFromPayload(payloadEvent);

            } else if (event instanceof DataAuditEvent dataEvent) {
                // Branch: Record Data Log following old strategy (Object Diff)
                // Retained for backward compatibility with older @Auditable usages
                saveDataLogFromDiff(dataEvent);
            }
        } catch (Exception e) {
            // DO NOT re-throw Exception — prevent crashing Async Thread
            // In production should be replaced with Logger:
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
        // Build detail content from JSON payload
        // Format: "isSuccess=true | Create new: {name: ...}" or "isSuccess=false | Error: ..."
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
     * Builds a concise description string to save into the "detail" column in the DB.
     *
     * Example format:
     *   ✅ Success → "Create new: {name: 'A', email: 'a@b.com'}"
     *   ❌ Failure → "[FAILED] BadCredentialsException: Invalid password | {email: 'a@b.com'}"
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

        // Truncate length to avoid overflowing TEXT column
        return detail.length() > 2000 ? detail.substring(0, 1997) + "..." : detail;
    }
}
