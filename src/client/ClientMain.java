package client;

import common.Constants;
import common.LogManager;
import client.ui.ClientUI;

/**
 * Entry point của Client application.
 */
public class ClientMain {

    public static void main(String[] args) {
        System.out.println("=== REMOTE CONTROL CLIENT ===");
        System.out.println("Starting client...\n");

        // Init logger
        LogManager logger = new LogManager("CLIENT", Constants.CLIENT_LOG_FILE);
        logger.info("Client initializing...");

        // Create UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI(logger);
            ui.setVisible(true);
            logger.info("Client UI started");
        });
    }
}
