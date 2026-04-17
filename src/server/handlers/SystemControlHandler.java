package server.handlers;

import common.*;
import org.json.JSONObject;

/**
 * Handler điều khiển hệ thống: shutdown, restart.
 */
public class SystemControlHandler {
    private LogManager logger;

    public SystemControlHandler(LogManager logger) {
        this.logger = logger;
    }

    public Message handle(Message request) {
        String action = request.getAction();

        switch (action) {
            case ActionType.SHUTDOWN:
                return shutdown(request);
            case ActionType.RESTART:
                return restart(request);
            default:
                return createErrorResponse(request, "Unknown system control action");
        }
    }

    /**
     * Shutdown máy tính server.
     */
    private Message shutdown(Message request) {
        try {
            logger.warn("SHUTDOWN command received!");

            // Delay để gửi response trước khi shutdown
            int delay = request.getData().optInt("delay", 10); // seconds

            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("message", "Shutting down in " + delay + " seconds");

            // Schedule shutdown
            new Thread(() -> {
                try {
                    Thread.sleep(delay * 1000);
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        Runtime.getRuntime().exec("shutdown /s /t 0");
                    } else if (os.contains("nix") || os.contains("nux")) {
                        Runtime.getRuntime().exec("shutdown -h now");
                    }
                } catch (Exception e) {
                    logger.error("Error executing shutdown", e);
                }
            }).start();

            logger.info("Shutdown scheduled in " + delay + " seconds");
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error handling shutdown", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    /**
     * Restart máy tính server.
     */
    private Message restart(Message request) {
        try {
            logger.warn("RESTART command received!");

            int delay = request.getData().optInt("delay", 10); // seconds

            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("message", "Restarting in " + delay + " seconds");

            // Schedule restart
            new Thread(() -> {
                try {
                    Thread.sleep(delay * 1000);
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        Runtime.getRuntime().exec("shutdown /r /t 0");
                    } else if (os.contains("nix") || os.contains("nux")) {
                        Runtime.getRuntime().exec("shutdown -r now");
                    }
                } catch (Exception e) {
                    logger.error("Error executing restart", e);
                }
            }).start();

            logger.info("Restart scheduled in " + delay + " seconds");
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error handling restart", e);
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
