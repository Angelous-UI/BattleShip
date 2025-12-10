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

public class VictoryController implements Initializable {

    @FXML
    private AnchorPane videoContainer;
    private MediaPlayer mediaPlayer;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupBackgroundVideo();
    }

    private void setupBackgroundVideo() {
        Platform.runLater(() -> {
            try {
                System.out.println("1. Starting video");

                videoContainer.getChildren().removeIf(node -> node instanceof MediaView);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }

                String videoPath = getClass().getResource("/Battleship-Videos/Victory.mp4").toExternalForm();
                System.out.println("2. Video path: " + videoPath);

                Media media = new Media(videoPath);
                mediaPlayer = new MediaPlayer(media);

                System.out.println("3. MediaPlayer created");

                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
                mediaView.setPreserveRatio(false);
                mediaView.setMouseTransparent(true);

                videoContainer.getChildren().add(0, mediaView);
                System.out.println("4. MediaView added. childs in videoContainer: " + videoContainer.getChildren().size());

                // NO hacer loop, queremos que termine
                mediaPlayer.setOnEndOfMedia(() -> {
                    System.out.println("Video terminado, esperando 5 segundos...");
                    // Esperar 5 segundos después de que termine el video
                    PauseTransition pause = new PauseTransition(Duration.seconds(5));
                    pause.setOnFinished(e -> loadStatsView());
                    pause.play();
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("ERROR in mediaPlayer: " + mediaPlayer.getError());
                    // Si hay error, ir directo a Stats después de 2 segundos
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> loadStatsView());
                    pause.play();
                });

                mediaPlayer.setOnReady(() -> {
                    System.out.println("5. Video Ready");
                    mediaPlayer.play();
                    System.out.println("6. Running video");

                    if (stage != null && !stage.isShowing()) {
                        stage.show();
                    }

                    // OPCIÓN: Si quieres que pase a Stats después de 5 segundos desde que INICIA
                    // (sin esperar a que termine el video), descomenta esto:
                    /*
                    PauseTransition pause = new PauseTransition(Duration.seconds(5));
                    pause.setOnFinished(e -> loadStatsView());
                    pause.play();
                    */
                });

                mediaPlayer.setVolume(0.3);

            } catch (Exception e) {
                System.err.println("EXCEPCIÓN en setupBackgroundVideo: " + e.getMessage());
                e.printStackTrace();
                videoContainer.setStyle("-fx-background-color: #001a33;");
                if (stage != null) {
                    stage.show();
                }
            }
        });
    }

    private void loadStatsView() {
        try {
            System.out.println("Cargando Stats view...");

            // Detener el video antes de cambiar
            stopVideo();

            StatsView statsView = new StatsView();

            Stage currentStage = (Stage) videoContainer.getScene().getWindow();
            currentStage.close();

            statsView.show();

        } catch (Exception e) {
            System.err.println("Error loading Stats view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}