package com.example.battleship;

import com.example.battleship.Views.MainMenuView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación Batalla Naval.
 *
 * @author Tu Nombre
 * @version 1.0
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainMenuView.getInstance();

    }

    public static void main(String[] args)
    {
        launch();
    }
}