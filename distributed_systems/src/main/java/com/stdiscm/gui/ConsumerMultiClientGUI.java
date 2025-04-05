package com.stdiscm.gui;

import com.stdiscm.consumer.ConsumerClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        for (int i = 1; i <= count; i++) {
            int clientNumber = i;
            Platform.runLater(() -> {
                Stage cStage = new Stage();
                cStage.setTitle("Consumer Client " + clientNumber);

                Label statusLabel = new Label("Waiting for uploads...");

                VBox layout = new VBox(10, statusLabel);
                layout.setPadding(new Insets(15));
                cStage.setScene(new Scene(layout, 300, 100));
                cStage.show();

                // Start consumer logic
                new Thread(() -> {
                    ConsumerClient.run("localhost", 9999, queueSize, statusLabel);
                }).start();
            });
        }
    }
}
