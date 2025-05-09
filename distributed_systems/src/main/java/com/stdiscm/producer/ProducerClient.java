package com.stdiscm.producer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.stdiscm.shared.UploadStatus;
import com.stdiscm.shared.ZipHelper;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ProducerClient {

    public static void sendFile(File videoFile, String host, int port, ObservableList<UploadStatus> uploadStatuses) {

        try (Socket socket = new Socket(host, port);
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));) {
            writeString(dos, videoFile.getName());

            // Wait for server readiness
            String serverResponse = dis.readUTF();
            if ("QUEUED".equals(serverResponse)) {
                updateLatestStatus("queued", uploadStatuses);
                System.out.println("File queued: " + videoFile.getName());

                String processingResponse = dis.readUTF();
                if ("UPLOADING".equals(processingResponse)) {
                    updateLatestStatus("uploading", uploadStatuses);

                    sendFileData(videoFile, dos, socket, uploadStatuses);
                    // Wait for server's final confirmation
                    String finalResponse = dis.readUTF();

                    if ("SUCCESS".equals(finalResponse)) {
                        updateLatestStatus("success", uploadStatuses);
                        System.out.println("Upload successful: " + videoFile.getName());
                    } else if ("DUPLICATE".equals(finalResponse)) {
                        updateLatestStatus("duplicate", uploadStatuses);
                        System.out.println("Upload duplicate: " + videoFile.getName());
                    } else {
                        updateLatestStatus("failed", uploadStatuses);
                        System.err.println("Upload failed or not acknowledged for file: " + videoFile.getName());
                    }
                } else {
                    updateLatestStatus("failed", uploadStatuses);
                    System.err.println("Unexpected processing response: " + processingResponse);
                }
            } else if ("FULLQUEUE".equals(serverResponse)) {
                updateLatestStatus("queue is full", uploadStatuses);
                System.err.println("Server queue full. Upload rejected for file: " + videoFile.getName());
            } else {
                updateLatestStatus("failed", uploadStatuses);
                System.err.println("Unexpected server response: " + serverResponse);
            }
        } catch (IOException e) {
            updateLatestStatus("failed", uploadStatuses);
            System.err.println("Failed to upload file: " + videoFile.getName());
            e.printStackTrace();
        }
    }

    public static void sendFolder(File folder, String host, int port, Label status, ProgressBar progBar,
            ObservableList<UploadStatus> uploadStatuses) {
        File[] videoFiles = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".mkv");
        });

        if (videoFiles == null || videoFiles.length == 0) {
            System.out.println("No valid videos in folder: " + folder.getName());
            return;
        }

        for (File video : videoFiles) {
            String msg = "Uploading from folder: " + folder.getName() + "\nFile: " + video.getName();
            Platform.runLater(() -> status.setText(msg));
            addStatus(video.getName(), "preparing", uploadStatuses);

            File fileToSend = prepareFileForUpload(video, uploadStatuses);
            sendFile(fileToSend, host, port, uploadStatuses);
            cleanupTempZip(video);
        }

        Platform.runLater(() -> status.setText("Upload complete for folder: " + folder.getName() + "\nWaiting..."));
    }

    private static void addStatus(String filename, String status, ObservableList<UploadStatus> uploadStatuses) {
        UploadStatus uploadStatus = new UploadStatus(filename, status);
        Platform.runLater(() -> {
            uploadStatuses.add(uploadStatus);
            // Keep only the 5 most recent uploads
            if (uploadStatuses.size() > 5) {
                uploadStatuses.remove(0);
            }
        });
    }

    public static void updateLatestStatus(String newStatus, ObservableList<UploadStatus> uploadStatuses) {
        Platform.runLater(() -> {
            if (!uploadStatuses.isEmpty()) {
                UploadStatus latest = uploadStatuses.get(uploadStatuses.size() - 1);
                latest.setStatus(newStatus);
            }
        });
    }

    private static void writeString(DataOutputStream dos, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(bytes.length);
        dos.write(bytes);
        dos.flush();
    }

    private static void sendFileData(File file, DataOutputStream dos, Socket socket,
            ObservableList<UploadStatus> uploadStatuses) throws IOException {
        writeString(dos, file.getName());
        long fileLength = file.length();
        dos.writeLong(fileLength);
        dos.flush();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    shutdownSocketOutput(socket);
                    updateLatestStatus("interrupted", uploadStatuses);
                    return;
                }
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
        }
    }

    /**
     * Compresses the given file if beneficial, updates uploadStatuses, and returns
     * the file to actually send (either the .zip or the original).
     */
    private static File prepareFileForUpload(File original, ObservableList<UploadStatus> uploadStatuses) {
        String baseName = stripExtension(original.getName());
        File zipFile = new File(original.getParent(), baseName + ".zip");

        boolean compressed = ZipHelper.compressFile(original, zipFile);
        return compressed ? zipFile : original;
    }

    /**
     * Deletes the temporary zip file if it exists.
     */
    private static void cleanupTempZip(File original) {
        String baseName = stripExtension(original.getName());
        File zipFile = new File(original.getParent(), baseName + ".zip");
        if (zipFile.exists() && !zipFile.delete()) {
            System.err.println("Failed to delete temporary zip: " + zipFile.getName());
        }
    }

    /**
     * Utility to strip the extension from a filename.
     */
    private static String stripExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(0, dotIndex) : filename;
    }

    /**
     * Attempts a graceful shutdown of the socket output stream.
     */
    private static void shutdownSocketOutput(Socket socket) {
        try {
            socket.shutdownOutput();
            System.out.println("Socket output shut down gracefully.");
        } catch (IOException e) {
            System.err.println("Error during socket output shutdown.");
            e.printStackTrace();
        }
    }
}
