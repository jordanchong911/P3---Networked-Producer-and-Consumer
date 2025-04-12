package com.stdiscm.gui;

import com.stdiscm.producer.ProducerClient;
import com.stdiscm.shared.ConfigHelper;
import com.stdiscm.shared.UploadStatus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
       String propertiesFilePath = "config.properties";
    ConfigHelper config = new ConfigHelper(propertiesFilePath);
        
        String host = config.getHost();
        int port = config.getPort();

        
        for (int i = 1; i <= count; i++) {
            int index = i;
            Platform.runLater(() -> {
                Stage chooserStage = new Stage();
                chooserStage.setTitle("Producer Client " + index);
            
                Label instructionLabel = new Label("Select a folder with videos:");
                Button chooseBtn = new Button("Choose Folder");
                Label status = new Label("Waiting...");
                ProgressBar progressBar = new ProgressBar(0);
                progressBar.setPrefWidth(300);
                progressBar.setVisible(false);

                    // Create a TableView to show upload statuses
                TableView<UploadStatus> table = new TableView<>();
                table.setPrefWidth(380);
                ObservableList<UploadStatus> uploadStatuses = FXCollections.observableArrayList();
                table.setItems(uploadStatuses);

                TableColumn<UploadStatus, String> fileColumn = new TableColumn<>("File");
                fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
                fileColumn.setPrefWidth(280);

                TableColumn<UploadStatus, String> statusColumn = new TableColumn<>("Status");
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
                statusColumn.setPrefWidth(100);

                table.getColumns().add(fileColumn);
                table.getColumns().add(statusColumn);

                VBox root = new VBox(10, instructionLabel, chooseBtn, status, progressBar, table);
                root.setPadding(new Insets(10));
                Scene scene = new Scene(root, 410, 265);
                chooserStage.setScene(scene);
                chooserStage.show();
            
                chooseBtn.setOnAction(event -> {
                    DirectoryChooser dc = new DirectoryChooser();
                    File folder = dc.showDialog(chooserStage);
                    if (folder != null && folder.isDirectory()) {
                        File[] videos = folder.listFiles((dir, name) ->
                            VIDEO_EXT.stream().anyMatch(ext -> name.toLowerCase().endsWith(ext))
                        );
                        if (videos != null && videos.length > 0) {
                            status.setText("Uploading " + videos.length + " files...");
                            // Updated sendFolder method now accepts a ProgressBar as well
                            new Thread(() -> ProducerClient.sendFolder(folder, host, port, status, progressBar, uploadStatuses)).start();
                        } else {
                            status.setText("No valid video files.");
                        }
                    }
                });
            });            
        }
    }
}
