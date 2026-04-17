package server.handlers;

import common.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;

/**
 * Handler quản lý applications trên server.
 * Xử lý: LIST_APPS, START_APP, STOP_APP.
 */
public class ApplicationHandler {
    private LogManager logger;

    public ApplicationHandler(LogManager logger) {
        this.logger = logger;
    }

    /**
     * Xử lý request liên quan đến application management.
     *
     * @param request Request từ client
     * @return Response message
     */
    public Message handle(Message request) {
        String action = request.getAction();

        switch (action) {
            case ActionType.LIST_APPS:
                return listApplications(request);
            case ActionType.START_APP:
                return startApplication(request);
            case ActionType.STOP_APP:
                return stopApplication(request);
            default:
                return createErrorResponse(request, "Unknown application action");
        }
    }

    /**
     * Lấy danh sách các applications đang chạy trên Windows.
     * Sử dụng PowerShell Get-Process để lấy processes có MainWindowTitle.
     *
     * @param request Request message
     * @return Response chứa danh sách apps (name, pid, windowTitle)
     */
    private Message listApplications(Message request) {
        try {
            logger.info("Listing applications...");

            // PowerShell command lấy processes có window
            String command = "powershell.exe \"Get-Process | Where-Object {$_.MainWindowTitle -ne ''} " +
                           "| Select-Object Name, Id, MainWindowTitle | ConvertTo-Json\"";

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();

            JSONObject data = new JSONObject();
            data.put("success", exitCode == 0);

            if (exitCode == 0 && output.length() > 0) {
                String jsonStr = output.toString();
                // Handle single object vs array
                if (jsonStr.trim().startsWith("[")) {
                    data.put("apps", new JSONArray(jsonStr));
                } else {
                    JSONArray arr = new JSONArray();
                    arr.put(new JSONObject(jsonStr));
                    data.put("apps", arr);
                }
                data.put("count", data.getJSONArray("apps").length());
            } else {
                data.put("apps", new JSONArray());
                data.put("count", 0);
            }

            logger.info("Listed " + data.getInt("count") + " applications");
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error listing apps", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    /**
     * Khởi động một application từ đường dẫn.
     *
     * @param request Request chứa {"path": "C:\\path\\to\\app.exe"}
     * @return Response với status (success/failure)
     */
    private Message startApplication(Message request) {
        try {
            String appPath = request.getData().getString("path");
            logger.info("Starting application: " + appPath);

            // Validate path exists
            File appFile = new File(appPath);
            if (!appFile.exists()) {
                return createErrorResponse(request, "Application not found: " + appPath);
            }

            // Start application
            ProcessBuilder pb = new ProcessBuilder(appPath);
            Process process = pb.start();

            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("pid", process.pid());
            data.put("path", appPath);

            logger.info("Started application: " + appPath + " (PID: " + process.pid() + ")");
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error starting app", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    /**
     * Dừng application bằng cách kill process theo PID.
     *
     * @param request Request chứa {"pid": 1234}
     * @return Response với status
     */
    private Message stopApplication(Message request) {
        try {
            int pid = request.getData().getInt("pid");
            logger.info("Stopping application PID: " + pid);

            // Kill process bằng taskkill
            String command = "taskkill /F /PID " + pid;
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            JSONObject data = new JSONObject();
            data.put("success", exitCode == 0);
            data.put("pid", pid);

            logger.info("Stopped application PID: " + pid);
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error stopping app", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    private Message createErrorResponse(Message request, String error) {
        JSONObject data = new JSONObject();
        data.put("error", error);
        data.put("success", false);
        Message response = new Message(request, data);
        response.setType(MessageType.ERROR);
        return response;
    }
}
