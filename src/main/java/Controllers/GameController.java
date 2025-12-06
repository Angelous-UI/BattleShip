package com.example.battleship.Controllers;

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

public class GameController implements Initializable {

    @FXML
    private AnchorPane videoContainer;

    private MediaPlayer mediaPlayer;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Game initialized");
        setupBackgroundVideo();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupBackgroundVideo() {
        try {
            String videoPath = getClass().getResource("/Battleship-Videos/Game.mp4").toExternalForm();
            Media media = new Media(videoPath);
            mediaPlayer = new MediaPlayer(media);

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
            mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
            mediaView.setPreserveRatio(false);

            videoContainer.getChildren().add(mediaView);

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });

            mediaPlayer.setOnReady(() -> {
                Platform.runLater(() -> {
                    if (stage != null) {
                        stage.show();
                    }
                });
                mediaPlayer.play();
            });

            mediaPlayer.setVolume(0.3);

        } catch (Exception e) {
            System.err.println("Error while trying playing the video: " + e.getMessage());
            e.printStackTrace();
            videoContainer.setStyle("-fx-background-color: #001a33;");
            if (stage != null) {
                stage.show();
            }
        }
    }

    public void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}