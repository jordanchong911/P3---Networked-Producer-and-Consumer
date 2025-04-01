package com.stdiscm;
import java.io.File;
import javax.swing.*;

public class ConsumerMain {
    public static void main(String[] args) {
        int port = 12345; // Port to listen for incoming connections
        String saveFolderPath = "uploaded_videos"; // Folder to save uploaded videos

        // Ensure the save folder exists
        File saveFolder = new File(saveFolderPath);
        if (!saveFolder.exists()) {
            saveFolder.mkdirs();
        }

        // Start the consumer
        Consumer consumer = new Consumer(port, saveFolderPath);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        // Start the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new VideoPreviewGUI(saveFolderPath));
    }
}