package com.example.battleship.Views;

import com.example.battleship.Controllers.StatsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * View for displaying game statistics after a match concludes.
 * <p>
 * Loads the Stats.fxml layout and provides access to the StatsController
 * for displaying detailed game results including ships sunk, accuracy, and player performance.
 * Unlike other views, this does not implement the singleton pattern as multiple
 * statistics screens may be viewed sequentially.
 * </p>
 *
 * @author Battleship Team
 * @version 1.0
 */
public class StatsView extends Stage {

    private StatsController controller;

    /**
     * Constructor for StatsView.
     * <p>
     * Loads the FXML layout, initializes the controller, configures the stage,
     * and prepares the window for display.
     * </p>
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    public StatsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Stats.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(this);

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Statistics - Battleship");
        setResizable(false);
    }

    /**
     * Gets the controller associated with this view.
     *
     * @return the StatsController instance
     */
    public StatsController getController() {
        return controller;
    }
}