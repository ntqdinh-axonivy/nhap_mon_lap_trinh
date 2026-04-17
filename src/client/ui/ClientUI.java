package client.ui;

import common.*;
import client.ClientCore;
import client.ui.tabs.*;
import javax.swing.*;
import java.awt.*;

/**
 * Main UI cho Client - tabbed interface với 10 tabs cho từng tính năng.
 */
public class ClientUI extends JFrame {
    private LogManager logger;
    private ClientCore clientCore;
    private JTabbedPane tabbedPane;
    private JTextArea logArea;
    private JLabel statusLabel;

    // Connection info
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;

    public ClientUI(LogManager logger) {
        this.logger = logger;
        this.clientCore = new ClientCore(logger);

        initComponents();
        logger.addListener(this::appendLog);
    }

    private void initComponents() {
        setTitle("Remote Control Client");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === TOP PANEL: Connection ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(52, 73, 94));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("🖥️ REMOTE CONTROL CLIENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        topPanel.add(Box.createHorizontalStrut(50));

        JLabel hostLabel = new JLabel("Server:");
        hostLabel.setForeground(Color.WHITE);
        topPanel.add(hostLabel);

        hostField = new JTextField("localhost", 12);
        topPanel.add(hostField);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(Color.WHITE);
        topPanel.add(portLabel);

        portField = new JTextField(String.valueOf(Constants.DEFAULT_PORT), 6);
        topPanel.add(portField);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> toggleConnection());
        topPanel.add(connectButton);

        statusLabel = new JLabel("● Disconnected");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        // === CENTER: Tabbed Pane ===
        tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Applications", new ApplicationTab(clientCore, logger));
        tabbedPane.addTab("Processes", new ProcessTab(clientCore, logger));
        tabbedPane.addTab("Screenshot", new ScreenshotTab(clientCore, logger));
        tabbedPane.addTab("File Transfer", new FileTransferTab(clientCore, logger));
        tabbedPane.addTab("System Control", new SystemControlTab(clientCore, logger));

        // Stub tabs
        tabbedPane.addTab("Keylogger", createStubPanel("Keylogger"));
        tabbedPane.addTab("Webcam", createStubPanel("Webcam"));
        tabbedPane.addTab("Network Monitor", createStubPanel("Network Monitor"));
        tabbedPane.addTab("Remote Desktop", createStubPanel("Remote Desktop"));
        tabbedPane.addTab("System Lock", createStubPanel("System Lock"));

        add(tabbedPane, BorderLayout.CENTER);

        // === BOTTOM: Log Panel ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        bottomPanel.setPreferredSize(new Dimension(getWidth(), 200));

        logArea = new JTextArea(10, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));

        JScrollPane logScroll = new JScrollPane(logArea);
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Toggle connection to server.
     */
    private void toggleConnection() {
        if (clientCore.isConnected()) {
            clientCore.disconnect();
            connectButton.setText("Connect");
            statusLabel.setText("● Disconnected");
            statusLabel.setForeground(Color.RED);
        } else {
            String host = hostField.getText();
            int port;
            try {
                port = Integer.parseInt(portField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid port number!");
                return;
            }

            if (clientCore.connect(host, port)) {
                connectButton.setText("Disconnect");
                statusLabel.setText("● Connected");
                statusLabel.setForeground(Color.GREEN);
            } else {
                JOptionPane.showMessageDialog(this, "Connection failed!");
            }
        }
    }

    /**
     * Append log to log area.
     */
    public void appendLog(String level, String message, String timestamp) {
        SwingUtilities.invokeLater(() -> {
            String line = String.format("[%s] [%s] %s\n", timestamp, level, message);
            logArea.append(line);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Create stub panel cho features chưa implement.
     */
    private JPanel createStubPanel(String featureName) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><h2>" + featureName + "</h2>" +
            "<p>TODO: Feature not yet implemented</p></html>",
            SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
