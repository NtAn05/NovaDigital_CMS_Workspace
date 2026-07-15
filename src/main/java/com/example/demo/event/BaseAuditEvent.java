package com.example.demo.event;

import org.springframework.context.ApplicationEvent;
import java.time.Instant;

/**
 * ============================================================
 * Lớp cha trừu tượng cho toàn bộ hệ thống Audit Event.
 * ============================================================
 *
 * THIẾT KẾ: Tất cả Event đều kế thừa lớp này để đảm bảo
 * mọi bản ghi audit đều có đầy đủ các trường chung.
 *
 * VỀ TRƯỜNG isSuccess & errorMessage:
 * -----------------------------------------
 * Mục đích: Phục vụ việc TRACK LỖI (Error Tracking).
 *
 * VÍ DỤ THỰC TẾ:
 *   - Khi user nhập sai mật khẩu 5 lần → action = "LOGIN",
 *     isSuccess = false, errorMessage = "Bad credentials (5th attempt)"
 *   - Khi ghi dữ liệu bị lỗi DB (deadlock...) → action = "CREATE_CONTACT",
 *     isSuccess = false, errorMessage = "DataAccessException: Deadlock found"
 *
 * VÌ SAO QUAN TRỌNG?
 *   1. SECURITY: Phát hiện tấn công Brute Force qua thống kê
 *      các event isSuccess=false của cùng một username/IP.
 *   2. DEBUGGING: Admin thấy ngay sự kiện nào thất bại để điều tra.
 *   3. COMPLIANCE: Các tiêu chuẩn bảo mật (ISO 27001, PCI-DSS)
 *      yêu cầu ghi nhận cả các hành động THẤT BẠI, không chỉ thành công.
 *   4. ALERTING: Hệ thống monitoring dễ dàng lọc isSuccess=false
 *      để gửi cảnh báo tự động qua email/Slack.
 * ============================================================
 */
public abstract class BaseAuditEvent extends ApplicationEvent {

    /** Tên hành động. Ví dụ: "LOGIN", "CREATE_CONTACT", "UPDATE_PROJECT" */
    private final String action;

    /** Tên bảng/tài nguyên bị tác động. Null nếu là Auth action. */
    private final String tableName;

    /** Tên người dùng thực hiện hành động (đã trích từ SecurityContextHolder). */
    private final String username;

    /** IP thực của client (đã trích từ RequestContextHolder, hỗ trợ X-Forwarded-For). */
    private final String ipAddress;

    /**
     * Thời điểm sự kiện xảy ra — ghi nhận tại HTTP Thread TRƯỚC publishEvent().
     * Lý do: Nếu ghi trong Async Thread, sẽ bị trễ vài ms do hàng đợi ThreadPool.
     */
    private final Instant timestamp;

    /**
     * Kết quả của hành động: true = thành công, false = thất bại.
     *
     * LÝ DO CẦN LƯU TRƯỜNG NÀY:
     * → Cho phép Admin lọc riêng các hành động thất bại để điều tra.
     * → Phát hiện bất thường: Nhiều isSuccess=false trong thời gian ngắn
     *   từ cùng một IP = dấu hiệu tấn công.
     * → Đáp ứng yêu cầu Compliance: Ghi nhận đầy đủ cả lỗi lẫn thành công.
     */
    private final boolean isSuccess;

    /**
     * Thông báo lỗi nếu isSuccess = false. Null khi thành công.
     *
     * LÝ DO LƯU ERRORTYPE THAY VÌ FULL STACKTRACE:
     * → Full stacktrace có thể chứa thông tin nhạy cảm về hạ tầng.
     * → Chỉ lưu tên Exception + message ngắn gọn là đủ để điều tra.
     * → Ví dụ: "BadCredentialsException: Mật khẩu không chính xác"
     */
    private final String errorMessage;

    /**
     * Constructor đầy đủ — dùng khi biết kết quả thực thi (sau proceed()).
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
    /** Dùng getAuditTimestamp() thay vì getTimestamp() vì ApplicationEvent đã có final getTimestamp() */
    public Instant getAuditTimestamp()    { return timestamp; }
    public boolean isSuccess()            { return isSuccess; }
    public String  getErrorMessage()      { return errorMessage; }
}
