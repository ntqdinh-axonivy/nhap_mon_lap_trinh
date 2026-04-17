package server;

import common.Constants;
import common.LogManager;
import server.ui.ServerUI;

/**
 * Entry point của Server application.
 * Khởi tạo UI và ServerCore, sau đó start listener.
 */
public class ServerMain {

    /**
     * Main method - khởi động server.
     *
     * @param args Command line arguments:
     *             args[0]: port (optional, default 8888)
     */
    public static void main(String[] args) {
        System.out.println("=== REMOTE CONTROL SERVER ===");
        System.out.println("Starting server...\n");

        // Parse port từ args hoặc dùng default
        int port = Constants.DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: " + Constants.DEFAULT_PORT);
            }
        }

        // Khởi tạo Logger
        LogManager logger = new LogManager("SERVER", Constants.SERVER_LOG_FILE);
        logger.info("Initializing server on port " + port);

        // Khởi tạo UI
        ServerUI ui = new ServerUI(logger);
        ui.show();

        // Khởi động server core
        ServerCore server = new ServerCore(port, ui, logger);

        // Thêm shutdown hook để cleanup khi tắt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            server.stop();
            logger.close();
        }));

        // Start server
        server.start();

        logger.info("Server started successfully on port " + port);
        System.out.println("Server is running. Check the UI for logs.");
        System.out.println("Press Ctrl+C to stop the server.");
    }
}
