package com.stdiscm;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String saveFolderPath;

    public ClientHandler(Socket clientSocket, String saveFolderPath) {
        this.clientSocket = clientSocket;
        this.saveFolderPath = saveFolderPath;
    }

    @Override
    public void run() {
        try (InputStream inputStream = clientSocket.getInputStream()) {
            byte[] buffer = new byte[8192]; // Buffer size for reading data
            int bytesRead;

            // Create a unique file name for the video
            File outputFile = new File(saveFolderPath, "video_" + System.currentTimeMillis() + ".mp4");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("Saved video: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}