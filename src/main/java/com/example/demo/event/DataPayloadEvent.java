package com.example.demo.event;

/**
 * ============================================================
 * Event for data modification actions: CREATE, UPDATE, DELETE.
 * ============================================================
 *
 * STRATEGY: SAVE REQUEST PAYLOAD (instead of Object Diff)
 * -------------------------------------------------------
 * Instead of deep-cloning objects before/after then diffing (complex, RAM heavy),
 * we simply save the JSON string of data sent by the client.
 *
 * ADVANTAGES:
 *   1. SIMPLE: No cloning needed, no reflection needed, no handling of JPA Lazy Loading.
 *   2. PERFORMANCE: Does not serialize/deserialize twice. JSON payload
 *      is already available from request body.
 *   3. TRANSPARENCY: Saves exactly what the client SENT — not what
 *      the server persists (which may have been mutated by logic).
 *   4. ASYNC SAFE: String is immutable, no race condition concerns
 *      when Listener processes on another Thread.
 *
 * TRADE-OFFS TO NOTE:
 *   - Does not know exactly which fields changed during UPDATE
 *     (only knows full payload sent).
 *   - If detailed diff is needed, an "oldSnapshot" from DB must be added.
 * ============================================================
 */
public class DataPayloadEvent extends BaseAuditEvent {

    /**
     * JSON string representing the data sent by the client (Request Body).
     *
     * Example when creating Contact:
     *   {"name":"Nguyen Van A","email":"a@gmail.com","title":"Price inquiry","content":"..."}
     *
     * Example when Replying (UPDATE) Contact:
     *   {"status":"DONE","reply":"Successfully contacted"}
     *
     * Example when DELETE:
     *   {"id": 42}  or simply "Deleted record ID=42"
     *
     * IMPORTANT: Aspect must filter out sensitive fields
     * (password, token...) BEFORE serializing into this string.
     */
    private final String requestPayload;

    /**
     * @param source         Object publishing event (Aspect instance)
     * @param action         "CREATE" | "UPDATE" | "DELETE"
     * @param tableName      Target table name. Example: "contacts", "projects"
     * @param username       Username of employee/user performing the action
     * @param ipAddress      Actual client IP
     * @param requestPayload JSON payload string from request (sensitive fields filtered)
     * @param isSuccess      true if DB operation succeeded
     * @param errorMessage   Error description if failed, null if successful
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
