package common;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý logging cho cả Client và Server.
 * Log ra console, UI, và file đồng thời.
 */
public class LogManager {
    private List<LogListener> listeners = new ArrayList<>();
    private PrintWriter fileWriter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private String componentName; // "CLIENT" hoặc "SERVER"

    /**
     * Constructor.
     *
     * @param componentName Tên component ("CLIENT" hoặc "SERVER")
     * @param logFilePath Đường dẫn file log (null nếu không log ra file)
     */
    public LogManager(String componentName, String logFilePath) {
        this.componentName = componentName;

        if (logFilePath != null) {
            try {
                fileWriter = new PrintWriter(new FileWriter(logFilePath, true), true);
            } catch (IOException e) {
                System.err.println("Cannot create log file: " + e.getMessage());
            }
        }
    }

    /**
     * Đăng ký listener để nhận log events (để hiển thị trên UI).
     *
     * @param listener LogListener implementation
     */
    public void addListener(LogListener listener) {
        listeners.add(listener);
    }

    /**
     * Log một message với level INFO.
     *
     * @param message Nội dung log
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * Log message được gửi đi.
     *
     * @param message Message object
     */
    public void logSent(Message message) {
        String detail = String.format("SENT → [%s] %s | RequestID: %s | Data: %s",
            message.getType(),
            message.getAction(),
            message.getRequestId().substring(0, 8) + "...",
            summarizeData(message.getData())
        );
        log("SEND", detail);
    }

    /**
     * Log message được nhận.
     *
     * @param message Message object
     */
    public void logReceived(Message message) {
        String detail = String.format("RECV ← [%s] %s | RequestID: %s | Data: %s",
            message.getType(),
            message.getAction(),
            message.getRequestId().substring(0, 8) + "...",
            summarizeData(message.getData())
        );
        log("RECV", detail);
    }

    /**
     * Log binary data transfer (file, image, stream).
     *
     * @param direction "SEND" hoặc "RECV"
     * @param dataType "FILE", "IMAGE", "STREAM"
     * @param size Kích thước bytes
     */
    public void logBinaryTransfer(String direction, String dataType, int size) {
        String detail = String.format("%s %s | Size: %s",
            direction.equals("SEND") ? "SENT →" : "RECV ←",
            dataType,
            formatBytes(size)
        );
        log("DATA", detail);
    }

    /**
     * Log lỗi.
     *
     * @param message Mô tả lỗi
     * @param exception Exception object (có thể null)
     */
    public void error(String message, Exception exception) {
        String detail = message;
        if (exception != null) {
            detail += " | Exception: " + exception.getClass().getSimpleName()
                   + " - " + exception.getMessage();
        }
        log("ERROR", detail);
    }

    /**
     * Log warning.
     *
     * @param message Nội dung warning
     */
    public void warn(String message) {
        log("WARN", message);
    }

    /**
     * Internal log method - ghi log ra tất cả destinations.
     *
     * @param level Log level (INFO, SEND, RECV, ERROR, WARN)
     * @param message Nội dung
     */
    private void log(String level, String message) {
        String timestamp = dateFormat.format(new Date());
        String logLine = String.format("[%s] [%s] [%s] %s",
            timestamp, componentName, level, message);

        // Log ra console
        System.out.println(logLine);

        // Log ra file
        if (fileWriter != null) {
            fileWriter.println(logLine);
        }

        // Notify UI listeners
        for (LogListener listener : listeners) {
            listener.onLog(level, message, timestamp);
        }
    }

    /**
     * Tóm tắt JSONObject data để log (tránh log quá dài).
     *
     * @param data JSONObject
     * @return String tóm tắt
     */
    private String summarizeData(org.json.JSONObject data) {
        if (data == null || data.length() == 0) {
            return "{}";
        }

        // Nếu có binary data (base64), chỉ hiện size
        if (data.has("image") || data.has("file") || data.has("data")) {
            String key = data.has("image") ? "image" : (data.has("file") ? "file" : "data");
            String value = data.getString(key);
            if (value.length() > 100) {
                return String.format("{%s: <binary %s>}", key, formatBytes(value.length()));
            }
        }

        // Nếu data ngắn, hiện full
        String full = data.toString();
        if (full.length() <= 100) {
            return full;
        }

        // Nếu dài, cắt bớt
        return full.substring(0, 97) + "...";
    }

    /**
     * Format bytes thành human-readable string.
     *
     * @param bytes Số bytes
     * @return String (e.g., "1.5 MB")
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Đóng log file.
     */
    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

    /**
     * Interface cho UI components để nhận log events.
     */
    public interface LogListener {
        /**
         * Được gọi khi có log mới.
         *
         * @param level Log level
         * @param message Nội dung
         * @param timestamp Timestamp
         */
        void onLog(String level, String message, String timestamp);
    }
}
