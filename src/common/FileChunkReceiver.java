package common;

import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class quản lý việc nhận và reassemble file chunks.
 *
 * USE CASE:
 * - Server gửi file 1GB = 1024 chunks x 1MB
 * - Client nhận từng chunks (có thể không theo thứ tự)
 * - FileChunkReceiver lưu chunks vào memory/disk
 * - Khi nhận đủ, tự động reassemble thành file hoàn chỉnh
 *
 * THREAD-SAFE: Có thể nhận chunks từ nhiều threads đồng thời.
 */
public class FileChunkReceiver {
    private String fileId;
    private String fileName;
    private long fileSize;
    private int totalChunks;
    private int chunkSize;

    // Lưu chunks đã nhận (chunkIndex -> data)
    private Map<Integer, byte[]> receivedChunks = new ConcurrentHashMap<>();

    // Callback khi hoàn thành
    private CompletionListener completionListener;

    // Logger
    private LogManager logger;

    /**
     * Constructor.
     *
     * @param fileInfo JSONObject chứa thông tin file (từ FILE_INFO message)
     * @param logger LogManager để log progress
     */
    public FileChunkReceiver(JSONObject fileInfo, LogManager logger) {
        this.fileId = fileInfo.getString("fileId");
        this.fileName = fileInfo.getString("fileName");
        this.fileSize = fileInfo.getLong("fileSize");
        this.totalChunks = fileInfo.getInt("totalChunks");
        this.chunkSize = fileInfo.getInt("chunkSize");
        this.logger = logger;

        if (logger != null) {
            logger.info(String.format("FileChunkReceiver initialized: %s (%d chunks, %s)",
                fileName, totalChunks, formatBytes(fileSize)));
        }
    }

    /**
     * Thêm một chunk đã nhận.
     * Tự động kiểm tra xem đã nhận đủ chưa, nếu đủ sẽ gọi reassemble().
     *
     * @param chunkIndex Index của chunk (0-based)
     * @param chunkData Dữ liệu chunk (byte array)
     * @return true nếu đã nhận đủ tất cả chunks
     */
    public synchronized boolean addChunk(int chunkIndex, byte[] chunkData) {
        // Validate
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            if (logger != null) {
                logger.warn("Invalid chunk index: " + chunkIndex);
            }
            return false;
        }

        // Lưu chunk
        receivedChunks.put(chunkIndex, chunkData);

        // Log progress
        int progress = (receivedChunks.size() * 100) / totalChunks;
        if (logger != null && receivedChunks.size() % 10 == 0) {
            logger.info(String.format("Receiving %s: %d%%", fileName, progress));
        }

        // Kiểm tra xem đã nhận đủ chưa
        if (receivedChunks.size() == totalChunks) {
            if (logger != null) {
                logger.info("All chunks received, reassembling file...");
            }
            return true;
        }

        return false;
    }

    /**
     * Ghép các chunks lại thành file hoàn chỉnh và lưu vào disk.
     *
     * @param outputPath Đường dẫn file output
     * @throws IOException nếu có lỗi ghi file
     */
    public void reassembleFile(String outputPath) throws IOException {
        if (receivedChunks.size() != totalChunks) {
            throw new IllegalStateException(
                String.format("Missing chunks: received %d/%d", receivedChunks.size(), totalChunks)
            );
        }

        long startTime = System.currentTimeMillis();

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            // Ghi chunks theo thứ tự
            for (int i = 0; i < totalChunks; i++) {
                byte[] chunk = receivedChunks.get(i);
                if (chunk == null) {
                    throw new IOException("Missing chunk: " + i);
                }
                fos.write(chunk);
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        if (logger != null) {
            logger.info(String.format("File reassembled successfully: %s (took %dms)",
                outputPath, duration));
        }

        // Gọi callback nếu có
        if (completionListener != null) {
            completionListener.onComplete(outputPath, fileSize);
        }

        // Clear memory
        receivedChunks.clear();
    }

    /**
     * Đăng ký callback khi transfer hoàn thành.
     *
     * @param listener CompletionListener
     */
    public void setCompletionListener(CompletionListener listener) {
        this.completionListener = listener;
    }

    /**
     * Lấy progress hiện tại (%).
     *
     * @return Progress từ 0-100
     */
    public int getProgress() {
        return (receivedChunks.size() * 100) / totalChunks;
    }

    /**
     * Kiểm tra xem đã nhận đủ chunks chưa.
     *
     * @return true nếu đã nhận đủ
     */
    public boolean isComplete() {
        return receivedChunks.size() == totalChunks;
    }

    /**
     * Lấy thông tin file.
     *
     * @return JSONObject chứa file info
     */
    public JSONObject getFileInfo() {
        JSONObject info = new JSONObject();
        info.put("fileId", fileId);
        info.put("fileName", fileName);
        info.put("fileSize", fileSize);
        info.put("totalChunks", totalChunks);
        info.put("receivedChunks", receivedChunks.size());
        info.put("progress", getProgress());
        return info;
    }

    /**
     * Format bytes thành human-readable.
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Interface cho completion callback.
     */
    public interface CompletionListener {
        /**
         * Được gọi khi file transfer hoàn thành.
         *
         * @param filePath Đường dẫn file đã reassemble
         * @param fileSize Kích thước file (bytes)
         */
        void onComplete(String filePath, long fileSize);
    }
}
