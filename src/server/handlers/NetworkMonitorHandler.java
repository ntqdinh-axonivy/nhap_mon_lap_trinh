package server.handlers;

import common.*;
import org.json.JSONObject;

/**
 * Handler đo network metrics: bandwidth, latency.
 */
public class NetworkMonitorHandler {
    private LogManager logger;

    public NetworkMonitorHandler(LogManager logger) {
        this.logger = logger;
    }

    public Message handle(Message request) {
        String action = request.getAction();

        switch (action) {
            case ActionType.MEASURE_BANDWIDTH:
                return measureBandwidth(request);
            case ActionType.MEASURE_LATENCY:
                return measureLatency(request);
            default:
                return createErrorResponse(request, "Unknown network monitor action");
        }
    }

    private Message measureBandwidth(Message request) {
        // TODO: Implement bandwidth measurement
        logger.info("Measuring bandwidth (stub)");

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("bandwidth_mbps", 100.5); // Fake data
        data.put("message", "Stub implementation");
        return new Message(request, data);
    }

    private Message measureLatency(Message request) {
        long startTime = System.currentTimeMillis();
        long latency = System.currentTimeMillis() - startTime;

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("latency_ms", latency);
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
