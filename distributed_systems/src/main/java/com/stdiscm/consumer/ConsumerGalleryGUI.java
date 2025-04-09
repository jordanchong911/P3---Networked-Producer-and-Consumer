package com.stdiscm.consumer;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.stdiscm.shared.ZipHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ConsumerGalleryGUI {

    private static final File UPLOAD_DIR = new File("uploads");
    private static final List<String> VIDEO_EXTS = Arrays.asList(".mp4", ".avi", ".mov", ".mkv");
    private static final String ZIP_EXT = ".zip";

    // Replace ListView with a TilePane for grid display.
    private TilePane videoGrid = new TilePane();
    private MediaView mediaView = new MediaView();

    // Preview handling variables.
    private MediaPlayer previewPlayer;
    private PauseTransition previewStopTimer;
    private Thread previewThread;
    private volatile boolean previewCancelled = false;
    
    // Add this field to your class.
    private File currentPreviewFile = null;

    public static void showGallery() {
        Platform.runLater(() -> {
            ConsumerGalleryGUI gui = new ConsumerGalleryGUI();
            gui.showStage();
        });
    }

    private void showStage() {
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Uploaded Videos");

        // Setup grid layout.
        videoGrid.setPadding(new Insets(10));
        videoGrid.setHgap(10);
        videoGrid.setVgap(10);
        videoGrid.setPrefColumns(4); // Adjust the number of columns as needed.
        videoGrid.setAlignment(Pos.TOP_LEFT);
        updateVideoGrid();

        // Create grid cells dynamically when files change.
        // Each cell (Label) will display the file name and will have hover/click effects.
        // We'll update the grid in updateVideoGrid() below.

        // When a cell is clicked, play the full video.
        // The hover events are handled in the cell creation.
        BorderPane layout = new BorderPane();
        layout.setLeft(videoGrid);
        layout.setCenter(mediaView);
        BorderPane.setMargin(videoGrid, new Insets(10, 0, 10, 10));
        BorderPane.setMargin(mediaView, new Insets(10, 100, 10, 0));

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);

        // Bind the MediaView's size.
        mediaView.setPreserveRatio(true);
        mediaView.fitWidthProperty().bind(
            scene.widthProperty()
                 .subtract(videoGrid.widthProperty())
                 .subtract(100)
        );
        mediaView.fitHeightProperty().bind(scene.heightProperty());

        primaryStage.show();
        startWatcherThread();
    }

    /**
     * Updates the grid with file cells.
     */
    private void updateVideoGrid() {
        if (!UPLOAD_DIR.exists()) {
            UPLOAD_DIR.mkdirs();
        }

        File[] files = UPLOAD_DIR.listFiles((dir, name) ->
                VIDEO_EXTS.stream().anyMatch(name.toLowerCase()::endsWith)
                        || name.toLowerCase().endsWith(ZIP_EXT));

        Platform.runLater(() -> {
            videoGrid.getChildren().clear();
            if (files != null) {
                for (File file : files) {
                    Label cell = createGridCell(file);
                    videoGrid.getChildren().add(cell);
                }
            }
        });
    }

    /**
     * Creates a grid cell (Label) for the given file with hover and click effects.
     */
    private Label createGridCell(File file) {
        Label cell = new Label(file.getName());
        cell.setPrefSize(100, 80); // Adjust cell size as needed.
        cell.setAlignment(Pos.CENTER);
        cell.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
        
        // Hover effect: outline the cell.
        cell.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            cell.setStyle("-fx-border-color: blue; -fx-border-width: 2px;");
            previewVideo(file);
        });
        cell.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            cell.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
            stopPreview();
        });
        // Click effect: fill the cell and play full video.
        cell.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            // Clear any preview and set a filled background for selection.
            stopPreview();
            cell.setStyle("-fx-background-color: lightblue;");
            playVideo(file);
        });
        return cell;
    }

    /**
     * Plays the full video. If the file is a zip archive, it extracts and uses the contained video.
     */
    private void playVideo(File file) {
        try {
            File videoFile = ZipHelper.tryExtract(file);
            Media media = new Media(videoFile.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            mediaView.setMediaPlayer(player);
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void previewVideo(File file) {
        // If the file is already being previewed, don't restart.
        if (file.equals(currentPreviewFile)) {
            return;
        }
        
        // Otherwise, stop any current preview and update the current file.
        stopPreview();
        currentPreviewFile = file;
        
        previewCancelled = false;
        previewThread = new Thread(() -> {
            try {
                File videoFile = ZipHelper.tryExtract(file);
                if (previewCancelled) return;
                Media media = new Media(videoFile.toURI().toString());
                Platform.runLater(() -> {
                    if (previewCancelled) return;
                    previewPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(previewPlayer);
                    previewPlayer.play();
                    previewStopTimer = new PauseTransition(Duration.seconds(10));
                    previewStopTimer.setOnFinished(e -> stopPreview());
                    previewStopTimer.play();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        previewThread.setDaemon(true);
        previewThread.start();
    }
    
    private void stopPreview() {
        previewCancelled = true;
        if (previewThread != null && previewThread.isAlive()) {
            previewThread.interrupt();
            previewThread = null;
        }
        if (previewStopTimer != null) {
            previewStopTimer.stop();
            previewStopTimer = null;
        }
        if (previewPlayer != null) {
            previewPlayer.stop();
            previewPlayer.dispose();
            previewPlayer = null;
        }
        currentPreviewFile = null;
    }
    

    private void startWatcherThread() {
        Thread watcher = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                    updateVideoGrid();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }
}
