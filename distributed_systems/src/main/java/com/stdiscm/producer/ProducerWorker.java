package com.stdiscm.producer;

import com.stdiscm.shared.VideoPacket;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ProducerWorker implements Runnable {
    private final File folder;
    private final String serverHost;
    private final int serverPort;

    public ProducerWorker(File folder, String serverHost, int serverPort) {
        this.folder = folder;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".mp4"));
        if (files == null) return;

        for (File video : files) {
            try {
                byte[] data = Files.readAllBytes(video.toPath());
                VideoPacket packet = new VideoPacket(video.getName(), data);

                try (Socket socket = new Socket(serverHost, serverPort);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    out.writeObject(packet);
                    System.out.println("Uploaded: " + video.getName());
                }

                Thread.sleep(2000); // delay between uploads

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
