package server.ui;

import common.LogManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Simple UI cho Server - chỉ gồm log window và status bar.
 * Chạy ở background, không có nhiều interactions.
 */
public class ServerUI extends JFrame {
    private JTextArea logArea;
    private JLabel statusLabel;
    private LogManager logger;

    /**
     * Constructor.
     *
     * @param logger LogManager để hook log events
     */
    public ServerUI(LogManager logger) {
        this.logger = logger;
        initComponents();

        // Hook logger vào UI
        logger.addListener(this::appendLog);
    }

    /**
     * Initialize UI components.
     */
    private void initComponents() {
        setTitle("Remote Control Server");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout chính
        setLayout(new BorderLayout());

        // === TOP PANEL: Title + IP Info ===
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("🖥️ REMOTE CONTROL SERVER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);

        topPanel.add(Box.createVerticalStrut(10));

        // Display all IP addresses
        JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ipPanel.setBackground(new Color(41, 128, 185));
        JLabel ipLabel = new JLabel("Server IPs: " + getLocalIPAddresses());
        ipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        ipLabel.setForeground(new Color(255, 255, 255));
        ipPanel.add(ipLabel);
        topPanel.add(ipPanel);

        add(topPanel, BorderLayout.NORTH);

        // === CENTER PANEL: Log Area ===
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(23, 32, 42));
        logArea.setForeground(new Color(236, 240, 241));
        logArea.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // === BOTTOM PANEL: Status Bar ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Status: INITIALIZING...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        versionLabel.setForeground(Color.GRAY);
        bottomPanel.add(versionLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("Server UI closing...");
                System.exit(0);
            }
        });
    }

    /**
     * Hiển thị window.
     */
    public void show() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }

    /**
     * Update status bar.
     *
     * @param status Status message
     */
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + status);

            // Color coding
            if (status.contains("LISTENING")) {
                statusLabel.setForeground(new Color(46, 204, 113)); // Green
            } else if (status.contains("ERROR")) {
                statusLabel.setForeground(new Color(231, 76, 60)); // Red
            } else if (status.contains("CLIENT CONNECTED")) {
                statusLabel.setForeground(new Color(52, 152, 219)); // Blue
            } else {
                statusLabel.setForeground(Color.BLACK);
            }
        });
    }

    /**
     * Append log message to log area.
     * Called by LogManager via listener.
     *
     * @param level Log level
     * @param message Message content
     * @param timestamp Timestamp
     */
    public void appendLog(String level, String message, String timestamp) {
        SwingUtilities.invokeLater(() -> {
            String line = String.format("[%s] [%s] %s\n", timestamp, level, message);
            logArea.append(line);

            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Get all local IP addresses of this machine.
     * Useful for client to know which IP to connect to.
     *
     * @return Comma-separated list of IP addresses
     */
    private String getLocalIPAddresses() {
        StringBuilder ips = new StringBuilder();
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                java.net.NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();

                // Skip loopback and down interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();

                    // Only IPv4 addresses
                    if (addr instanceof java.net.Inet4Address) {
                        if (ips.length() > 0) {
                            ips.append(", ");
                        }
                        ips.append(addr.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            return "Unable to detect";
        }

        return ips.length() > 0 ? ips.toString() : "No network interface found";
    }
}
