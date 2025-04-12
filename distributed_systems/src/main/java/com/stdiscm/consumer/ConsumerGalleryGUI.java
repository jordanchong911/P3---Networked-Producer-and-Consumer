package com.stdiscm.consumer;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.stdiscm.gui.ConsumerMultiClientGUI;
import com.stdiscm.shared.ZipHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConsumerGalleryGUI {

    private static final File UPLOAD_DIR = new File("uploads");
    private static final List<String> VIDEO_EXTS = List.of(".mp4", ".avi", ".mov", ".mkv");
    private static final String ZIP_EXT = ".zip";

    private final TilePane videoGrid = new TilePane();
    private final MediaView mediaView = new MediaView();
    private final Map<File, File> extractedCache = new ConcurrentHashMap<>();

    private MediaPlayer previewPlayer;
    private PauseTransition previewDurationTimer; // ensures exactly 10 s of preview
    private PauseTransition previewStopTimer;     // delay after exit before stopping

    private MediaPlayer fullPlayer;
    private Label currentPlayingCell;

    public static void showGallery() {
        Platform.runLater(() -> new ConsumerGalleryGUI().showStage());
    }

    private void showStage() {
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Uploaded Videos");

        // Grid setup
        videoGrid.setPadding(new Insets(10));
        videoGrid.setHgap(10);
        videoGrid.setVgap(10);
        videoGrid.setPrefColumns(4);
        videoGrid.setAlignment(Pos.TOP_LEFT);

        updateVideoGrid();

        // Layout
        BorderPane layout = new BorderPane();
        layout.setLeft(videoGrid);
        StackPane mediaHolder = new StackPane(mediaView);
        mediaHolder.setMinSize(400, 300);
        mediaHolder.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        layout.setCenter(mediaHolder);
        BorderPane.setMargin(videoGrid, new Insets(10, 0, 10, 10));
        BorderPane.setMargin(mediaHolder, new Insets(10, 100, 10, 0));

        // Bind view size
        mediaView.fitWidthProperty().bind(mediaHolder.widthProperty());
        mediaView.fitHeightProperty().bind(mediaHolder.heightProperty());

        Scene scene = new Scene(layout, 800, 500);
        scene.getStylesheets().add(
            ConsumerMultiClientGUI.class.getResource("/styles.css").toExternalForm()
        );
        primaryStage.setScene(scene);
        primaryStage.show();

        startDirectoryWatcher();
    }

    private void updateVideoGrid() {
        UPLOAD_DIR.mkdirs();
        File[] files = UPLOAD_DIR.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return VIDEO_EXTS.stream().anyMatch(lower::endsWith)
                || lower.endsWith(ZIP_EXT);
        });

        videoGrid.getChildren().clear();
        if (files != null) {
            videoGrid.getChildren().addAll(
                Arrays.stream(files)
                      .map(this::createGridCell)
                      .collect(Collectors.toList())
            );
        }
    }


    private Label createGridCell(File file) {
        Label cell = new Label(file.getName());
        cell.getStyleClass().add("cell");
        cell.setPickOnBounds(true);
        cell.setOnMouseEntered(e -> {
            // 1) Cancel any pending stop
            if (fullPlayer != null) {
                return;
            }
            cell.getStyleClass().add("cell-hover");

            if (previewStopTimer != null) {
                previewStopTimer.stop();
            }
        
            startPreview(file);
        });
        
        cell.setOnMouseExited(e -> {
            // 1) Remove hover style
            if (fullPlayer != null) {
                return;
            }

            if (cell != currentPlayingCell) {
                cell.getStyleClass().remove("cell-hover");
            }
                
            // 3) Schedule stopPreview after 2 seconds
            if (previewStopTimer != null) {
                previewStopTimer.stop();
            }
            previewStopTimer = new PauseTransition(Duration.seconds(2));
            previewStopTimer.setOnFinished(evt -> stopPreview());
            previewStopTimer.play();
        });

        cell.setOnMouseClicked(e -> {
            System.out.print("clicked " + file.getName());
            cell.getStyleClass().remove("cell-hover");
            stopPreview();
            System.out.print(cell == currentPlayingCell);
            if (cell == currentPlayingCell) {
                stopFullVideo();
            } else {
                stopFullVideo();
                currentPlayingCell = cell;
                cell.getStyleClass().add("cell-selected");
                playVideo(file, cell);
            }
        });

        return cell;
    }

    private File resolveVideo(File file) throws IOException {
        if (!file.getName().toLowerCase().endsWith(ZIP_EXT)) {
            return file;
        }
        return extractedCache.computeIfAbsent(file, f -> {
                return ZipHelper.tryExtract(f);
        });
    }

    private void playVideo(File file, Label cell) {
        try {
            File video = resolveVideo(file);
            fullPlayer  = new MediaPlayer(new Media(video.toURI().toString()));
            mediaView.setMediaPlayer(fullPlayer);
            fullPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview(File file) {
        try {
            File video = resolveVideo(file);
            if (previewPlayer != null) {
                previewPlayer.stop();
                previewPlayer.dispose();
            }
            previewPlayer = new MediaPlayer(new Media(video.toURI().toString()));
            mediaView.setMediaPlayer(previewPlayer);
            previewPlayer.play();

            previewDurationTimer = new PauseTransition(Duration.seconds(10));
            previewDurationTimer.setOnFinished(evt -> stopPreview());
            previewDurationTimer.play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void stopPreview() {
        if (previewStopTimer != null) {
            previewStopTimer.stop();
            previewStopTimer = null;
        }
        if (previewDurationTimer != null) {
            previewDurationTimer.stop();
            previewDurationTimer = null;
        }
        if (previewPlayer != null) {
            previewPlayer.stop();
            previewPlayer.dispose();
            previewPlayer = null;
        }
    }

    private void stopFullVideo() {
        if (fullPlayer != null) {
            fullPlayer.stop();
            fullPlayer.dispose();
            fullPlayer = null;
        }
        if (currentPlayingCell != null) {
            currentPlayingCell.getStyleClass().remove("cell-selected");
            currentPlayingCell = null;
            
        }
    }

    private void startDirectoryWatcher() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            UPLOAD_DIR.toPath().register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);

            Thread watcherThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watcher.take();
                        List<WatchEvent<?>> events = key.pollEvents();
                        if (!events.isEmpty()) {
                            Platform.runLater(this::updateVideoGrid);
                        }
                        key.reset();
                    }
                } catch (InterruptedException ignored) { }
            });
            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
