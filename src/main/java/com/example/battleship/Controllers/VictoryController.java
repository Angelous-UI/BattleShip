package com.example.battleship.Controllers;

import com.example.battleship.Views.StatsView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the victory/defeat screen.
 * <p>
 * Displays an appropriate video animation based on the game outcome (victory or defeat)
 * and transitions to the statistics view after the animation completes.
 * </p>
 *
 * @author Battleship Team
 * @version 1.0
 */
public class VictoryController implements Initializable {

    @FXML
    private AnchorPane videoContainer;

    private MediaPlayer mediaPlayer;
    private Stage stage;
    private String playerName;
    private boolean playerWon;
    private int playerShipsSunk;
    private int machineShipsSunk;
    private int playerShots;
    private int playerHits;
    private int playerMisses;
    private int machineMisses;

    private volatile boolean isTransitioning = false;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setGameStats(String playerName, boolean playerWon, int playerShipsSunk,
                             int machineShipsSunk, int playerShots, int playerHits,
                             int playerMisses, int machineMisses) {
        this.playerName = playerName;
        this.playerWon = playerWon;
        this.playerShipsSunk = playerShipsSunk;
        this.machineShipsSunk = machineShipsSunk;
        this.playerShots = playerShots;
        this.playerHits = playerHits;
        this.playerMisses = playerMisses;
        this.machineMisses = machineMisses;

        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.millis(300));
            delay.setOnFinished(e -> setupBackgroundVideo());
            delay.play();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    /**
     * Sets up and plays the background video based on game outcome.
     *
     */
    private void setupBackgroundVideo() {
        if (isTransitioning) {
            System.out.println("⚠️ Ya hay una transición en progreso");
            return;
        }

        Platform.runLater(() -> {
            try {
                System.out.println("🎬 Starting victory/defeat video...");

                videoContainer.getChildren().removeIf(n -> n instanceof MediaView);

                String videoPath;
                if (playerWon) {
                    videoPath = getClass().getResource("/Battleship-Videos/Victory.mp4").toExternalForm();
                } else {
                    videoPath = getClass().getResource("/Battleship-Videos/Defeat.mp4").toExternalForm();
                }

                Media media = new Media(videoPath);
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(0.5);

                mediaPlayer.setCycleCount(1);

                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
                mediaView.setPreserveRatio(false);
                mediaView.setMouseTransparent(true);

                videoContainer.getChildren().add(0, mediaView);

                mediaPlayer.setOnReady(() -> {
                    System.out.println("✅ Video ready - Preloading...");

                    mediaPlayer.pause();

                    mediaPlayer.seek(Duration.seconds(0.5));

                    PauseTransition preloadWait = new PauseTransition(Duration.millis(400));
                    preloadWait.setOnFinished(e1 -> {
                        System.out.println("🔄 Buffer loaded, seeking to start...");

                        mediaPlayer.seek(Duration.ZERO);

                        PauseTransition finalWait = new PauseTransition(Duration.millis(200));
                        finalWait.setOnFinished(e2 -> {
                            System.out.println("▶️ Playing video NOW");
                            mediaPlayer.play();

                            double duration = playerWon ? 5.0 : 1.5;
                            scheduleStatsTransition(duration);
                        });
                        finalWait.play();
                    });
                    preloadWait.play();
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("❌ Video error: " + mediaPlayer.getError());
                    mediaPlayer.getError().printStackTrace();

                    scheduleStatsTransition(0.5);
                });

                mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                    System.out.println("📊 Video status: " + oldStatus + " → " + newStatus);
                });

            } catch (Exception e) {
                System.err.println("❌ Error loading video: " + e.getMessage());
                e.printStackTrace();
                scheduleStatsTransition(0.5);
            }
        });
    }

    /**
     * Programa la transición a la vista de estadísticas
     */
    private void scheduleStatsTransition(double seconds) {
        if (isTransitioning) {
            System.out.println("⚠️ Transición ya programada, ignorando duplicado");
            return;
        }

        isTransitioning = true;

        PauseTransition pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> loadStatsView());
        pause.play();
    }

    /**
     * Loads the statistics view with the game results.
     *
     */
    private void loadStatsView() {
        if (!isTransitioning) {
            System.out.println("⚠️ loadStatsView llamado sin flag de transición");
            return;
        }

        Platform.runLater(() -> {
            try {
                System.out.println("📊 Loading statistics screen...");

                stopVideo();

                Stage currentStage = (Stage) videoContainer.getScene().getWindow();

                StatsView statsView = new StatsView();
                statsView.getController().setGameStats(
                        playerName,
                        playerWon,
                        playerShipsSunk,
                        machineShipsSunk,
                        playerShots,
                        playerHits,
                        playerMisses,
                        machineMisses
                );

                statsView.show();

                PauseTransition delay = new PauseTransition(Duration.millis(100));
                delay.setOnFinished(e -> {
                    currentStage.close();
                    System.out.println("✅ Victory window closed");
                });
                delay.play();

            } catch (Exception e) {
                System.err.println("❌ Error loading Stats: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isTransitioning = false;
            }
        });
    }

    /**
     * Stops and disposes of the video player.
     *
     */
    public void stopVideo() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                System.err.println("⚠️ Error stopping video: " + e.getMessage());
            } finally {
                mediaPlayer = null;
            }
        }
    }
}