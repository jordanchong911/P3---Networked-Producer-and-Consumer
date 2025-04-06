package com.stdiscm.gui;

import com.stdiscm.producer.ProducerClient;
import com.stdiscm.shared.ConfigLoader;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ProducerMultiClientGUI {

    private static final List<String> VIDEO_EXT = Arrays.asList(".mp4", ".avi", ".mov", ".mkv");

    public static void display() {
        Stage stage = new Stage();
        stage.setTitle("Set Up Producers");

        Label prompt = new Label("How many producers?");
        TextField inputField = new TextField();
        Button nextBtn = new Button("Next");

        VBox layout = new VBox(10, prompt, inputField, nextBtn);
        layout.setPadding(new Insets(15));
        stage.setScene(new Scene(layout, 300, 150));
        stage.show();

        nextBtn.setOnAction(e -> {
            try {
                int count = Integer.parseInt(inputField.getText());
                stage.close();
                showFolderChoosers(count);
            } catch (NumberFormatException ex) {
                prompt.setText("❌ Invalid number. Try again:");
            }
        });
    }

    private static void showFolderChoosers(int count) {
        ConfigLoader config = new ConfigLoader("config.properties");
        String host = config.getConsumerHost();
        int port = config.getConsumerPort();
        
        for (int i = 1; i <= count; i++) {
            int index = i;
            Platform.runLater(() -> {
                Stage chooserStage = new Stage();
                chooserStage.setTitle("Producer Client " + index);

                Label label = new Label("Select a folder with videos:");
                Button chooseBtn = new Button("Choose Folder");
                Label status = new Label("Waiting...");

                chooseBtn.setOnAction(event -> {
                    DirectoryChooser dc = new DirectoryChooser();
                    File folder = dc.showDialog(chooserStage);
                    if (folder != null && folder.isDirectory()) {
                        File[] videos = folder.listFiles((dir, name) ->
                            VIDEO_EXT.stream().anyMatch(name.toLowerCase()::endsWith));
                        if (videos != null && videos.length > 0) {
                            status.setText("Uploading " + videos.length + " files...");
                            new Thread(() -> ProducerClient.sendFolder(folder, host, port)).start();
                        } else {
                            status.setText("No valid video files.");
                        }
                    }
                });

                VBox layout = new VBox(10, label, chooseBtn, status);
                layout.setPadding(new Insets(15));
                chooserStage.setScene(new Scene(layout, 350, 150));
                chooserStage.show();
            });
        }
    }
}
