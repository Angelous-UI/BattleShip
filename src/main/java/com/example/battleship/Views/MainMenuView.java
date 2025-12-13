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
 * Loads the MainMenu.fxml file, attaches the MainMenuController,
 * and manages a singleton instance of this view.
 */
public class MainMenuView extends Stage {

    private MainMenuController controller;

    /**
     * Private constructor for MainMenuView.
     *
     * @throws IOException if FXML loading fails
     */
    private MainMenuView() throws IOException{
        FXMLLoader loader= new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(this);

        Scene scene = new Scene (root);
        setScene(scene);
        setTitle("Battleship Main Menu");
        setResizable(false);


        setOnCloseRequest(event -> {
            controller.stopVideo();
            Holder.INSTANCE= null;
        });

        show();

    }

    private static class Holder{
        private static MainMenuView INSTANCE=null;
    }

    /**
     * @return the controller attached to this view
     */
    public MainMenuController getController(){
        return controller;
    }

    /**
     * Retrieves the single instance of MainMenuView.
     *
     * @return MainMenuView instance
     * @throws IOException if FXML fails to load
     */
    public static MainMenuView getInstance() throws IOException{
        Holder.INSTANCE=Holder.INSTANCE !=null ? Holder.INSTANCE : new MainMenuView();
        return Holder.INSTANCE;
    }

    /**
     * Deletes the singleton instance and closes the view.
     */
    public static void deleteInstance(){
        if ( Holder.INSTANCE !=null){
            Holder.INSTANCE.getController().stopVideo();
            Holder.INSTANCE.close();
            Holder.INSTANCE = null;
        }
    }


}