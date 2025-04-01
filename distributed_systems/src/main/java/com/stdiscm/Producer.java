package com.stdiscm;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Producer implements Runnable {
    private final String videoFolderPath;

    public Producer(String videoFolderPath) {
        this.videoFolderPath = videoFolderPath;
    }

    @Override
    public void run() {
        try {
            processVideos();
        } catch (IOException e) {
            System.err.println("Error processing videos: " + e.getMessage());
        }
    }

    private void processVideos() throws IOException {
        File folder = new File(videoFolderPath);
        if (!folder.isDirectory()) {
            System.err.println("Invalid folder path: " + videoFolderPath);
            return;
        }

        File[] videoFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4")); // Assuming MP4 videos
        if (videoFiles == null || videoFiles.length == 0) {
            System.out.println("No video files found in " + videoFolderPath);
            return;
        }

        for (File videoFile : videoFiles) {
            System.out.println("Processing video: " + videoFile.getName());
            byte[] videoData = readFile(videoFile);
            if (videoData != null) {
                uploadVideo(videoData);
            }
        }
    }

    private byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            return buffer;
        }
    }

    private void uploadVideo(byte[] videoData) throws IOException {
        // Connect to the consumer
        try (Socket socket = new Socket("localhost", 12345); // Replace with consumer's IP if needed
             OutputStream outputStream = socket.getOutputStream()) {

            // Send video data to the consumer
            outputStream.write(videoData);
            System.out.println("Uploaded video to consumer");
        } catch (IOException e) {
            System.err.println("Failed to upload video: " + e.getMessage());
        }
    }
}