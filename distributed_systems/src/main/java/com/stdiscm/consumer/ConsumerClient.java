package com.stdiscm.consumer;

import com.stdiscm.shared.VideoPacket;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConsumerClient {

    public static void run(String host, int port, int queueSize, Label statusLabel) {
        BlockingQueue<VideoPacket> queue = new ArrayBlockingQueue<>(queueSize);

        // 🆕 Launch the consumer GUI to view videos
        new Thread(() -> ConsumerGalleryGUI.launchGUI()).start();

        // Thread to listen for uploads
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                while (true) {
                    Socket client = server.accept();
                    try (ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
                        VideoPacket packet = (VideoPacket) in.readObject();
                        if (!queue.offer(packet)) {
                            log(statusLabel, "Queue full. Dropped: " + packet.getFilename());
                        } else {
                            log(statusLabel, "Received: " + packet.getFilename());
                        }
                    } catch (Exception e) {
                        log(statusLabel, "Invalid upload.");
                    }
                }
            } catch (IOException e) {
                log(statusLabel, "Error starting server: " + e.getMessage());
            }
        }).start();

        // Thread to consume and save videos
        new Thread(() -> {
            while (true) {
                try {
                    VideoPacket packet = queue.take();
                    Path dir = Paths.get("uploads");
                    Files.createDirectories(dir);

                    String filename = UUID.randomUUID() + "_" + packet.getFilename();
                    Path filepath = dir.resolve(filename);
                    Files.write(filepath, packet.getData());
                    log(statusLabel, "Saved: " + filename);
                } catch (Exception e) {
                    log(statusLabel, "Error saving file.");
                }
            }
        }).start();
    }

    private static void log(Label label, String message) {
        Platform.runLater(() -> label.setText(message));
        System.out.println("[Consumer] " + message);
    }
}
