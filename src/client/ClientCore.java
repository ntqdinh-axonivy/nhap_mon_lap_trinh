package client;

import common.*;
import java.net.Socket;
import java.io.IOException;

/**
 * Core client class - quản lý socket connection tới server.
 */
public class ClientCore {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private LogManager logger;
    private boolean connected = false;

    public ClientCore(LogManager logger) {
        this.logger = logger;
    }

    /**
     * Connect tới server.
     */
    public boolean connect(String host, int port) {
        try {
            logger.info("Connecting to " + host + ":" + port);

            socket = new Socket(host, port);
            socket.setSoTimeout(Constants.SOCKET_TIMEOUT);

            this.serverHost = host;
            this.serverPort = port;
            this.connected = true;

            logger.info("Connected to server successfully");
            return true;

        } catch (IOException e) {
            logger.error("Connection failed", e);
            connected = false;
            return false;
        }
    }

    /**
     * Disconnect khỏi server.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
            logger.info("Disconnected from server");
        } catch (IOException e) {
            logger.error("Error disconnecting", e);
        }
    }

    /**
     * Gửi request và nhận response.
     */
    public Message sendRequest(Message request) throws Exception {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IOException("Not connected to server");
        }

        NetworkUtils.sendMessage(socket, request);
        logger.logSent(request);

        Message response = NetworkUtils.receiveMessage(socket);
        logger.logReceived(response);

        return response;
    }

    /**
     * Gửi request không đợi response (fire-and-forget).
     */
    public void sendRequestAsync(Message request) throws Exception {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IOException("Not connected to server");
        }

        NetworkUtils.sendMessage(socket, request);
        logger.logSent(request);
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public Socket getSocket() {
        return socket;
    }

    public String getServerInfo() {
        return serverHost + ":" + serverPort;
    }
}
