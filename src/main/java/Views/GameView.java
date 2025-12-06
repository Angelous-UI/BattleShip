package com.example.battleship.views;

import com.example.battleship.Controllers.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class GameView extends Stage {

    private GameController controller;

    public GameView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Game.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root, 900, 600);

        this.setTitle("Batalla Naval - Game");
        this.initStyle(StageStyle.UNDECORATED);
        this.setScene(scene);
        this.setResizable(false);
    }

    public GameController getController() {
        return controller;
    }
}