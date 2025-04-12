package com.stdiscm.consumer;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import com.stdiscm.shared.UploadStatus;
import com.stdiscm.shared.ZipHelper;
import com.stdiscm.shared.DuplicateChecker;

public class ConsumerWorker implements Runnable {
    private final DuplicateChecker duplicateChecker;
    private final BlockingQueue<QueuedUpload> videoQueue;
    private final File uploadDir;
    private final ObservableList<UploadStatus> progressList;
    private volatile boolean running = true;

    public ConsumerWorker(BlockingQueue<QueuedUpload> videoQueue, File uploadDir, ObservableList<UploadStatus> progressList, DuplicateChecker duplicateChecker) {
        this.videoQueue = videoQueue;
        this.uploadDir = uploadDir;
        this.progressList = progressList;
        this.duplicateChecker = duplicateChecker;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                QueuedUpload upload = videoQueue.take(); // blocks
                while (!upload.isReady()) {Thread.sleep(10);
                } // Sleep for a while before checking again
                processUpload(upload);
            } catch (InterruptedException e) {
                if (!running) break; // Exit cleanly
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
        System.out.println("ConsumerWorker thread exiting.");
    }

    private void processUpload(QueuedUpload upload) {
        Socket socket = upload.getSocket();
        UploadStatus statusHolder = upload.getStatus();

        
        try (
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {

             // Send the "UPLOADING" signal.
            dos.writeUTF("UPLOADING");
            dos.flush();

            int nameLength = dis.readInt();
            byte[] nameBytes = new byte[nameLength];
            dis.readFully(nameBytes);
            String fileName = new String(nameBytes, StandardCharsets.UTF_8);
            long fileLength = dis.readLong();

            byte[] buffer = new byte[4096];
            long remaining = fileLength;
            int bytesRead;

            File tempFile = File.createTempFile("upload_", "_" + fileName);

            // File outFile = new File(uploadDir, UUID.randomUUID() + "_" + fileName);
            try (
                FileOutputStream fos = new FileOutputStream(tempFile)) {
            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
                double progress = 1 - (double) remaining / fileLength;
                Platform.runLater(() -> statusHolder.setProgress(progress));
            } 
            fos.flush();
            }

            File videoFile = ZipHelper.tryExtract(tempFile);
            String fileHash = duplicateChecker.computeFileHash(videoFile);

            if (duplicateChecker.isDuplicate(fileHash)) {
                dos.writeUTF("DUPLICATE");
                dos.flush();
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("Failed to delete duplicate temporary file: " + tempFile.getName());
                }
            } else {
                duplicateChecker.register(fileHash);
                System.out.print(generateNewFileName(fileName) );
                File destFile = new File(uploadDir, generateNewFileName(fileName) );
                Files.move(tempFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                dos.writeUTF("SUCCESS");
                dos.flush();
            }
            
        } catch (IOException e) {
            System.out.println("Upload failed for " + upload.getFileName());
            e.printStackTrace();
        } finally {
            Platform.runLater(() -> progressList.remove(statusHolder));
            try {socket.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Generates a new file name by appending a timestamp and a unique UUID.
     * <p>
     * If the original file is compressed (ends with ".zip"), the method retains the ".zip"
     * extension. Otherwise, it uses the original file's extension.
     *
     * @param originalName The original file name.
     * @return A cleaner, unique file name with timestamp and UUID, retaining the correct extension.
     */
    public static String generateNewFileName(String originalName) {
        String baseName;
        String extension = "";

        // Check if the file is compressed (i.e. ends with ".zip")
        if (originalName.toLowerCase().endsWith(".zip")) {
            // Retain the ".zip" extension
            extension = ".zip";
            // Remove the ".zip" part to leave the underlying name (could be "video.mp4" if original was "video.mp4.zip")
            baseName = originalName.substring(0, originalName.length() - 4);
        } else {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
                baseName = originalName.substring(0, dotIndex);
                extension = originalName.substring(dotIndex); // includes the dot
            } else {
                // In case there's no dot, treat the entire string as the base name, without extension.
                baseName = originalName;
            }
        }
        
        // Generate a timestamp in the format: yyyyMMddHHmmss
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // Generate a UUID to ensure uniqueness
        String shortUuid = UUID.randomUUID().toString().substring(0, 12);


        // Combine the base name, timestamp, UUID, and extension to create a new file name.
        String newFileName = baseName + "_" + timestamp + "_" + shortUuid + extension;
        return newFileName;
    }
}
