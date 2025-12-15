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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.battleship.Views.GameView;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Controller for the main menu view.
 * <p>
 * Handles UI interactions, button animations, explosion effects and background video playback in the main menu.
 * Manages game state persistence through username-based save files and provides navigation to game sessions.
 * </p>
 *
 * @author Battleship Team
 * @version 1.0
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

    @FXML
    private TextField usernameField;

    @FXML
    private Label statusLabel;

    private MediaPlayer mediaPlayer;
    private Stage stage;
    private String savedPlayerName = "";

    private SerializableFileHandler serializableHandler = new SerializableFileHandler();

    /**
     * Initializes the main menu controller.
     * <p>
     * Sets up the background video, adds explosion effects to all buttons,
     * and configures listeners for username field changes to update the continue button state.
     * </p>
     *
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupBackgroundVideo();
        addExplosionEffect(playButton);
        addExplosionEffect(continueButton);
        addExplosionEffect(exitButton);

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateContinueButton();
            if (statusLabel != null && !newVal.trim().isEmpty()) {
                statusLabel.setText("");
            }
        });

        updateContinueButton();
    }

    /**
     * Updates the continue button state based on saved game availability.
     * <p>
     * Checks if a saved game file exists for the entered username.
     * Enables the button if a save file is found, disables it otherwise.
     * </p>
     */
    private void updateContinueButton() {
        try {
            String username = usernameField.getText().trim().toLowerCase();

            if (username.isEmpty()) {
                continueButton.setDisable(true);
                continueButton.setOpacity(0.5);
                return;
            }

            String filename = "game_save_" + username + ".dat";
            GameState savedState = (GameState) serializableHandler.deserialize(filename);
            boolean hasSave = (savedState != null);

            continueButton.setDisable(!hasSave);

            if (!hasSave) {
                continueButton.setOpacity(0.5);
            } else {
                continueButton.setOpacity(1.0);
                savedPlayerName = username;
            }

            System.out.println((hasSave ? "✅" : "⚠️") + " Save game for " + username + ": " + hasSave);

        } catch (Exception e) {
            continueButton.setDisable(true);
            continueButton.setOpacity(0.5);
            System.out.println("⚠️ No saved game found");
        }
    }

    /**
     * Adds a click-triggered explosion animation and button shake effect to a button.
     * <p>
     * When clicked, the button triggers particle explosion effects and shakes,
     * then executes the corresponding action after a short delay.
     * </p>
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
     * <p>
     * Generates multiple colored particles that radiate outward from the center point,
     * fading and shrinking as they travel.
     * </p>
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
     * <p>
     * Creates a rapid back-and-forth motion to simulate an impact effect.
     * </p>
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
     * <p>
     * Handles video initialization, resizing to fit the container,
     * automatic looping, and error recovery. Falls back to a solid color
     * background if video loading fails.
     * </p>
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
                System.out.println("4. MediaView added. Children in videoContainer: " + videoContainer.getChildren().size());

                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("ERROR in mediaPlayer: " + mediaPlayer.getError());
                    mediaPlayer.dispose();
                    setupBackgroundVideo();
                });

                mediaPlayer.setOnReady(() -> {
                    System.out.println("5. Video Ready");
                    mediaPlayer.play();
                    System.out.println("6. Playing video");

                    if (stage != null && !stage.isShowing()) {
                        stage.show();
                    }
                });

                mediaPlayer.setVolume(0.3);

            } catch (Exception e) {
                System.err.println("EXCEPTION in setupBackgroundVideo: " + e.getMessage());
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
     * <p>
     * Properly disposes of media resources and performs garbage collection
     * before transitioning to the game screen.
     * </p>
     */
    private void loadGameView() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }

            System.gc();

            Stage currentStage = (Stage) playButton.getScene().getWindow();

            GameView.deleteInstance();
            MainMenuView.deleteInstance();

            GameView gameView = GameView.getInstance();

            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "New Game" button action.
     * <p>
     * Validates the username input, deletes any existing save file for the player,
     * displays a status message, and initializes a new game session after a brief delay.
     * </p>
     */
    @FXML
    private void onNewGame() {
        String username = usernameField.getText();

        if (username == null || username.trim().isEmpty()) {
            flashUsernameField();
            if (statusLabel != null) {
                statusLabel.setText("⚠️ Ingresa Tu Nombre Para Iniciar!");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
            return;
        }

        username = username.trim();

        String filename = "game_save_" + username.toLowerCase() + ".dat";
        File saveFile = new File(filename);

        if (saveFile.exists()) {
            statusLabel.setText("🗑️ Partida De " + username + " Ha Sido Borrada. Iniciando Nueva Partida...");
            statusLabel.setStyle("-fx-text-fill: #FFA500; -fx-font-weight: bold;");
            deleteGameSaveFile(username);
        } else {
            statusLabel.setText("🎮 Iniciando Una Nueva Partida Para " + username + "...");
            statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        }

        savedPlayerName = username;

        System.out.println("Starting Game with player: " + username);

        PauseTransition delay = new PauseTransition(Duration.millis(1500));
        String finalUsername = username;
        delay.setOnFinished(e -> {
            stopVideo();

            try {
                GameView.deleteInstance();
                GameView gameView = GameView.getInstance();
                gameView.getController().initializeNewGame(finalUsername);

                Stage currentStage = (Stage) playButton.getScene().getWindow();
                currentStage.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        delay.play();
    }

    /**
     * Applies a visual flash effect to the username field.
     * <p>
     * Used to indicate invalid input by temporarily showing a red border.
     * </p>
     */
    private void flashUsernameField() {
        String errorStyle = "-fx-border-color: #e74c3c; -fx-border-width: 2px;";

        usernameField.setStyle(errorStyle);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            usernameField.setStyle(null);
        });
        pause.play();
    }

    /**
     * Handles the "Continue" button action.
     * <p>
     * Validates the username, loads the corresponding saved game state,
     * and resumes the game session. Displays an error message if no save file is found.
     * </p>
     */
    @FXML
    private void onContinue() {
        String username = usernameField.getText().trim().toLowerCase();

        if (username.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("⚠️ Enter your name first");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
            flashUsernameField();
            return;
        }

        System.out.println("Loading Game for player: " + username);

        String filename = "game_save_" + username + ".dat";
        GameState savedState = (GameState) serializableHandler.deserialize(filename);

        if (savedState != null) {
            System.out.println("✅ Save game found: " + savedState.getGamePhase());

            savedPlayerName = username;

            stopVideo();
            try {
                GameView.deleteInstance();
                GameView gameView = GameView.getInstance();

                gameView.getController().loadSavedGame(savedState, username);

                Stage currentStage = (Stage) continueButton.getScene().getWindow();
                currentStage.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ No save game found for: " + username);
            if (statusLabel != null) {
                statusLabel.setText("❌ No saved game for " + username);
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * Deletes the save game file for the specified player.
     *
     * @param playerName the name of the player whose save file should be deleted
     */
    private void deleteGameSaveFile(String playerName) {
        String fileName = "game_save_" + playerName.toLowerCase().trim() + ".dat";
        try {
            File saveFile = new File(fileName);
            if (saveFile.exists()) {
                if (saveFile.delete()) {
                    System.out.println("🗑️ File deleted: " + fileName);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error deleting file: " + e.getMessage());
        }
    }

    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param type the type of alert
     * @param title the alert title
     * @param header the alert header text
     * @param content the alert content text
     */
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
     * <p>
     * Stops video playback and terminates the application.
     * </p>
     */
    @FXML
    private void onExit() {
        stopVideo();
        System.exit(0);
    }

    /**
     * Stops and disposes of the background video player.
     * <p>
     * Safely handles disposal even if the media player is already stopped or disposed.
     * </p>
     */
    public void stopVideo() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
                    mediaPlayer.stop();
                }
                mediaPlayer.dispose();
            } catch (Exception e) {
                System.err.println("⚠️ Error stopping video: " + e.getMessage());
            } finally {
                mediaPlayer = null;
            }
        }
    }
}