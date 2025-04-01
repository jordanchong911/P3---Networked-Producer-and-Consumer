package com.stdiscm;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Consumer implements Runnable {
    private final int port; // Port to listen for incoming connections
    private final String saveFolderPath; // Folder to save uploaded videos

    public Consumer(int port, String saveFolderPath) {
        this.port = port;
        this.saveFolderPath = saveFolderPath;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Consumer is running and listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Handle the client connection in a separate thread
                Thread clientHandler = new Thread(new ClientHandler(clientSocket, saveFolderPath));
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Error in consumer: " + e.getMessage());
        }
    }
}