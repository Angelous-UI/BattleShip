package com.example.battleship.Views;

import com.example.battleship.Controllers.VictoryController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * View displayed when the game ends (victory or defeat).
 * <p>
 * This window is modal, preventing interaction with other windows while displayed.
 * Uses a singleton pattern to ensure only one victory/defeat screen exists at a time.
 * </p>
 *
 * @author Battleship Team
 * @version 1.0
 */
public class VictoryView extends Stage {

    private VictoryController controller;

    /**
     * Private constructor that loads Victory.fxml and sets up the modal window.
     * <p>
     * Initializes the controller, configures the stage as an application modal,
     * and sets up the close request handler to clean up the singleton instance.
     * </p>
     *
     * @throws IOException if the FXML cannot be loaded
     */
    private VictoryView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Victory.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Battleship Victory");
        setResizable(false);

        initModality(Modality.APPLICATION_MODAL);

        setOnCloseRequest(event -> {
            Holder.INSTANCE = null;
        });

        show();
    }

    /**
     * Gets the controller for this view.
     *
     * @return the VictoryController instance
     */
    public VictoryController getController() {
        return controller;
    }

    /**
     * Holder class for lazy-loaded singleton instance.
     * <p>
     * Uses the initialization-on-demand holder idiom for thread-safe singleton creation.
     * </p>
     */
    private static class Holder {
        private static VictoryView INSTANCE = null;
    }

    /**
     * Gets the singleton instance of VictoryView.
     * <p>
     * Creates a new instance if one doesn't exist, otherwise returns the existing instance.
     * </p>
     *
     * @return the VictoryView singleton instance
     * @throws IOException if loading fails
     */
    public static VictoryView getInstance() throws IOException {
        Holder.INSTANCE = Holder.INSTANCE != null ? Holder.INSTANCE : new VictoryView();
        return Holder.INSTANCE;
    }

    /**
     * Closes and deletes the current singleton instance.
     * <p>
     * Closes the stage and nullifies the singleton reference to allow garbage collection.
     * </p>
     */
    public static void deleteInstance() {
        if (Holder.INSTANCE != null) {
            Holder.INSTANCE.close();
            Holder.INSTANCE = null;
        }
    }
}