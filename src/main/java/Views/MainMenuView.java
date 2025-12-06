package com.example.battleship.views;

import com.example.battleship.Controllers.MainMenuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Vista del menú principal del juego.
 * Configura la ventana y carga el FXML.
 *
 * @author Tu Nombre
 * @version 1.0
 */
public class MainMenuView extends Stage {

    private MainMenuController controller;

    public MainMenuView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root, 900, 600);

        this.setTitle("Batalla Naval");
        this.initStyle(StageStyle.UNDECORATED); // ← QUITAR BORDES
        this.setScene(scene);
        this.setResizable(false);
    }


    public MainMenuController getController() {
        return controller;
    }
}