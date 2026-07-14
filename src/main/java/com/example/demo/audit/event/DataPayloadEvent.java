package com.example.demo.audit.event;

/**
 * ============================================================
 * Event cho các hành động thay đổi dữ liệu: CREATE, UPDATE, DELETE.
 * ============================================================
 *
 * CHIẾN LƯỢC: LƯU REQUEST PAYLOAD (thay vì Object Diff)
 * -------------------------------------------------------
 * Thay vì deep-clone object trước/sau rồi diff (phức tạp, tốn RAM),
 * chúng ta đơn giản lưu lại chuỗi JSON của dữ liệu client gửi lên.
 *
 * ƯU ĐIỂM:
 *   1. ĐƠNG GIẢN: Không cần clone, không cần reflection, không cần
 *      xử lý Lazy Loading của JPA.
 *   2. HIỆU NĂNG: Không serialize/deserialize 2 lần. JSON payload
 *      đã có sẵn từ request body.
 *   3. MINH BẠCH: Lưu đúng những gì client GỬI LÊN — không phải
 *      những gì server lưu xuống (có thể đã bị biến đổi bởi logic).
 *   4. AN TOÀN VỚI ASYNC: String là immutable, không lo race condition
 *      khi Listener xử lý trên Thread khác.
 *
 * NHƯỢC ĐIỂM CẦN LƯU Ý:
 *   - Không biết chính xác field nào thay đổi trong UPDATE
 *     (chỉ biết toàn bộ payload được gửi lên).
 *   - Nếu cần diff chi tiết, cần thêm "oldSnapshot" từ DB.
 * ============================================================
 */
public class DataPayloadEvent extends BaseAuditEvent {

    /**
     * Chuỗi JSON đại diện cho dữ liệu client gửi lên (Request Body).
     *
     * Ví dụ khi tạo Contact:
     *   {"name":"Nguyễn Văn A","email":"a@gmail.com","title":"Hỏi giá","content":"..."}
     *
     * Ví dụ khi Reply (UPDATE) Contact:
     *   {"status":"DONE","reply":"Đã liên hệ thành công"}
     *
     * Ví dụ khi DELETE:
     *   {"id": 42}  hoặc chỉ lưu "Xóa bản ghi ID=42"
     *
     * QUAN TRỌNG: Aspect phải lọc bỏ các field nhạy cảm
     * (password, token...) TRƯỚC KHI serialize vào chuỗi này.
     */
    private final String requestPayload;

    /**
     * @param source         Đối tượng gọi publishEvent (Aspect instance)
     * @param action         "CREATE" | "UPDATE" | "DELETE"
     * @param tableName      Tên bảng bị tác động. Ví dụ: "contacts", "projects"
     * @param username       Tên nhân viên/user thực hiện
     * @param ipAddress      IP thực của client
     * @param requestPayload Chuỗi JSON payload từ request (đã lọc sensitive fields)
     * @param isSuccess      true nếu thao tác DB thành công
     * @param errorMessage   Mô tả lỗi nếu thất bại, null nếu thành công
     */
    public DataPayloadEvent(Object  source,
                            String  action,
                            String  tableName,
                            String  username,
                            String  ipAddress,
                            String  requestPayload,
                            boolean isSuccess,
                            String  errorMessage) {
        super(source, action, tableName, username, ipAddress, isSuccess, errorMessage);
        this.requestPayload = requestPayload;
    }

    public String getRequestPayload() { return requestPayload; }
}
