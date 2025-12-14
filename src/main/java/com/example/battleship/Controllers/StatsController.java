package com.example.battleship.Controllers;

import com.example.battleship.Model.Player.PlayerData;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Controller for the game statistics screen.
 * <p>
 * Displays detailed game statistics including ships sunk, shots fired, and accuracy.
 * Manages player data persistence and provides navigation back to the main menu.
 * </p>
 *
 * @author Battleship Team
 * @version 1.0
 */
public class StatsController implements Initializable {

    @FXML
    private AnchorPane statsBackground;

    @FXML
    private Label playerNameLabel;

    @FXML
    private Label winnerLabel;

    @FXML
    private Label playerShipsSunkLabel;

    @FXML
    private Label machineShipsSunkLabel;

    @FXML
    private Label playerMissesLabel;

    @FXML
    private Label machineMissesLabel;

    @FXML
    private Button backToMenuButton;

    private Stage stage;

    /**
     * Sets the stage for this controller.
     *
     * @param stage the stage to be associated with this controller
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Initializes the controller.
     *
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    /**
     * Sets and displays the game statistics.
     * <p>
     * Updates the player's persistent data (games played, wins, shots, hits),
     * saves it to file, and displays all statistics on the screen including:
     * player name, game outcome, ships sunk on both sides, and missed shots.
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

        PlayerData currentPlayer = PlayerData.loadPlayerData(playerName);

        if (currentPlayer == null) {
            currentPlayer = new PlayerData(playerName);
        }

        currentPlayer.incrementGamesPlayed();
        if (playerWon) {
            currentPlayer.incrementGamesWon();
        }
        currentPlayer.addShots(playerShots);
        currentPlayer.addHits(playerHits);

        PlayerData.savePlayerData(currentPlayer);

        playerNameLabel.setText("👤 JUGADOR: " + playerName);
        playerNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        if (playerWon) {
            winnerLabel.setText("🏆 VICTORIA! 🏆");
            winnerLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 36px; -fx-font-weight: bold;");
        } else {
            winnerLabel.setText("💀 DERROTA 💀");
            winnerLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 36px; -fx-font-weight: bold;");
        }

        playerShipsSunkLabel.setText("⚓ Barcos enemigos hundidos: " + playerShipsSunk + " / 10");
        machineShipsSunkLabel.setText("⚓ Tus barcos hundidos: " + machineShipsSunk + " / 10");
        playerMissesLabel.setText("💦 Tus disparos fallidos: " + playerMisses);
        machineMissesLabel.setText("💦 Fallos de la máquina: " + machineMisses);
    }

    /**
     * Handles the back to menu button action.
     * <p>
     * Triggers an explosion effect, shakes the button, and navigates back
     * to the main menu after a brief delay.
     * </p>
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onBackToMenu(ActionEvent event) {
        double x = backToMenuButton.getLayoutX() + backToMenuButton.getWidth() / 2;
        double y = backToMenuButton.getLayoutY() + backToMenuButton.getHeight() / 2;

        createExplosion(x, y);
        shakeButton(backToMenuButton);

        PauseTransition wait = new PauseTransition(Duration.millis(350));
        wait.setOnFinished(e -> loadMenu());
        wait.play();
    }

    /**
     * Loads the main menu view.
     * <p>
     * Cleans up the current statistics view and creates a new main menu instance.
     * </p>
     */
    private void loadMenu() {
        try {
            com.example.battleship.Views.MainMenuView.deleteInstance();
            com.example.battleship.Views.MainMenuView mainMenu = com.example.battleship.Views.MainMenuView.getInstance();

            Stage currentStage = (Stage) backToMenuButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            System.err.println("Error loading menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a particle-based explosion animation at the given coordinates.
     * <p>
     * Generates multiple colored particles that radiate outward from the center point,
     * fading and shrinking as they travel. Used for visual feedback on button clicks.
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

            statsBackground.getChildren().add(particle);

            double angle = Math.toRadians(random.nextInt(360));
            double distance = random.nextDouble() * 100 + 50;
            double targetX = x + Math.cos(angle) * distance;
            double targetY = y + Math.sin(angle) * distance;

            Timeline t = new Timeline(
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

            t.setOnFinished(e -> statsBackground.getChildren().remove(particle));
            t.play();
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
}