package server.handlers;

import common.*;
import org.json.JSONObject;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Handler chụp màn hình server.
 * Sử dụng Robot class để capture screen.
 */
public class ScreenCaptureHandler {
    private LogManager logger;
    private Robot robot;

    public ScreenCaptureHandler(LogManager logger) {
        this.logger = logger;
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            logger.error("Failed to initialize Robot", e);
        }
    }

    public Message handle(Message request) {
        if (request.getAction().equals(ActionType.SCREENSHOT)) {
            return captureScreen(request);
        }
        return createErrorResponse(request, "Unknown screen capture action");
    }

    /**
     * Chụp toàn bộ màn hình và encode thành JPEG base64.
     */
    private Message captureScreen(Message request) {
        try {
            logger.info("Capturing screenshot...");

            if (robot == null) {
                return createErrorResponse(request, "Robot not initialized");
            }

            // Get screen size
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            // Capture screen
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            // Encode to JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, Constants.SCREENSHOT_FORMAT, baos);
            byte[] imageBytes = baos.toByteArray();

            // Encode to Base64
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("image", base64Image);
            data.put("width", screenRect.width);
            data.put("height", screenRect.height);
            data.put("format", Constants.SCREENSHOT_FORMAT);
            data.put("size", imageBytes.length);

            logger.info("Screenshot captured: " + imageBytes.length + " bytes");
            logger.logBinaryTransfer("SEND", "IMAGE", imageBytes.length);

            return new Message(request, data);

        } catch (Exception e) {
            logger.error("Error capturing screenshot", e);
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
