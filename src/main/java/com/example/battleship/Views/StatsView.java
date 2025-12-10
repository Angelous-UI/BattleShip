package com.example.battleship.Views;

import com.example.battleship.Controllers.StatsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StatsView {

    public void show() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Stats.fxml"));
        Parent root = loader.load();

        StatsController controller = loader.getController();

        Stage stage = new Stage();
        controller.setStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Stats - Battleship");
        stage.show();
    }
}