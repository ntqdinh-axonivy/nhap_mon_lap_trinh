package server;

import common.*;
import server.ui.ServerUI;
import server.handlers.*;
import org.json.JSONObject;
import java.net.Socket;

/**
 * Router để điều hướng messages đến handler tương ứng dựa trên action type.
 * Pattern: Strategy Pattern - mỗi handler là một strategy riêng.
 */
public class MessageRouter {
    private ServerUI ui;
    private LogManager logger;

    // Handlers cho từng loại action
    private ApplicationHandler appHandler;
    private ProcessHandler processHandler;
    private ScreenCaptureHandler screenHandler;
    private KeyloggerHandler keyloggerHandler;
    private FileTransferHandler fileHandler;
    private SystemControlHandler systemHandler;
    private WebcamHandler webcamHandler;
    private NetworkMonitorHandler networkHandler;
    private RemoteDesktopHandler desktopHandler;
    private SystemLockHandler lockHandler;

    /**
     * Constructor - khởi tạo tất cả handlers.
     *
     * @param ui Server UI để log
     * @param logger LogManager
     */
    public MessageRouter(ServerUI ui, LogManager logger) {
        this.ui = ui;
        this.logger = logger;

        // Initialize all handlers
        this.appHandler = new ApplicationHandler(logger);
        this.processHandler = new ProcessHandler(logger);
        this.screenHandler = new ScreenCaptureHandler(logger);
        this.keyloggerHandler = new KeyloggerHandler(logger);
        this.fileHandler = new FileTransferHandler(logger);
        this.systemHandler = new SystemControlHandler(logger);
        this.webcamHandler = new WebcamHandler(logger);
        this.networkHandler = new NetworkMonitorHandler(logger);
        this.desktopHandler = new RemoteDesktopHandler(logger);
        this.lockHandler = new SystemLockHandler(logger);

        logger.info("MessageRouter initialized with all handlers");
    }

    /**
     * Route message đến handler phù hợp dựa trên action type.
     *
     * @param request Request message từ client
     * @param socket Socket connection (needed cho streaming handlers)
     * @return Response message (hoặc null nếu handler tự gửi response)
     */
    public Message route(Message request, Socket socket) {
        try {
            String action = request.getAction();

            // Application Management
            if (action.equals(ActionType.LIST_APPS) ||
                action.equals(ActionType.START_APP) ||
                action.equals(ActionType.STOP_APP)) {
                return appHandler.handle(request);
            }

            // Process Management
            else if (action.equals(ActionType.LIST_PROCESSES) ||
                     action.equals(ActionType.START_PROCESS) ||
                     action.equals(ActionType.KILL_PROCESS)) {
                return processHandler.handle(request);
            }

            // Screen Capture
            else if (action.equals(ActionType.SCREENSHOT)) {
                return screenHandler.handle(request);
            }

            // Keylogger
            else if (action.equals(ActionType.START_KEYLOGGER) ||
                     action.equals(ActionType.STOP_KEYLOGGER) ||
                     action.equals(ActionType.GET_KEYLOG)) {
                return keyloggerHandler.handle(request);
            }

            // File Transfer
            else if (action.equals(ActionType.UPLOAD_FILE) ||
                     action.equals(ActionType.DOWNLOAD_FILE)) {
                return fileHandler.handle(request, socket);
            }

            // System Control
            else if (action.equals(ActionType.SHUTDOWN) ||
                     action.equals(ActionType.RESTART)) {
                return systemHandler.handle(request);
            }

            // Webcam
            else if (action.equals(ActionType.START_WEBCAM) ||
                     action.equals(ActionType.STOP_WEBCAM)) {
                return webcamHandler.handle(request, socket);
            }

            // Network Monitor
            else if (action.equals(ActionType.MEASURE_BANDWIDTH) ||
                     action.equals(ActionType.MEASURE_LATENCY)) {
                return networkHandler.handle(request);
            }

            // Remote Desktop
            else if (action.equals(ActionType.START_REMOTE_DESKTOP) ||
                     action.equals(ActionType.STOP_REMOTE_DESKTOP) ||
                     action.equals(ActionType.MOUSE_EVENT) ||
                     action.equals(ActionType.KEYBOARD_EVENT)) {
                return desktopHandler.handle(request, socket);
            }

            // System Lock
            else if (action.equals(ActionType.LOCK_SYSTEM) ||
                     action.equals(ActionType.UNLOCK_SYSTEM)) {
                return lockHandler.handle(request);
            }

            // Unknown action
            else {
                logger.warn("Unknown action: " + action);
                return createErrorResponse(request, "Unknown action: " + action);
            }

        } catch (Exception e) {
            logger.error("Error routing message", e);
            return createErrorResponse(request, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Tạo error response message.
     *
     * @param request Request gốc
     * @param errorMessage Error message
     * @return Error response
     */
    private Message createErrorResponse(Message request, String errorMessage) {
        JSONObject data = new JSONObject();
        data.put("error", errorMessage);
        data.put("success", false);

        Message response = new Message(request, data);
        response.setType(MessageType.ERROR);
        return response;
    }
}
