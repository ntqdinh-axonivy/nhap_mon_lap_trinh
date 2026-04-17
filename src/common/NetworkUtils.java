package common;

import org.json.JSONObject;
import java.io.*;
import java.net.Socket;

/**
 * Utility class để gửi/nhận messages qua TCP socket với length-prefix protocol.
 *
 * PROTOCOL FORMAT:
 * ┌─────────────────┬──────────────────┐
 * │ 4 bytes (int)   │  N bytes (data)  │
 * │ Message Length  │  JSON/Binary     │
 * └─────────────────┴──────────────────┘
 */
public class NetworkUtils {

    /**
     * Gửi một message qua socket với length-prefix protocol.
     * Format: [4 bytes length][JSON data]
     *
     * THREAD-SAFE: Synchronized để tránh interleaving khi nhiều threads gửi.
     *
     * @param socket Socket để gửi
     * @param message Message cần gửi
     * @throws IOException nếu có lỗi network
     */
    public static synchronized void sendMessage(Socket socket, Message message) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        String json = message.toJSON();
        byte[] data = json.getBytes("UTF-8");

        // Validation: Kiểm tra message không quá lớn
        if (data.length > Constants.MAX_MESSAGE_SIZE) {
            throw new IOException("Message too large: " + data.length + " bytes");
        }

        // Gửi độ dài trước (4 bytes)
        out.writeInt(data.length);

        // Gửi data
        out.write(data);
        out.flush();
    }

    /**
     * Nhận một message từ socket với length-prefix protocol.
     * Đọc đúng số bytes được chỉ định trong header.
     *
     * BLOCKING: Method này sẽ block cho đến khi nhận đủ data hoặc timeout.
     *
     * @param socket Socket để nhận
     * @return Message nhận được
     * @throws IOException nếu có lỗi network hoặc connection bị đóng
     * @throws Exception nếu JSON không hợp lệ
     */
    public static Message receiveMessage(Socket socket) throws IOException, Exception {
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // Đọc độ dài (4 bytes) - BLOCKING
        int length = in.readInt();

        // Validation: Tránh out of memory nếu length quá lớn
        if (length <= 0 || length > Constants.MAX_MESSAGE_SIZE) {
            throw new IOException("Invalid message length: " + length);
        }

        // Đọc chính xác 'length' bytes - BLOCKING
        byte[] data = new byte[length];
        in.readFully(data); // readFully() đảm bảo đọc đủ hoặc throw EOFException

        String json = new String(data, "UTF-8");
        return Message.fromJSON(json);
    }

    /**
     * Gửi binary data (file chunk, image) qua socket.
     * Format: [4 bytes metadata length][JSON metadata][4 bytes data length][binary data]
     *
     * Metadata JSON chứa:
     * - type: "FILE", "IMAGE", "STREAM"
     * - size: kích thước data
     * - chunkIndex: (optional) index của chunk
     * - totalChunks: (optional) tổng số chunks
     *
     * @param socket Socket để gửi
     * @param metadata JSON metadata
     * @param binaryData Binary data cần gửi
     * @throws IOException nếu có lỗi network
     */
    public static synchronized void sendBinaryData(Socket socket, JSONObject metadata, byte[] binaryData)
            throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Gửi metadata
        byte[] metadataBytes = metadata.toString().getBytes("UTF-8");
        out.writeInt(metadataBytes.length);
        out.write(metadataBytes);

        // Gửi binary data
        out.writeInt(binaryData.length);
        out.write(binaryData);
        out.flush();
    }

    /**
     * Nhận binary data từ socket.
     *
     * @param socket Socket để nhận
     * @return BinaryDataPacket chứa metadata và data
     * @throws IOException nếu có lỗi network
     */
    public static BinaryDataPacket receiveBinaryData(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // Đọc metadata
        int metadataLength = in.readInt();
        if (metadataLength <= 0 || metadataLength > 1024 * 1024) { // Max 1MB metadata
            throw new IOException("Invalid metadata length: " + metadataLength);
        }

        byte[] metadataBytes = new byte[metadataLength];
        in.readFully(metadataBytes);
        JSONObject metadata = new JSONObject(new String(metadataBytes, "UTF-8"));

        // Đọc binary data
        int dataLength = in.readInt();
        if (dataLength < 0 || dataLength > Constants.MAX_MESSAGE_SIZE) {
            throw new IOException("Invalid data length: " + dataLength);
        }

        byte[] data = new byte[dataLength];
        in.readFully(data);

        return new BinaryDataPacket(metadata, data);
    }

    /**
     * Gửi file qua socket bằng chunking mechanism.
     * Tự động chia file thành chunks và gửi từng chunks.
     *
     * USE CASE: Gửi file 1GB
     * - File chia thành 1024 chunks x 1MB
     * - Mỗi chunk gửi riêng với metadata (chunkIndex, totalChunks)
     * - Receiver có thể track progress và reassemble
     *
     * @param socket Socket để gửi
     * @param fileId Unique ID cho file transfer session
     * @param filePath Đường dẫn file cần gửi
     * @param logger LogManager để log progress (có thể null)
     * @throws IOException nếu có lỗi
     */
    public static void sendFileChunked(Socket socket, String fileId, String filePath, LogManager logger)
            throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        long fileSize = file.length();
        int chunkSize = Constants.FILE_CHUNK_SIZE;
        int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);

        // Gửi FILE_INFO message trước
        JSONObject infoData = new JSONObject();
        infoData.put("fileId", fileId);
        infoData.put("fileName", file.getName());
        infoData.put("fileSize", fileSize);
        infoData.put("totalChunks", totalChunks);
        infoData.put("chunkSize", chunkSize);

        Message infoMsg = new Message(MessageType.STREAM, ActionType.FILE_INFO, infoData);
        sendMessage(socket, infoMsg);

        if (logger != null) {
            logger.info(String.format("Sending file: %s (%d chunks)", file.getName(), totalChunks));
        }

        // Gửi từng chunk
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[chunkSize];
            int chunkIndex = 0;
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) > 0) {
                // Tạo chunk metadata
                JSONObject chunkMeta = new JSONObject();
                chunkMeta.put("fileId", fileId);
                chunkMeta.put("chunkIndex", chunkIndex);
                chunkMeta.put("totalChunks", totalChunks);
                chunkMeta.put("chunkSize", bytesRead);

                // Tạo chunk message
                byte[] chunkData = (bytesRead == chunkSize) ? buffer :
                                   java.util.Arrays.copyOf(buffer, bytesRead);

                JSONObject data = new JSONObject();
                data.put("metadata", chunkMeta);
                data.put("data", java.util.Base64.getEncoder().encodeToString(chunkData));

                Message chunkMsg = new Message(MessageType.STREAM, ActionType.FILE_CHUNK, data);
                sendMessage(socket, chunkMsg);

                if (logger != null && chunkIndex % 10 == 0) { // Log mỗi 10 chunks
                    int progress = (int) ((chunkIndex + 1) * 100.0 / totalChunks);
                    logger.info(String.format("File transfer progress: %d%%", progress));
                }

                chunkIndex++;
            }
        }

        // Gửi TRANSFER_COMPLETE
        JSONObject completeData = new JSONObject();
        completeData.put("fileId", fileId);
        completeData.put("success", true);

        Message completeMsg = new Message(MessageType.RESPONSE, ActionType.TRANSFER_COMPLETE, completeData);
        sendMessage(socket, completeMsg);

        if (logger != null) {
            logger.info("File transfer completed: " + file.getName());
        }
    }

    /**
     * Inner class để wrap binary data + metadata.
     */
    public static class BinaryDataPacket {
        public JSONObject metadata;
        public byte[] data;

        public BinaryDataPacket(JSONObject metadata, byte[] data) {
            this.metadata = metadata;
            this.data = data;
        }
    }
}
