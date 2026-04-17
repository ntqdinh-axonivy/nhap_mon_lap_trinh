package server.handlers;

import common.*;
import org.json.JSONObject;

/**
 * Handler lock/unlock hệ thống (block input).
 * TODO: Implement với JNA User32.BlockInput().
 */
public class SystemLockHandler {
    private LogManager logger;
    private boolean isLocked = false;

    public SystemLockHandler(LogManager logger) {
        this.logger = logger;
    }

    public Message handle(Message request) {
        String action = request.getAction();

        switch (action) {
            case ActionType.LOCK_SYSTEM:
                return lockSystem(request);
            case ActionType.UNLOCK_SYSTEM:
                return unlockSystem(request);
            default:
                return createErrorResponse(request, "Unknown system lock action");
        }
    }

    private Message lockSystem(Message request) {
        // TODO: Implement với JNA User32.BlockInput(true)
        logger.warn("System LOCK - Not fully implemented");
        isLocked = true;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "System locked (stub implementation)");
        return new Message(request, data);
    }

    private Message unlockSystem(Message request) {
        // TODO: Implement với JNA User32.BlockInput(false)
        logger.info("System UNLOCK");
        isLocked = false;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("message", "System unlocked");
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
