package common;

import org.json.JSONObject;
import java.util.UUID;

/**
 * Đại diện cho một message trao đổi giữa Client và Server.
 * Tất cả communication đều dùng class này.
 */
public class Message {
    private String type;        // MessageType: REQUEST, RESPONSE, STREAM, ERROR
    private String action;      // ActionType: LIST_APPS, SCREENSHOT, etc.
    private String requestId;   // UUID để match request-response
    private long timestamp;     // Timestamp khi tạo message
    private JSONObject data;    // Payload data

    /**
     * Constructor tạo message mới (tự động generate requestId và timestamp).
     *
     * @param type Loại message (REQUEST, RESPONSE, STREAM, ERROR)
     * @param action Hành động cụ thể (LIST_APPS, KILL_PROCESS, etc.)
     * @param data Dữ liệu đi kèm (có thể null)
     */
    public Message(String type, String action, JSONObject data) {
        this.type = type;
        this.action = action;
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.data = (data != null) ? data : new JSONObject();
    }

    /**
     * Constructor để tạo response từ request (giữ nguyên requestId).
     *
     * @param originalRequest Request gốc cần response
     * @param data Dữ liệu response
     */
    public Message(Message originalRequest, JSONObject data) {
        this.type = MessageType.RESPONSE;
        this.action = originalRequest.getAction();
        this.requestId = originalRequest.getRequestId();
        this.timestamp = System.currentTimeMillis();
        this.data = (data != null) ? data : new JSONObject();
    }

    /**
     * Parse JSON string thành Message object.
     *
     * @param json JSON string nhận được từ network
     * @return Message object
     * @throws Exception nếu JSON không hợp lệ
     */
    public static Message fromJSON(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        Message msg = new Message(
            obj.getString("type"),
            obj.getString("action"),
            obj.has("data") ? obj.getJSONObject("data") : null
        );
        msg.requestId = obj.getString("requestId");
        msg.timestamp = obj.getLong("timestamp");
        return msg;
    }

    /**
     * Chuyển Message object thành JSON string để gửi qua network.
     *
     * @return JSON string
     */
    public String toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("action", action);
        obj.put("requestId", requestId);
        obj.put("timestamp", timestamp);
        obj.put("data", data);
        return obj.toString();
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public String getRequestId() { return requestId; }
    public long getTimestamp() { return timestamp; }
    public JSONObject getData() { return data; }
    public void setData(JSONObject data) { this.data = data; }
}
