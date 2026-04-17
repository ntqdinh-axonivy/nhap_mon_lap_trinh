package server.handlers;

import common.*;
import org.json.JSONObject;
import java.net.Socket;

/**
 * Handler webcam streaming.
 * TODO: Implement sử dụng Webcam Capture library.
 */
public class WebcamHandler {
    private LogManager logger;
    private boolean isStreaming = false;

    public WebcamHandler(LogManager logger) {
        this.logger = logger;
    }

    public Message handle(Message request, Socket socket) {
        String action = request.getAction();

        switch (action) {
            case ActionType.START_WEBCAM:
                return startWebcam(request, socket);
            case ActionType.STOP_WEBCAM:
                return stopWebcam(request);
            default:
                return createErrorResponse(request, "Unknown webcam action");
        }
    }

    private Message startWebcam(Message request, Socket socket) {
        // TODO: Implement với Webcam Capture library
        logger.warn("Webcam START - Not fully implemented");
        isStreaming = true;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "Webcam started (stub implementation)");
        return new Message(request, data);
    }

    private Message stopWebcam(Message request) {
        logger.info("Webcam STOP");
        isStreaming = false;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "Webcam stopped");
        return new Message(request, data);
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
