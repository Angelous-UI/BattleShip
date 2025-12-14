package com.example.battleship.Controllers;

import com.example.battleship.Model.Game.Game;
import com.example.battleship.Model.Game.GameState;
import com.example.battleship.Model.Serializable.SerializableFileHandler;
import com.example.battleship.Model.TextFile.IPlaneTextFileHandler;
import com.example.battleship.Views.MainMenuView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.battleship.Views.GameView;
/*import com.example.battleship.Model.Game.GameStateHolder;*/

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * controller for the main menu view
 * Handles ui interactions, button animations, explosion effects and background video playback in the main menu
 */
public class MainMenuController implements Initializable {

    @FXML
    private AnchorPane videoContainer;

    @FXML
    private Button playButton;

    @FXML
    private Button continueButton;

    @FXML
    private Button exitButton;

    @FXML
    private AnchorPane menuPane;

    private MediaPlayer mediaPlayer;
    private Stage stage;

    private SerializableFileHandler serializableHandler = new SerializableFileHandler();

    /**
     * Initializes the main menu, setting up the background video
     * and button explosion animations.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupBackgroundVideo();
        addExplosionEffect(playButton);
        addExplosionEffect(continueButton);
        addExplosionEffect(exitButton);

        //updateContinueButton();
    }

    /**
     * Updates the continue button
     */
    private void updateContinueButton() {
        try {
            GameState savedState = (GameState) serializableHandler.deserialize("game_save.dat");
            boolean hasSave = (savedState != null);

            continueButton.setDisable(!hasSave);

            if (!hasSave) {
                continueButton.setOpacity(0.5);
            } else {
                continueButton.setOpacity(1.0);
            }

            System.out.println((hasSave ? "✅" : "⚠️") + " Partida guardada: " + hasSave);

        } catch (Exception e) {
            // Si hay error al leer, asumir que no hay partida
            continueButton.setDisable(true);
            continueButton.setOpacity(0.5);
            System.out.println("⚠️ No se encontró partida guardada");
        }
    }

    /**
     * Adds a click-triggered explosion animation and button shake effect.
     *
     * @param button the button to attach the effects to
     */
    private void addExplosionEffect(Button button) {
        button.setOnMouseClicked(event -> {
            event.consume();

            double centerX = button.getLayoutX() + button.getWidth() / 2;
            double centerY = button.getLayoutY() + button.getHeight() / 2;

            createExplosion(centerX, centerY);
            shakeButton(button);

            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> {
                if (button == playButton) {
                    onNewGame();
                } else if (button == continueButton) {
                    onContinue();
                } else if (button == exitButton) {
                    onExit();
                }
            });
            pause.play();
        });
    }

    /**
     * Creates a particle-based explosion animation at the given coordinates.
     *
     * @param x the x-coordinate of the explosion center
     * @param y the y-coordinate of the explosion center
     */
    private void createExplosion(double x, double y) {
        Random random = new Random();
        int particleCount = 30;

        for (int i = 0; i < particleCount; i++) {
            Circle particle = new Circle(3);
            particle.setFill(Color.rgb(255, random.nextInt(100) + 50, 0));
            particle.setLayoutX(x);
            particle.setLayoutY(y);

            menuPane.getChildren().add(particle);

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
            timeline.setOnFinished(e -> menuPane.getChildren().remove(particle));
        }
    }

    /**
     * Applies a shaking animation to the specified button.
     *
     * @param button the button to shake
     */
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

    /**
     * Links the controller to the main application window.
     *
     * @param stage the main stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Loads and starts playing the looping background video.
     * Handles video recreation, resizing, and error recovery.
     */
    private void setupBackgroundVideo() {
        Platform.runLater(() -> {
            try {
                System.out.println("1. Starting video");

                videoContainer.getChildren().removeIf(node -> node instanceof MediaView);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }

                String videoPath = getClass().getResource("/Battleship-Videos/MainMenu.mp4").toExternalForm();
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

                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("ERROR in mediaPlayer: " + mediaPlayer.getError());
                    // Intentar recargar
                    mediaPlayer.dispose();
                    setupBackgroundVideo();
                });

                mediaPlayer.setOnReady(() -> {
                    System.out.println("5. Video Ready");
                    mediaPlayer.play();
                    System.out.println("6. Running video");

                    if (stage != null && !stage.isShowing()) {
                        stage.show();
                    }
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

    /**
     * Closes the main menu and loads the game view.
     */
    private void loadGameView() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }

            System.gc(); // fuerza la limpieza


            Stage currentStage = (Stage) playButton.getScene().getWindow();

            GameView.deleteInstance(); // Limpia cualquier instancia previa de Game
            MainMenuView.deleteInstance(); // Limpia el MainMenu actual

            GameView gameView = GameView.getInstance(); // Crea la nueva instancia de Game

            currentStage.close(); // Cierra la ventana del menú

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "New Game" action.
     * Stops video playback and loads the game.
     */
    @FXML
    private void onNewGame() {
        stopVideo();
        System.out.println("Starting Game...");

        try {
            GameView.deleteInstance();
            GameView gameView = GameView.getInstance();

            // ✅ Inicializar nuevo juego
            gameView.getController().initializeNewGame("player");

            Stage currentStage = (Stage) playButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Continue" button action.
     * Currently only prints debug info.
     */
    @FXML
    private void onContinue() {
        System.out.println("Loading Game...");

        GameState savedState = (GameState) serializableHandler.deserialize("game_save.dat");

        if (savedState != null) {
            System.out.println(savedState.getGamePhase());
            stopVideo();
            try {
                GameView.deleteInstance();
                GameView gameView = GameView.getInstance();

                // ✅ Cargar partida guardada
                gameView.getController().loadSavedGame(savedState);

                Stage currentStage = (Stage) continueButton.getScene().getWindow();
                currentStage.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ No se encontró partida guardada");
            // Mostrar alerta al usuario
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }


    /**
     * Handles the "Exit" button action.
     * Stops video playback and exits the application.
     */
    @FXML
    private void onExit() {
        stopVideo();
        System.exit(0);
    }

    /**
     * Stops and disposes of the background video.
     */
    public void stopVideo() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
                    mediaPlayer.stop();
                }
                mediaPlayer.dispose();
            } catch (Exception e) {
                System.err.println("⚠️ Error al detener video: " + e.getMessage());
            } finally {
                mediaPlayer = null;
            }
        }
    }
}