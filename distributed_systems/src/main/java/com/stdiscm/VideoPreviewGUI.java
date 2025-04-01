package com.stdiscm;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoPreviewGUI {
    private final JFrame frame;
    private final JPanel videoPanel;

    public VideoPreviewGUI(String saveFolderPath) {
        // Initialize the frame
        frame = new JFrame("Uploaded Videos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Panel to hold video entries
        videoPanel = new JPanel();
        videoPanel.setLayout(new GridLayout(0, 1, 10, 10)); // Single column layout with spacing

        JScrollPane scrollPane = new JScrollPane(videoPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Start monitoring the folder
        monitorFolder(saveFolderPath);

        // Make the frame visible
        frame.setVisible(true);
    }

    private void monitorFolder(String saveFolderPath) {
        new Thread(() -> {
            File folder = new File(saveFolderPath);
            File[] previousFiles = folder.listFiles();

            while (true) {
                try {
                    Thread.sleep(2000); // Check every 2 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                File[] currentFiles = folder.listFiles();
                if (currentFiles != null && !java.util.Arrays.equals(previousFiles, currentFiles)) {
                    updateVideoList(currentFiles, saveFolderPath);
                    previousFiles = currentFiles;
                }
            }
        }).start();
    }

    private void updateVideoList(File[] files, String saveFolderPath) {
        // Clear the video panel
        videoPanel.removeAll();

        // Add video entries
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".mp4")) {
                Date uploadTime = new Date(file.lastModified());
                addVideoEntry(file, uploadTime, saveFolderPath);
            }
        }

        // Refresh the panel
        videoPanel.revalidate();
        videoPanel.repaint();
    }

    private void addVideoEntry(File videoFile, Date uploadTime, String saveFolderPath) {
        // Create a panel for the video entry
        JPanel videoEntryPanel = new JPanel();
        videoEntryPanel.setLayout(new BorderLayout());
        videoEntryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Thumbnail
        ImageIcon thumbnailIcon = getThumbnailIcon(videoFile, saveFolderPath);
        JLabel thumbnailLabel = new JLabel(thumbnailIcon);
        videoEntryPanel.add(thumbnailLabel, BorderLayout.WEST);

        // Text panel (title and timestamp)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(2, 1));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Title
        JLabel titleLabel = new JLabel(videoFile.getName(), SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        textPanel.add(titleLabel);

        // Upload timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JLabel timestampLabel = new JLabel("Uploaded: " + dateFormat.format(uploadTime), SwingConstants.LEFT);
        timestampLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textPanel.add(timestampLabel);

        videoEntryPanel.add(textPanel, BorderLayout.CENTER);

        // Add the video entry panel to the main panel
        videoPanel.add(videoEntryPanel);
    }

    private ImageIcon getThumbnailIcon(File videoFile, String saveFolderPath) {
        try {
            // Generate a thumbnail using JCodec
            File thumbnailFile = ThumbnailGenerator.generateThumbnail(videoFile, saveFolderPath);
            ImageIcon icon = new ImageIcon(thumbnailFile.getAbsolutePath());

            // Resize the thumbnail to fit the layout
            Image scaledImage = icon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            System.err.println("Failed to generate thumbnail for " + videoFile.getName() + ": " + e.getMessage());
            return null;
        }
    }
}