package com.example.battleship.Controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class StatsController implements Initializable {

    @FXML private AnchorPane statsBackground;
    @FXML private Label winnerLabel;
    @FXML private Label playerShipsSunkLabel;
    @FXML private Label machineShipsSunkLabel;
    @FXML private Label playerMissesLabel;
    @FXML private Label machineMissesLabel;
    @FXML private Button backToMenuButton;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    public void setGameStats(boolean playerWon, int playerShipsSunk, int machineShipsSunk,
                             int playerMisses, int machineMisses) {

        if (playerWon) {
            winnerLabel.setText("🏆 ¡VICTORIA! 🏆");
            winnerLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 36px; -fx-font-weight: bold;");
        } else {
            winnerLabel.setText("💀 DERROTA 💀");
            winnerLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 36px; -fx-font-weight: bold;");
        }

        playerShipsSunkLabel.setText("Barcos enemigos hundidos: " + playerShipsSunk);
        machineShipsSunkLabel.setText("Tus barcos hundidos: " + machineShipsSunk);
        playerMissesLabel.setText("Tus disparos fallidos: " + playerMisses);
        machineMissesLabel.setText("Fallos de la máquina: " + machineMisses);
    }

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

    private void loadMenu() {
        try {
            com.example.battleship.Views.MainMenuView.deleteInstance();
            com.example.battleship.Views.MainMenuView mainMenu = com.example.battleship.Views.MainMenuView.getInstance();

            Stage currentStage = (Stage) backToMenuButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            System.err.println("Error cargando el menú: " + e.getMessage());
            e.printStackTrace();
        }
    }


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
