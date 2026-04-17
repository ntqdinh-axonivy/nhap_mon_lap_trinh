package common;

/**
 * Định nghĩa các loại message trong hệ thống.
 */
public class MessageType {
    /** Message yêu cầu từ Client */
    public static final String REQUEST = "REQUEST";

    /** Message phản hồi từ Server */
    public static final String RESPONSE = "RESPONSE";

    /** Stream data liên tục (webcam, screen sharing, file chunks) */
    public static final String STREAM = "STREAM";

    /** Message báo lỗi */
    public static final String ERROR = "ERROR";

    /** Heartbeat để kiểm tra connection còn sống */
    public static final String HEARTBEAT = "HEARTBEAT";
}
