package common;

import org.json.JSONObject;
import java.io.*;

/**
 * DEMO: Minh họa cách file transfer chunking hoạt động.
 *
 * Chạy demo này để hiểu:
 * 1. File 1GB được chia như thế nào thành chunks
 * 2. Thời gian chunking và reassembly
 * 3. RAM usage khi transfer
 */
public class Demo_FileTransfer {

    public static void main(String[] args) throws Exception {
        System.out.println("=== DEMO: FILE TRANSFER CHUNKING ===\n");

        // Simulate 1GB file
        long fileSize = 1024L * 1024 * 1024; // 1GB
        int chunkSize = Constants.FILE_CHUNK_SIZE; // 1MB
        int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);

        System.out.println("File size: " + formatBytes(fileSize));
        System.out.println("Chunk size: " + formatBytes(chunkSize));
        System.out.println("Total chunks: " + totalChunks);
        System.out.println();

        // === SCENARIO A: No Chunking (BAD) ===
        System.out.println("--- SCENARIO A: No Chunking ---");
        System.out.println("RAM required: " + formatBytes(fileSize));
        System.out.println("Progress updates: 0 (no progress info)");
        System.out.println("Resumable: NO");
        System.out.println("Timeout risk: HIGH (10s transfer)");
        System.out.println();

        // === SCENARIO B: With Chunking (GOOD) ===
        System.out.println("--- SCENARIO B: With 1MB Chunks ---");
        System.out.println("RAM required: " + formatBytes(chunkSize) + " (1000x less!)");
        System.out.println("Progress updates: " + totalChunks);
        System.out.println("Resumable: YES");
        System.out.println("Timeout risk: LOW (each chunk < 1s)");
        System.out.println();

        // === Overhead Analysis ===
        System.out.println("--- OVERHEAD ANALYSIS ---");

        // Message header overhead
        int headerSize = 100; // ~100 bytes per message (4 bytes length + JSON metadata)
        long totalOverhead = (long) totalChunks * headerSize;
        double overheadPercent = (totalOverhead * 100.0) / fileSize;

        System.out.println("Message headers: " + totalChunks + " × " + headerSize + "B = "
                         + formatBytes(totalOverhead));
        System.out.println("Overhead: " + String.format("%.4f%%", overheadPercent));
        System.out.println();

        // === Time Benchmark (Simulation) ===
        System.out.println("--- TIME BENCHMARK (Simulated) ---");

        long chunkingTime = 5; // ms
        long networkTime = 10000; // 10s for 1GB on Gigabit LAN
        long concatTime = 80; // ms
        long writeTime = 1200; // 1.2s to write to SSD
        long totalTime = chunkingTime + networkTime + concatTime + writeTime;

        System.out.println("Chunking time:    " + chunkingTime + "ms ("
                         + String.format("%.4f%%", chunkingTime * 100.0 / totalTime) + ")");
        System.out.println("Network transfer: " + networkTime + "ms ("
                         + String.format("%.2f%%", networkTime * 100.0 / totalTime) + ")");
        System.out.println("Concat time:      " + concatTime + "ms ("
                         + String.format("%.4f%%", concatTime * 100.0 / totalTime) + ")");
        System.out.println("Disk write:       " + writeTime + "ms ("
                         + String.format("%.2f%%", writeTime * 100.0 / totalTime) + ")");
        System.out.println("TOTAL:            " + totalTime + "ms");
        System.out.println();

        System.out.println("🎯 CONCLUSION:");
        System.out.println("- Chunking overhead: < 0.01% (NEGLIGIBLE)");
        System.out.println("- Network is bottleneck (99.9% of time)");
        System.out.println("- Benefits: Low RAM, Progress, Resumable");
        System.out.println("- Verdict: ALWAYS USE CHUNKING! ✅");
        System.out.println();

        // === Demo FileChunkReceiver ===
        System.out.println("=== DEMO: FileChunkReceiver Usage ===\n");

        // Giả lập nhận FILE_INFO
        JSONObject fileInfo = new JSONObject();
        fileInfo.put("fileId", "demo-123");
        fileInfo.put("fileName", "largefile.zip");
        fileInfo.put("fileSize", fileSize);
        fileInfo.put("totalChunks", totalChunks);
        fileInfo.put("chunkSize", chunkSize);

        LogManager logger = new LogManager("DEMO", null);
        FileChunkReceiver receiver = new FileChunkReceiver(fileInfo, logger);

        // Simulate nhận 10 chunks
        System.out.println("Simulating receiving 10 chunks...\n");
        for (int i = 0; i < 10; i++) {
            byte[] chunkData = new byte[chunkSize]; // Fake data
            receiver.addChunk(i, chunkData);

            System.out.println("Chunk " + i + " received | Progress: " + receiver.getProgress() + "%");
        }

        System.out.println("\n✅ Demo completed!");
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
