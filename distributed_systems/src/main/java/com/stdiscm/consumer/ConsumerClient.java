package com.stdiscm.consumer;

// import com.stdiscm.shared.VideoPacket;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerClient  {
    private int port;
    private int consumerThreads;
    private File uploadDir;
    private BlockingQueue<File> videoQueue;
    // Callback to update the GUI's video list
    private ObservableList<UploadStatus> progressList;

    public ConsumerClient(int port, int consumerThreads, int maxQueueLength, ObservableList<UploadStatus> progressList) {
        uploadDir = new File("uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        this.port = port;
        this.consumerThreads = consumerThreads;
        this.progressList = progressList;
        this.videoQueue = new ArrayBlockingQueue<>(maxQueueLength);
    }

    public void start() {
        // Start the server in a separate thread.
        new Thread(new UploadServer()).start();
        // Start the video processing thread that polls the queue.
    }


    // The server listens for incoming upload connections.
    private class UploadServer implements Runnable {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("ConsumerClient listening on port " + port);
                ExecutorService executor = Executors.newFixedThreadPool(consumerThreads);
                System.out.println(consumerThreads);
                while (true) {
                    Socket socket = serverSocket.accept();
                    executor.execute(new UploadHandler(socket));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Handles a single upload connection.
    private class UploadHandler implements Runnable {
        private Socket socket;

        public UploadHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            final UploadStatus[] statusHolder = new UploadStatus[1]; // Final wrapper for UploadStatus
            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                // Read the file name.
                int fileNameLength = dis.readInt();
                byte[] fileNameBytes = new byte[fileNameLength];
                dis.readFully(fileNameBytes);
                String fileName = new String(fileNameBytes);

                // Read the file size.
                long fileSize = dis.readLong();
                
                statusHolder[0] = new UploadStatus(fileName);
                Platform.runLater(() -> progressList.add(statusHolder[0]));

                // Save the file to the uploads folder.
                File outFile = new File(uploadDir, UUID.randomUUID() + "_" + fileName);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[4096];
                    long totalRead = 0;
                    int read;
                    while (totalRead < fileSize &&
                           (read = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
                        fos.write(buffer, 0, read);
                        totalRead += read;
                        // Update progress (from 0.0 to 1.0)
                        double progress = (double) totalRead / fileSize;
                        Platform.runLater(() -> statusHolder[0].setProgress(progress));
                    }
                }
                System.out.println("Received file: " + fileName);

                // Try to add the video file to the queue (leaky bucket).
                if (!videoQueue.offer(outFile)) {
                    System.out.println("Queue full, dropping video: " + fileName);
                } else {

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (statusHolder[0] != null) {
                    Platform.runLater(() -> progressList.remove(statusHolder[0]));
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore close exception.
                }
            }
        }
    }
}
