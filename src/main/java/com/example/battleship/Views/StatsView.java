package com.example.battleship.Views;

import com.example.battleship.Controllers.StatsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StatsView extends Stage {
    private StatsController controller;

    public StatsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Stats.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(this);

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Estadísticas - Battleship");
        setResizable(false);
    }

    public StatsController getController() {
        return controller;
    }
}