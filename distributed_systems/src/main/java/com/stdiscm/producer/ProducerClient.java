package com.stdiscm.producer;

import com.stdiscm.shared.VideoPacket;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ProducerClient {

    // Uploads ALL .mp4 files in the 'videos/' folder (used in thread-based producer mode)
    public static void runProducer(String host, int port) {
        try {
            File dir = new File("videos");
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".mp4"));

            if (files == null || files.length == 0) {
                System.out.println("No .mp4 files found in folder: videos/");
                return;
            }

            for (File video : files) {
                sendFile(video, host, port);
                Thread.sleep(2000); // simulate delay
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Uploads a single selected file (used by the GUI)
    public static void sendFile(File videoFile, String host, int port) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            byte[] data = Files.readAllBytes(videoFile.toPath());
            VideoPacket packet = new VideoPacket(videoFile.getName(), data);
            out.writeObject(packet);

            System.out.println("Uploaded: " + videoFile.getName());

        } catch (Exception e) {
            System.err.println("Failed to upload file: " + videoFile.getName());
            e.printStackTrace();
        }
    }

    public static void sendFolder(File folder, String host, int port) {
        File[] videoFiles = folder.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".mp4") ||
            name.toLowerCase().endsWith(".avi") ||
            name.toLowerCase().endsWith(".mov") ||
            name.toLowerCase().endsWith(".mkv"));
    
        if (videoFiles == null || videoFiles.length == 0) {
            System.out.println("No valid videos in: " + folder.getName());
            return;
        }
    
        for (File video : videoFiles) {
            sendFile(video, host, port);
            try {
                Thread.sleep(1000); // simulate delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
}
