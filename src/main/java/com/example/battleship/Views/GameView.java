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
 * cleanup when the window closes.
 */
public class GameView extends Stage {

    private GameController controller;

    /**
     * Private constructor for the GameView singleton.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    private GameView() throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Game.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(this);

        Scene scene =new Scene(root);
        setScene(scene);
        setTitle("BattleShip Game");
        setResizable(false);

        //cerrar vebntaba
        setOnCloseRequest(event ->{
            controller.stopVideo();
            Holder.INSTANCE=null;
        });

        show();
    }

    /**
     * @return the controller associated with this view
     */
    public GameController getController(){
        return controller;
    }

    /**
     * Holder class for thread-safe lazy-loaded singleton.
     */
    private static class Holder{
        private static GameView INSTANCE=null;
    }

    /**
     * Returns the singleton instance of GameView.
     *
     * @return GameView instance
     * @throws IOException if loading fails
     */
    public static GameView getInstance() throws IOException{
        Holder.INSTANCE = Holder.INSTANCE !=null ? Holder.INSTANCE : new GameView();
        return Holder.INSTANCE;
    }

    /**
     * Deletes and closes the current GameView instance.
     */
    public static void deleteInstance(){
        if(Holder.INSTANCE !=null){
            Holder.INSTANCE.getController().stopVideo();
            Holder.INSTANCE.close();
            Holder.INSTANCE=null;
        }
    }


}