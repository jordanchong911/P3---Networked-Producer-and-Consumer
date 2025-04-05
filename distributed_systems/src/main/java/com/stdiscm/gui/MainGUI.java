package com.stdiscm.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainGUI extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Media Upload Role Selector");

        Label prompt = new Label("Select your role:");

        Button producerBtn = new Button("Producer");
        Button consumerBtn = new Button("Consumer");

        producerBtn.setOnAction(e -> {
            primaryStage.close();
            ProducerMultiClientGUI.display();
        });

        consumerBtn.setOnAction(e -> {
            primaryStage.close();
            ConsumerMultiClientGUI.display();
        });

        VBox layout = new VBox(15, prompt, producerBtn, consumerBtn);
        layout.setPadding(new Insets(20));
        primaryStage.setScene(new Scene(layout, 300, 150));
        primaryStage.show();
    }
}
