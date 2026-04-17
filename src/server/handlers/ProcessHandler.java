package server.handlers;

import common.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;

/**
 * Handler quản lý processes trên server.
 * Xử lý: LIST_PROCESSES, START_PROCESS, KILL_PROCESS.
 */
public class ProcessHandler {
    private LogManager logger;

    public ProcessHandler(LogManager logger) {
        this.logger = logger;
    }

    public Message handle(Message request) {
        String action = request.getAction();

        switch (action) {
            case ActionType.LIST_PROCESSES:
                return listProcesses(request);
            case ActionType.START_PROCESS:
                return startProcess(request);
            case ActionType.KILL_PROCESS:
                return killProcess(request);
            default:
                return createErrorResponse(request, "Unknown process action");
        }
    }

    /**
     * List tất cả processes đang chạy.
     */
    private Message listProcesses(Message request) {
        try {
            logger.info("Listing processes...");

            String command = "powershell.exe \"Get-Process | Select-Object Name, Id, CPU, " +
                           "WorkingSet, StartTime | ConvertTo-Json\"";

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            process.waitFor();

            JSONObject data = new JSONObject();
            data.put("success", true);

            String jsonStr = output.toString();
            if (jsonStr.trim().startsWith("[")) {
                data.put("processes", new JSONArray(jsonStr));
            } else if (jsonStr.trim().startsWith("{")) {
                JSONArray arr = new JSONArray();
                arr.put(new JSONObject(jsonStr));
                data.put("processes", arr);
            } else {
                data.put("processes", new JSONArray());
            }

            data.put("count", data.getJSONArray("processes").length());

            logger.info("Listed " + data.getInt("count") + " processes");
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error listing processes", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    /**
     * Start một process mới.
     */
    private Message startProcess(Message request) {
        try {
            String command = request.getData().getString("command");
            logger.info("Starting process: " + command);

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = pb.start();

            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("pid", process.pid());
            data.put("command", command);

            logger.info("Started process: " + command + " (PID: " + process.pid() + ")");
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error starting process", e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    /**
     * Kill một process theo PID.
     */
    private Message killProcess(Message request) {
        try {
            int pid = request.getData().getInt("pid");
            logger.info("Killing process PID: " + pid);

            Process process = Runtime.getRuntime().exec("taskkill /F /PID " + pid);
            int exitCode = process.waitFor();

            JSONObject data = new JSONObject();
            data.put("success", exitCode == 0);
            data.put("pid", pid);

            logger.info("Killed process PID: " + pid);
            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error killing process", e);
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
