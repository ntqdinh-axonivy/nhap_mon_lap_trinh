package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

public class ScreenshotTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JLabel imageLabel;

    public ScreenshotTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        imageLabel = new JLabel("No screenshot", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(800, 600));
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton captureBtn = new JButton("📷 Capture Screenshot");
        JButton saveBtn = new JButton("💾 Save");

        captureBtn.addActionListener(e -> captureScreenshot());
        saveBtn.addActionListener(e -> saveScreenshot());

        buttonPanel.add(captureBtn);
        buttonPanel.add(saveBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void captureScreenshot() {
        try {
            logger.info("Requesting screenshot...");
            Message request = new Message(MessageType.REQUEST, ActionType.SCREENSHOT, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                String base64Image = response.getData().getString("image");
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                
                Image img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                ImageIcon icon = new ImageIcon(img.getScaledInstance(800, 600, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
                imageLabel.setText("");

                logger.info("Screenshot received: " + imageBytes.length + " bytes");
            }
        } catch (Exception e) {
            logger.error("Error capturing screenshot", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void saveScreenshot() {
        // TODO: Implement save to file
        JOptionPane.showMessageDialog(this, "Save feature TODO");
    }
}
