package com.stdiscm.consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ConsumerGalleryGUI extends Application {

    private static final File UPLOAD_DIR = new File("uploads");
    private static final List<String> VIDEO_EXTS = Arrays.asList(".mp4", ".avi", ".mov", ".mkv");

    private ListView<File> videoList;
    private MediaView mediaView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Uploaded Videos");

        videoList = new ListView<>();
        mediaView = new MediaView();

        updateVideoList();

        videoList.setOnMouseClicked(event -> {
            File selected = videoList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                playVideo(selected);
            }
        });

        BorderPane layout = new BorderPane();
        layout.setLeft(videoList);
        layout.setCenter(mediaView);
        BorderPane.setMargin(videoList, new Insets(10));
        BorderPane.setMargin(mediaView, new Insets(10));

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        startWatcherThread(); // auto-refresh video list
    }

    private void playVideo(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            mediaView.setMediaPlayer(player);
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateVideoList() {
        if (!UPLOAD_DIR.exists()) UPLOAD_DIR.mkdirs();

        File[] files = UPLOAD_DIR.listFiles((dir, name) ->
            VIDEO_EXTS.stream().anyMatch(name.toLowerCase()::endsWith));

        Platform.runLater(() -> {
            videoList.getItems().clear();
            if (files != null) {
                videoList.getItems().addAll(files);
            }
        });
    }

    private void startWatcherThread() {
        Thread watcher = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                    updateVideoList();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }

    public static void launchGUI() {
        Application.launch(ConsumerGalleryGUI.class);
    }
}
