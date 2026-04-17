package client.ui.tabs;

import common.LogManager;
import client.ClientCore;
import javax.swing.*;
import java.awt.*;

public class ProcessTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;

    public ProcessTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("<html><h2>ProcessTab</h2><p>TODO: Implement UI for this feature</p></html>", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }
}
