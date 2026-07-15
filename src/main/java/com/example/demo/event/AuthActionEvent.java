package com.example.demo.event;

/**
 * Event cho các hành động xác thực:
 * LOGIN, LOGOUT, LOGIN_FAILED, CHANGE_PASSWORD.
 *
 * Thêm thuộc tính userAgent để Admin biết user đang dùng
 * thiết bị/trình duyệt nào → Hữu ích để phát hiện truy cập bất thường
 * (ví dụ: cùng account nhưng userAgent khác hoàn toàn).
 *
 * isSuccess = false dùng cho: LOGIN_FAILED (sai mật khẩu, tài khoản bị khóa...)
 * isSuccess = true  dùng cho: LOGIN thành công, LOGOUT, CHANGE_PASSWORD thành công.
 */
public class AuthActionEvent extends BaseAuditEvent {

    /**
     * Giá trị từ header "User-Agent".
     * Ví dụ: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124..."
     *
     * Trích xuất TẠI HTTP THREAD trong Aspect trước publishEvent()
     * để tránh Context Loss khi sang Async Thread.
     */
    private final String userAgent;

    /**
     * @param source       Đối tượng gọi publishEvent (thường là Aspect instance)
     * @param action       "LOGIN" | "LOGOUT" | "LOGIN_FAILED" | "CHANGE_PASSWORD"
     * @param username     Tên tài khoản (lấy từ SecurityContextHolder)
     * @param ipAddress    IP thực của client
     * @param userAgent    Chuỗi User-Agent từ HTTP header
     * @param isSuccess    true nếu hành động thành công
     * @param errorMessage Mô tả lỗi nếu isSuccess = false, null nếu thành công
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
