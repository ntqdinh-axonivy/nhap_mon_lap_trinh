package server.handlers;

import common.*;
import org.json.JSONObject;

/**
 * Handler keylogger - ghi lại các phím được nhấn.
 * TODO: Implement sử dụng JNativeHook library.
 */
public class KeyloggerHandler {
    private LogManager logger;
    private boolean isLogging = false;
    private StringBuilder keyBuffer = new StringBuilder();

    public KeyloggerHandler(LogManager logger) {
        this.logger = logger;
    }

    public Message handle(Message request) {
        String action = request.getAction();

        switch (action) {
            case ActionType.START_KEYLOGGER:
                return startKeylogger(request);
            case ActionType.STOP_KEYLOGGER:
                return stopKeylogger(request);
            case ActionType.GET_KEYLOG:
                return getKeylog(request);
            default:
                return createErrorResponse(request, "Unknown keylogger action");
        }
    }

    private Message startKeylogger(Message request) {
        // TODO: Implement với JNativeHook
        logger.warn("Keylogger START - Not fully implemented");
        isLogging = true;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "Keylogger started (stub implementation)");
        return new Message(request, data);
    }

    private Message stopKeylogger(Message request) {
        logger.info("Keylogger STOP");
        isLogging = false;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "Keylogger stopped");
        return new Message(request, data);
    }

    private Message getKeylog(Message request) {
        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("keys", keyBuffer.toString());
        data.put("length", keyBuffer.length());
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
