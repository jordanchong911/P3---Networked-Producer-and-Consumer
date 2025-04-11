package com.stdiscm.consumer;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.stdiscm.shared.DuplicateChecker;
import com.stdiscm.shared.UploadStatus;
import com.stdiscm.shared.ZipHelper;

public class ConsumerClient {
    private final int port;
    private final int consumerThreads;
    private final File uploadDir;
    private final BlockingQueue<QueuedUpload> videoQueue;
    private final ObservableList<UploadStatus> progressList;

    private volatile boolean running;
    private ExecutorService consumerExecutor;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private final List<ConsumerWorker> workers ;
    DuplicateChecker duplicateChecker;

    public ConsumerClient(int port, int consumerThreads, int maxQueueLength, ObservableList<UploadStatus> progressList) {
        this.port = port;
        this.consumerThreads = consumerThreads;
        this.workers = new ArrayList<>();
        this.progressList = progressList;
        this.duplicateChecker = new DuplicateChecker();
        this.videoQueue = new ArrayBlockingQueue<>(maxQueueLength);
        this.uploadDir = initiateUploadFolder("uploads");
    }

    public void start() {
        running = true;
        consumerExecutor = Executors.newFixedThreadPool(consumerThreads);

        for (int i = 0; i < consumerThreads; i++) {
            ConsumerWorker worker = new ConsumerWorker(videoQueue, uploadDir, progressList, duplicateChecker);
            workers.add(worker);
            consumerExecutor.execute(worker);
        }

        serverThread = new Thread(new UploadServer(), "UploadServerThread");
        serverThread.start();
    }

    public void stop() {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverThread != null && serverThread.isAlive()) {
            try { serverThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        for (ConsumerWorker worker : workers) worker.stop();
        if (consumerExecutor != null) {
            consumerExecutor.shutdown(); // allow ongoing tasks to complete
            try {
                if (!consumerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("Worker threads did not shut down gracefully. Forcing shutdown.");
                    consumerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                consumerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("ConsumerClient stopped.");
    }

    private class UploadServer implements Runnable {

        @Override
        public void run() {
            try (ServerSocket server = new ServerSocket(port)) {
                serverSocket = server;
                System.out.println("ConsumerClient listening on port " + port);
                while (running) handleClient();
            } catch (IOException e) {
                if (running) e.printStackTrace();
            } finally {
                System.out.println("Upload server stopping...");
            }
        }
    
        private void handleClient() {
            Socket socket = null;
            try {
                // Accept the next incoming connection
                socket = serverSocket.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    
                // Read metadata: file name length, file name, and file length.
                int fileNameLength = dis.readInt();
                byte[] nameBytes = new byte[fileNameLength];
                dis.readFully(nameBytes);
                String fileName = new String(nameBytes, StandardCharsets.UTF_8);
    
                // Prepare the upload status and the queued upload object.
                UploadStatus status = new UploadStatus(fileName);
                QueuedUpload queuedUpload = new QueuedUpload(socket, status, fileName);
    
                // Try to add the queued upload to the processing queue.
                if (!videoQueue.offer(queuedUpload)) {
                    System.out.println("Queue full, dropping video: " + fileName);
                    dos.writeUTF("FULLQUEUE");
                    dos.flush();
                    socket.close();
                } else {
                    // Signal the client to start streaming file data.
                    dos.writeUTF("QUEUED");
                    dos.flush();
                    Platform.runLater(() -> progressList.add(status));
                    queuedUpload.setReady();
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
                // In case of error, close the socket if it hasn't been closed.
                if (socket != null && !socket.isClosed()) {
                    try {socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public File initiateUploadFolder(String pathName) {
        // Use the provided pathName if needed; otherwise, default to "uploads"
        File uploadDir = new File(pathName);

        // Try to create the directory if it doesn't exist.
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            System.err.println("Could not create upload directory.");
            return uploadDir;
        } else if (!uploadDir.exists()) {
            // This branch is just a safe fallback, though it will never be reached
            System.err.println("Upload directory does not exist after mkdirs attempt.");
            return uploadDir;
        } else {
            // Directory created or already exists.
            System.out.println("Upload directory is ready at: " + uploadDir.getAbsolutePath());
        }

        File[] files = uploadDir.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("The directory is empty or an I/O error occurred.");
            return uploadDir;
        }

        ExecutorService executor = Executors.newFixedThreadPool(consumerThreads);

        for (File file : files) {
            // Submit each file processing task to the thread pool
            executor.submit(() -> {
                try {
                    // Process the file: extract, compute hash, and register
                    File videoFile = ZipHelper.tryExtract(file);
                    String fileHash = duplicateChecker.computeFileHash(videoFile);
                    duplicateChecker.register(fileHash);

                    // Log type of file
                    if (file.isFile()) {
                        System.out.println("File: " + file.getName());
                    } else if (file.isDirectory()) {
                        System.out.println("Directory: " + file.getName());
                    }
                } catch (Exception e) {
                    // Handle exceptions to avoid thread pool termination issues
                    System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
                }
            });
        }

        // Shutdown the thread pool and wait for tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                System.out.println("Forcing shutdown as tasks did not finish in time.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return uploadDir;
    }
}