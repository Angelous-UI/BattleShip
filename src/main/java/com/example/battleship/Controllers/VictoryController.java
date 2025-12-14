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

    /**
     * Sets the stage for this controller.
     *
     * @param stage the stage to be associated with this controller
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Sets the game statistics and initializes the victory/defeat video.
     * <p>
     * This method must be called after the controller is initialized and before
     * the view is displayed. It stores all game statistics and triggers the
     * video setup with the appropriate animation.
     * </p>
     *
     * @param playerName the name of the player
     * @param playerWon true if the player won, false if the player lost
     * @param playerShipsSunk number of enemy ships sunk by the player
     * @param machineShipsSunk number of player ships sunk by the machine
     * @param playerShots total number of shots fired by the player
     * @param playerHits total number of successful hits by the player
     * @param playerMisses total number of missed shots by the player
     * @param machineMisses total number of missed shots by the machine
     */
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

        setupBackgroundVideo();
    }

    /**
     * Initializes the controller.
     * <p>
     * The video initialization is deferred until {@link #setGameStats} is called
     * to ensure all game data is available before starting the animation.
     * </p>
     *
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    /**
     * Sets up and plays the background video based on game outcome.
     * <p>
     * Loads either the victory or defeat video, configures the media player,
     * and schedules a transition to the statistics view after the video duration.
     * Victory videos play for 5 seconds, defeat videos for 1.5 seconds.
     * </p>
     */
    private void setupBackgroundVideo() {
        Platform.runLater(() -> {
            try {
                System.out.println("🎬 Starting video...");

                String videoPath;

                if (playerWon) {
                    videoPath = getClass().getResource("/Battleship-Videos/Victory.mp4").toExternalForm();
                } else {
                    videoPath = getClass().getResource("/Battleship-Videos/Defeat.mp4").toExternalForm();
                }

                Media media = new Media(videoPath);
                mediaPlayer = new MediaPlayer(media);

                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
                mediaView.setPreserveRatio(false);

                videoContainer.getChildren().add(0, mediaView);

                mediaPlayer.setOnReady(() -> {
                    System.out.println("✅ Video ready, playing...");
                    mediaPlayer.play();

                    double duration = playerWon ? 5 : 1.5;

                    PauseTransition pause = new PauseTransition(Duration.seconds(duration));
                    pause.setOnFinished(e -> loadStatsView());
                    pause.play();
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("❌ Video error: " + mediaPlayer.getError());
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> loadStatsView());
                    pause.play();
                });

                mediaPlayer.setVolume(0.5);

            } catch (Exception e) {
                System.err.println("❌ Error loading video: " + e.getMessage());
                e.printStackTrace();
                loadStatsView();
            }
        });
    }

    /**
     * Loads the statistics view with the game results.
     * <p>
     * Stops the current video, creates a new statistics view instance,
     * passes all game statistics to it, and closes the victory/defeat window.
     * </p>
     */
    private void loadStatsView() {
        try {
            System.out.println("📊 Loading statistics screen...");

            stopVideo();

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

            Stage currentStage = (Stage) videoContainer.getScene().getWindow();
            currentStage.close();

            statsView.show();

        } catch (Exception e) {
            System.err.println("❌ Error loading Stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stops and disposes of the video player.
     * <p>
     * Safely releases media resources and nullifies the media player reference.
     * </p>
     */
    public void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}