package com.example.battleship.Views;

import com.example.battleship.Controllers.MainMenuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * View responsible for displaying the main menu window.
 * <p>
 * Implements a singleton pattern to ensure only one main menu instance exists at a time.
 * Loads the MainMenu.fxml file, attaches the MainMenuController, and manages the lifecycle
 * of the main menu stage including proper video cleanup on close.
 * </p>
 *
 * @author Battleship Team
 * @version 1.0
 */
public class MainMenuView extends Stage {

    private MainMenuController controller;

    /**
     * Private constructor for MainMenuView.
     * <p>
     * Loads the FXML layout, initializes the controller, configures the stage,
     * and sets up the close request handler to properly dispose of resources.
     * </p>
     *
     * @throws IOException if FXML loading fails
     */
    private MainMenuView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(this);

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Battleship Main Menu");
        setResizable(false);

        setOnCloseRequest(event -> {
            controller.stopVideo();
            Holder.INSTANCE = null;
        });

        show();
    }

    /**
     * Holder class for lazy-loaded singleton instance.
     * <p>
     * Uses the initialization-on-demand holder idiom for thread-safe singleton creation.
     * </p>
     */
    private static class Holder {
        private static MainMenuView INSTANCE = null;
    }

    /**
     * Gets the controller attached to this view.
     *
     * @return the MainMenuController instance
     */
    public MainMenuController getController() {
        return controller;
    }

    /**
     * Retrieves the singleton instance of MainMenuView.
     * <p>
     * Creates a new instance if one doesn't exist, otherwise returns the existing instance.
     * </p>
     *
     * @return the MainMenuView singleton instance
     * @throws IOException if FXML fails to load
     */
    public static MainMenuView getInstance() throws IOException {
        Holder.INSTANCE = Holder.INSTANCE != null ? Holder.INSTANCE : new MainMenuView();
        return Holder.INSTANCE;
    }

    /**
     * Deletes the singleton instance and closes the view.
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