package server.handlers;

import common.*;
import org.json.JSONObject;
import java.net.Socket;

/**
 * Handler remote desktop - stream màn hình + điều khiển mouse/keyboard.
 * TODO: Implement screen streaming + input handling.
 */
public class RemoteDesktopHandler {
    private LogManager logger;
    private boolean isStreaming = false;

    public RemoteDesktopHandler(LogManager logger) {
        this.logger = logger;
    }

    public Message handle(Message request, Socket socket) {
        String action = request.getAction();

        switch (action) {
            case ActionType.START_REMOTE_DESKTOP:
                return startRemoteDesktop(request, socket);
            case ActionType.STOP_REMOTE_DESKTOP:
                return stopRemoteDesktop(request);
            case ActionType.MOUSE_EVENT:
                return handleMouseEvent(request);
            case ActionType.KEYBOARD_EVENT:
                return handleKeyboardEvent(request);
            default:
                return createErrorResponse(request, "Unknown remote desktop action");
        }
    }

    private Message startRemoteDesktop(Message request, Socket socket) {
        // TODO: Start screen streaming thread
        logger.warn("Remote Desktop START - Not fully implemented");
        isStreaming = true;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "Remote desktop started (stub implementation)");
        return new Message(request, data);
    }

    private Message stopRemoteDesktop(Message request) {
        logger.info("Remote Desktop STOP");
        isStreaming = false;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "Remote desktop stopped");
        return new Message(request, data);
    }

    private Message handleMouseEvent(Message request) {
        // TODO: Simulate mouse event
        JSONObject data = new JSONObject();
        data.put("success", true);
        return new Message(request, data);
    }

    private Message handleKeyboardEvent(Message request) {
        // TODO: Simulate keyboard event
        JSONObject data = new JSONObject();
        data.put("success", true);
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
