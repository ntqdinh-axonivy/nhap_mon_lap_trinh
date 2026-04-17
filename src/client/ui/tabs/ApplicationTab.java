package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ApplicationTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JTable appTable;
    private DefaultTableModel tableModel;

    public ApplicationTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Table
        String[] columns = {"Name", "PID", "Window Title"};
        tableModel = new DefaultTableModel(columns, 0);
        appTable = new JTable(tableModel);
        add(new JScrollPane(appTable), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("🔄 Refresh List");
        JButton startBtn = new JButton("▶️ Start App");
        JButton stopBtn = new JButton("⏹️ Stop App");

        refreshBtn.addActionListener(e -> refreshApplicationList());
        startBtn.addActionListener(e -> startApplication());
        stopBtn.addActionListener(e -> stopApplication());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(startBtn);
        buttonPanel.add(stopBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshApplicationList() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.LIST_APPS, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                JSONArray apps = response.getData().getJSONArray("apps");
                tableModel.setRowCount(0);
                for (int i = 0; i < apps.length(); i++) {
                    JSONObject app = apps.getJSONObject(i);
                    tableModel.addRow(new Object[]{
                        app.optString("Name", ""),
                        app.optInt("Id", 0),
                        app.optString("MainWindowTitle", "")
                    });
                }
                logger.info("Loaded " + apps.length() + " applications");
            }
        } catch (Exception e) {
            logger.error("Error refreshing app list", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void startApplication() {
        String path = JOptionPane.showInputDialog(this, "Enter application path:");
        if (path != null && !path.trim().isEmpty()) {
            try {
                JSONObject data = new JSONObject();
                data.put("path", path);
                Message request = new Message(MessageType.REQUEST, ActionType.START_APP, data);
                Message response = clientCore.sendRequest(request);

                if (response.getData().getBoolean("success")) {
                    JOptionPane.showMessageDialog(this, "Application started!");
                    refreshApplicationList();
                }
            } catch (Exception e) {
                logger.error("Error starting app", e);
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void stopApplication() {
        int row = appTable.getSelectedRow();
        if (row >= 0) {
            int pid = (int) tableModel.getValueAt(row, 1);
            try {
                JSONObject data = new JSONObject();
                data.put("pid", pid);
                Message request = new Message(MessageType.REQUEST, ActionType.STOP_APP, data);
                Message response = clientCore.sendRequest(request);

                if (response.getData().getBoolean("success")) {
                    JOptionPane.showMessageDialog(this, "Application stopped!");
                    refreshApplicationList();
                }
            } catch (Exception e) {
                logger.error("Error stopping app", e);
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
