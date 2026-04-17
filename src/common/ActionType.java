package common;

/**
 * Định nghĩa tất cả các action types trong hệ thống.
 */
public class ActionType {
    // Application Management
    public static final String LIST_APPS = "LIST_APPS";
    public static final String START_APP = "START_APP";
    public static final String STOP_APP = "STOP_APP";

    // Process Management
    public static final String LIST_PROCESSES = "LIST_PROCESSES";
    public static final String START_PROCESS = "START_PROCESS";
    public static final String KILL_PROCESS = "KILL_PROCESS";

    // Screen Capture
    public static final String SCREENSHOT = "SCREENSHOT";

    // Keylogger
    public static final String START_KEYLOGGER = "START_KEYLOGGER";
    public static final String STOP_KEYLOGGER = "STOP_KEYLOGGER";
    public static final String GET_KEYLOG = "GET_KEYLOG";

    // File Transfer
    public static final String UPLOAD_FILE = "UPLOAD_FILE";
    public static final String DOWNLOAD_FILE = "DOWNLOAD_FILE";
    public static final String FILE_CHUNK = "FILE_CHUNK";
    public static final String FILE_INFO = "FILE_INFO";
    public static final String TRANSFER_COMPLETE = "TRANSFER_COMPLETE";

    // System Control
    public static final String SHUTDOWN = "SHUTDOWN";
    public static final String RESTART = "RESTART";

    // Webcam
    public static final String START_WEBCAM = "START_WEBCAM";
    public static final String STOP_WEBCAM = "STOP_WEBCAM";
    public static final String WEBCAM_FRAME = "WEBCAM_FRAME";

    // Network Monitor
    public static final String MEASURE_BANDWIDTH = "MEASURE_BANDWIDTH";
    public static final String MEASURE_LATENCY = "MEASURE_LATENCY";

    // Remote Desktop
    public static final String START_REMOTE_DESKTOP = "START_REMOTE_DESKTOP";
    public static final String STOP_REMOTE_DESKTOP = "STOP_REMOTE_DESKTOP";
    public static final String DESKTOP_FRAME = "DESKTOP_FRAME";
    public static final String MOUSE_EVENT = "MOUSE_EVENT";
    public static final String KEYBOARD_EVENT = "KEYBOARD_EVENT";

    // System Lock
    public static final String LOCK_SYSTEM = "LOCK_SYSTEM";
    public static final String UNLOCK_SYSTEM = "UNLOCK_SYSTEM";
}
