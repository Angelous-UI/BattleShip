package com.example.battleship.Views;

import com.example.battleship.Controllers.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Singleton view responsible for loading and displaying the game window.
 * <p>
 * Loads the Game.fxml layout, attaches the GameController, and handles
 * cleanup when the window closes. Ensures only one game instance exists at a time.
 * </p>
 *
 * @author Battleship Team
 * @version 1.0
 */
public class GameView extends Stage {

    private GameController controller;

    /**
     * Private constructor for the GameView singleton.
     * <p>
     * Loads the FXML layout, initializes the controller, configures the stage,
     * sets up the close request handler, and notifies the controller when the scene is ready.
     * </p>
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    private GameView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Game.fxml"));
        Parent root = loader.load();

        controller = loader.getController();
        controller.setStage(this);

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("BattleShip Game");
        setResizable(false);

        setOnCloseRequest(event -> {
            controller.stopVideo();
            Holder.INSTANCE = null;
        });

        show();

        controller.onSceneReady();
    }

    /**
     * Gets the controller associated with this view.
     *
     * @return the GameController instance
     */
    public GameController getController() {
        return controller;
    }

    /**
     * Holder class for thread-safe lazy-loaded singleton.
     * <p>
     * Uses the initialization-on-demand holder idiom for thread-safe singleton creation.
     * </p>
     */
    private static class Holder {
        private static GameView INSTANCE = null;
    }

    /**
     * Returns the singleton instance of GameView.
     * <p>
     * Creates a new instance if one doesn't exist, otherwise returns the existing instance.
     * </p>
     *
     * @return the GameView singleton instance
     * @throws IOException if loading fails
     */
    public static GameView getInstance() throws IOException {
        Holder.INSTANCE = Holder.INSTANCE != null ? Holder.INSTANCE : new GameView();
        return Holder.INSTANCE;
    }

    /**
     * Deletes and closes the current GameView instance.
     * <p>
     * Properly stops the background video, closes the stage, and nullifies
     * the singleton reference to allow garbage collection.
     * </p>
     */
    public static void deleteInstance() {
        if (Holder.INSTANCE != null) {
            Holder.INSTANCE.getController().stopVideo();
            Holder.INSTANCE.close();
            Holder.INSTANCE = null;
        }
    }
}