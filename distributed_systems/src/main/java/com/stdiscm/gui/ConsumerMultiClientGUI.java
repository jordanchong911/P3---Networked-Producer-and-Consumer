package com.stdiscm.gui;

import com.stdiscm.consumer.ConsumerClient;
import com.stdiscm.consumer.ConsumerGalleryGUI;
import com.stdiscm.shared.ConfigHelper;
import com.stdiscm.shared.UploadStatus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConsumerMultiClientGUI {

    public static void display() {
        Stage stage = new Stage();
        stage.setTitle("Set Up Consumers");

        Label label1 = new Label("Enter number of consumer clients:");
        TextField consumersField = new TextField();
        consumersField.setPromptText("e.g. 3");

        Label label2 = new Label("Enter max queue size:");
        TextField queueField = new TextField();
        queueField.setPromptText("e.g. 10");

        Button startBtn = new Button("Start Consumers");

        VBox layout = new VBox(10, label1, consumersField, label2, queueField, startBtn);
        layout.setPadding(new Insets(15));
        stage.setScene(new Scene(layout, 300, 200));
        stage.show();

        startBtn.setOnAction(e -> {
            try {
                int count = Integer.parseInt(consumersField.getText());
                int queueSize = Integer.parseInt(queueField.getText());
                stage.close();
                launchConsumers(count, queueSize);
            } catch (NumberFormatException ex) {
                label1.setText("❌ Please enter valid numbers.");
            }
        });
    }
    
    private static void launchConsumers(int count, int queueSize) {
               String propertiesFilePath = "config.properties";
    ConfigHelper config = new ConfigHelper(propertiesFilePath);
        
        int port = config.getPort();

        // Initialize the observable list for uploaded video names.
        ObservableList<UploadStatus> progressList = FXCollections.observableArrayList();

        // Start the ConsumerClient (server) in a separate thread.
        ConsumerClient client = new ConsumerClient(port, count, queueSize, progressList);
        client.start();
        
        ConsumerGalleryGUI.showGallery();
        showUploadProgressStage(progressList);
    }

    // Creates and shows a new stage to display upload progress.
    private static void showUploadProgressStage(ObservableList<UploadStatus> progressList) {
        Stage progressStage = new Stage();
        progressStage.setTitle("Upload Progress");

        // Create a TableView with two columns: File Name and Progress.
        TableView<UploadStatus> tableView = new TableView<>(progressList);
        tableView.setPrefWidth(400);

        TableColumn<UploadStatus, String> fileCol = new TableColumn<>("File");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileCol.setPrefWidth(200);

        TableColumn<UploadStatus, Double> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(new PropertyValueFactory<>("progress"));
        progressCol.setPrefWidth(180);
        // Use a custom cell factory to display a progress bar.
        progressCol.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar(0);

            @Override
            protected void updateItem(Double progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress);
                    setGraphic(progressBar);
                }
            }
        });

        tableView.getColumns().add(fileCol);
        tableView.getColumns().add(progressCol);


        VBox vbox = new VBox(tableView);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 410, 300);
        progressStage.setScene(scene);
        progressStage.show();
    }
}
