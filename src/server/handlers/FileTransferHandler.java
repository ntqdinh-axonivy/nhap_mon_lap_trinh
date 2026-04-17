package server.handlers;

import common.*;
import org.json.JSONObject;
import java.net.Socket;
import java.io.File;

/**
 * Handler xử lý file transfer giữa client và server.
 * Hỗ trợ: UPLOAD (client → server), DOWNLOAD (server → client).
 */
public class FileTransferHandler {
    private LogManager logger;

    public FileTransferHandler(LogManager logger) {
        this.logger = logger;
    }

    /**
     * Handle file transfer requests.
     * Cần socket vì sẽ gửi chunks qua network.
     */
    public Message handle(Message request, Socket socket) {
        String action = request.getAction();

        switch (action) {
            case ActionType.DOWNLOAD_FILE:
                return handleDownload(request, socket);
            case ActionType.UPLOAD_FILE:
                return handleUpload(request, socket);
            default:
                return createErrorResponse(request, "Unknown file transfer action");
        }
    }

    /**
     * Handle DOWNLOAD: Server gửi file cho client.
     * Client request file path, server gửi file bằng chunks.
     */
    private Message handleDownload(Message request, Socket socket) {
        try {
            String filePath = request.getData().getString("path");
            String fileId = request.getData().getString("fileId");

            logger.info("Download request: " + filePath);

            File file = new File(filePath);
            if (!file.exists()) {
                return createErrorResponse(request, "File not found: " + filePath);
            }

            // Sử dụng NetworkUtils.sendFileChunked() để gửi file
            NetworkUtils.sendFileChunked(socket, fileId, filePath, logger);

            // Return success response
            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("fileId", fileId);
            data.put("fileName", file.getName());
            data.put("fileSize", file.length());

            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error handling download", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    /**
     * Handle UPLOAD: Client gửi file cho server.
     * Server sẽ nhận chunks và reassemble.
     */
    private Message handleUpload(Message request, Socket socket) {
        try {
            String savePath = request.getData().getString("savePath");
            String fileId = request.getData().getString("fileId");

            logger.info("Upload request: save to " + savePath);

            // TODO: Implement receiving file chunks
            // Client sẽ gửi FILE_INFO → FILE_CHUNK_0..N → TRANSFER_COMPLETE
            // Server dùng FileChunkReceiver để reassemble

            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("fileId", fileId);
            data.put("message", "Ready to receive file");

            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error handling upload", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    private Message createErrorResponse(Message request, String error) {
        JSONObject data = new JSONObject();
        data.put("error", error);
        data.put("success", false);
        Message response = new Message(request, data);
        response.setType(MessageType.ERROR);
        return response;
    }
}
