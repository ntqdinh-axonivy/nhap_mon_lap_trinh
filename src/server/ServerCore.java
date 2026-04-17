package server;

import common.*;
import server.ui.ServerUI;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * Core server class - quản lý TCP socket listener và client connections.
 * Sử dụng ExecutorService thread pool để handle nhiều clients đồng thời.
 */
public class ServerCore {
    private int port;
    private ServerSocket serverSocket;
    private ServerUI ui;
    private LogManager logger;
    private MessageRouter router;
    private ExecutorService threadPool;
    private boolean running = false;

    /**
     * Constructor.
     *
     * @param port Port để listen
     * @param ui Server UI để hiển thị logs
     * @param logger LogManager
     */
    public ServerCore(int port, ServerUI ui, LogManager logger) {
        this.port = port;
        this.ui = ui;
        this.logger = logger;
        this.router = new MessageRouter(ui, logger);
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Khởi động server listener trên port đã chỉ định.
     * Chạy trong thread riêng để không block main thread.
     */
    public void start() {
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setSoTimeout(0); // No timeout - block indefinitely
                running = true;

                logger.info("Server listening on port " + port);
                ui.updateStatus("LISTENING on port " + port);

                while (running) {
                    try {
                        // Accept client connection - BLOCKING
                        Socket clientSocket = serverSocket.accept();
                        String clientAddr = clientSocket.getInetAddress().getHostAddress();

                        logger.info("Client connected: " + clientAddr);
                        ui.updateStatus("CLIENT CONNECTED: " + clientAddr);

                        // Spawn new thread để handle client
                        threadPool.execute(new ClientHandler(clientSocket, clientAddr));

                    } catch (SocketException e) {
                        if (!running) {
                            // Server đang shutdown - normal
                            break;
                        }
                        logger.error("Socket error", e);
                    }
                }

            } catch (IOException e) {
                logger.error("Server startup failed", e);
                ui.updateStatus("ERROR: " + e.getMessage());
            } finally {
                logger.info("Server stopped");
                ui.updateStatus("STOPPED");
            }
        });
    }

    /**
     * Dừng server và cleanup resources.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (Exception e) {
            logger.error("Error stopping server", e);
        }
    }

    /**
     * Inner class xử lý mỗi client connection.
     * Mỗi client chạy trong thread riêng từ thread pool.
     */
    private class ClientHandler implements Runnable {
        private Socket socket;
        private String clientAddr;

        public ClientHandler(Socket socket, String clientAddr) {
            this.socket = socket;
            this.clientAddr = clientAddr;
        }

        @Override
        public void run() {
            try {
                // Set socket timeout để tránh block vô hạn
                socket.setSoTimeout(Constants.SOCKET_TIMEOUT);

                logger.info("ClientHandler started for: " + clientAddr);

                // Main message loop
                while (!socket.isClosed() && running) {
                    try {
                        // Nhận request từ client - BLOCKING
                        Message request = NetworkUtils.receiveMessage(socket);
                        logger.logReceived(request);

                        // Route đến handler tương ứng
                        Message response = router.route(request, socket);

                        // Gửi response về client (nếu có)
                        if (response != null) {
                            NetworkUtils.sendMessage(socket, response);
                            logger.logSent(response);
                        }

                    } catch (SocketTimeoutException e) {
                        // Timeout - gửi heartbeat để check connection
                        try {
                            Message heartbeat = new Message(
                                MessageType.HEARTBEAT,
                                "PING",
                                null
                            );
                            NetworkUtils.sendMessage(socket, heartbeat);
                        } catch (IOException ioe) {
                            // Connection lost
                            logger.warn("Client timeout: " + clientAddr);
                            break;
                        }
                    }
                }

            } catch (EOFException e) {
                logger.info("Client disconnected: " + clientAddr);
            } catch (Exception e) {
                logger.error("ClientHandler error for " + clientAddr, e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {}

                logger.info("ClientHandler stopped for: " + clientAddr);
                ui.updateStatus("CLIENT DISCONNECTED: " + clientAddr);
            }
        }
    }
}
