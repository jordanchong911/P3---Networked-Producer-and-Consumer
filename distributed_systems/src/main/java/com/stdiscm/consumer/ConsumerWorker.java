package com.stdiscm.consumer;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import com.stdiscm.shared.UploadStatus;

public class ConsumerWorker implements Runnable {
    private final BlockingQueue<QueuedUpload> videoQueue;
    private final File uploadDir;
    private final ObservableList<UploadStatus> progressList;
    private volatile boolean running = true;

    public ConsumerWorker(BlockingQueue<QueuedUpload> videoQueue, File uploadDir, ObservableList<UploadStatus> progressList) {
        this.videoQueue = videoQueue;
        this.uploadDir = uploadDir;
        this.progressList = progressList;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                QueuedUpload upload = videoQueue.take(); // blocks
                while (!upload.isReady()) Thread.sleep(10); // Sleep for a while before checking again
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

            File outFile = new File(uploadDir, UUID.randomUUID() + "_" + fileName);
            try (
                FileOutputStream fos = new FileOutputStream(outFile)) {
            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
                double progress = 1 - (double) remaining / fileLength;
                Platform.runLater(() -> statusHolder.setProgress(progress));
            } 
            fos.flush();
            }

            dos.writeUTF("SUCCESS");
            dos.flush();
            
        } catch (IOException e) {
            System.out.println("Upload failed for " + upload.getFileName());
            e.printStackTrace();
        } finally {
            Platform.runLater(() -> progressList.remove(statusHolder));
            try {socket.close();
            } catch (IOException ignored) {}
        }
    }
}
