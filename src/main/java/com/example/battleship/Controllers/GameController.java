package com.example.battleship.Controllers;

import com.example.battleship.Views.GameView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private AnchorPane videoContainer;

    @FXML
    private Button GoBackButton;

    @FXML
    private AnchorPane menuPane2;

    private MediaPlayer mediaPlayer;
    private Stage stage;


    int[][] board = {
            {0,0,0,0,0,0,0,0},
            {1,1,1,1,0,0,0,0},
            {0,0,1,1,0,1,0,0},
            {0,0,1,1,0,1,1,0},
            {1,1,1,0,0,0,0,0},
            {0,0,1,1,0,0,0,0},
            {0,0,0,0,1,1,0,0},
            {0,0,0,0,0,1,0,0}
    };


    public void setStage(Stage stage) {
        this.stage = stage;
    }

/*    public void drawBoard() {
        Canvas canvas = new Canvas(board[0][0], board[0][1]);
    }*/

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupBackgroundVideo();
        addExplosionEffect(GoBackButton);
    }

    private void addExplosionEffect(Button button) {
        button.setOnMouseClicked(event -> {
            event.consume();

            double centerX = button.getLayoutX() + button.getWidth() / 2;
            double centerY = button.getLayoutY() + button.getHeight() / 2;

            createExplosion(centerX, centerY);
            shakeButton(button);

            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> {
                if (button == GoBackButton) {
                    onBackMenu();
                }
            });
            pause.play();
        });
    }

    private void createExplosion(double x, double y) {
        Random random = new Random();
        int particleCount = 30;

        for (int i = 0; i < particleCount; i++) {
            Circle particle = new Circle(3);
            particle.setFill(Color.rgb(255, random.nextInt(100) + 50, 0));
            particle.setLayoutX(x);
            particle.setLayoutY(y);

            menuPane2.getChildren().add(particle);

            double angle = Math.toRadians(random.nextInt(360));
            double distance = random.nextDouble() * 100 + 50;
            double targetX = x + Math.cos(angle) * distance;
            double targetY = y + Math.sin(angle) * distance;

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(particle.layoutXProperty(), x),
                            new KeyValue(particle.layoutYProperty(), y),
                            new KeyValue(particle.opacityProperty(), 1.0),
                            new KeyValue(particle.radiusProperty(), 3)
                    ),
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(particle.layoutXProperty(), targetX),
                            new KeyValue(particle.layoutYProperty(), targetY),
                            new KeyValue(particle.opacityProperty(), 0),
                            new KeyValue(particle.radiusProperty(), 0)
                    )
            );

            timeline.play();
            timeline.setOnFinished(e -> menuPane2.getChildren().remove(particle));
        }
    }

    private void shakeButton(Button button) {
        Timeline shake = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(button.translateXProperty(), 0),
                        new KeyValue(button.translateYProperty(), 0)
                ),
                new KeyFrame(Duration.millis(50),
                        new KeyValue(button.translateXProperty(), -5),
                        new KeyValue(button.translateYProperty(), 3)
                ),
                new KeyFrame(Duration.millis(100),
                        new KeyValue(button.translateXProperty(), 5),
                        new KeyValue(button.translateYProperty(), -3)
                ),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(button.translateXProperty(), -3),
                        new KeyValue(button.translateYProperty(), 2)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(button.translateXProperty(), 0),
                        new KeyValue(button.translateYProperty(), 0)
                )
        );
        shake.play();
    }

    private void loadMainMenuView() {
        try {
            GameView mainmenuView = new GameView();

            Stage currentStage = (Stage) GoBackButton.getScene().getWindow();
            currentStage.close();

            mainmenuView.show();

        } catch (Exception e) {
            System.err.println("Error loading Game view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackMenu() {
        stopVideo();
        System.out.println("Go back to main menu...");
        loadMainMenuView();
    }
}