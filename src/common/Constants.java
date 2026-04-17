package common;

/**
 * Các hằng số dùng chung trong project.
 */
public class Constants {
    // Network
    public static final int DEFAULT_PORT = 8888;
    public static final int MAX_MESSAGE_SIZE = 100 * 1024 * 1024; // 100MB
    public static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    public static final int HEARTBEAT_INTERVAL = 10000; // 10 seconds

    // File Transfer - QUAN TRỌNG: Đây là kích thước mỗi chunk
    /**
     * Kích thước mỗi chunk khi transfer file.
     * 1MB = optimal cho LAN:
     * - Đủ lớn để giảm overhead
     * - Đủ nhỏ để có progress updates mượt
     * - Phù hợp với RAM buffer
     */
    public static final int FILE_CHUNK_SIZE = 1024 * 1024; // 1MB per chunk

    // Screen Capture
    public static final int SCREENSHOT_QUALITY = 80; // JPEG quality 0-100
    public static final String SCREENSHOT_FORMAT = "jpg";

    // Webcam
    public static final int WEBCAM_FPS = 15; // 15 frames per second
    public static final int WEBCAM_WIDTH = 640;
    public static final int WEBCAM_HEIGHT = 480;
    public static final int WEBCAM_QUALITY = 70; // JPEG quality

    // Remote Desktop
    public static final int DESKTOP_FPS = 10;
    public static final int DESKTOP_QUALITY = 70;

    // Keylogger
    public static final int KEYLOG_BUFFER_SIZE = 1000; // Số keys tối đa buffer
    public static final String KEYLOG_FILE = "keylog.txt";

    // Logging
    public static final String SERVER_LOG_FILE = "server.log";
    public static final String CLIENT_LOG_FILE = "client.log";
}
