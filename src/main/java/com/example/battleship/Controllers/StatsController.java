package com.example.battleship.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class StatsController implements Initializable {

    @FXML
    private AnchorPane statsBackground; // o como se llame tu AnchorPane

    @FXML
    private Button backButton; // tu botón de regreso

    private Stage stage;

    // AGREGA ESTE MÉTODO
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Tu código de inicialización aquí
    }

    // Tus otros métodos...
}