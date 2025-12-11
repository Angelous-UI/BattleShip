package com.example.battleship.Views;

import com.example.battleship.Controllers.VictoryController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;


/**
 * View displayed when the player wins the game.
 * <p>
 * This window is modal, preventing interaction with the parent window.
 * It uses a singleton pattern to ensure only one victory screen exists.
 */
public class VictoryView extends Stage {
    private    VictoryController controller;

    /**
     * Private constructor that loads Victory.fxml and sets up the modal window.
     *
     * @throws IOException if the FXML cannot be loaded
     */
    private VictoryView() throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Victory.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Battleship victory");
        setResizable(false);

        //Volemos modal entonces no puede interactuar con la ventana padre
        initModality(Modality.APPLICATION_MODAL);

        //Certrar ventana
        setOnCloseRequest(event->{
            Holder.INSTANCE=null;
        });
        show();
    }

    /**
     * @return the controller for this view
     */
    public VictoryController getController() {
        return controller;
    }

    private static class Holder{
        private static VictoryView INSTANCE=null;
    }

    /**
     * @return the singleton instance of VictoryView
     * @throws IOException if loading fails
     */
    public static VictoryView getInstance() throws IOException{
        Holder.INSTANCE=Holder.INSTANCE !=null? Holder.INSTANCE: new VictoryView();
        return Holder.INSTANCE;
    }

    /**
     * Closes and deletes the current singleton instance.
     */
    public static void deleteInstance(){
        if (Holder.INSTANCE !=null){
            Holder.INSTANCE.close();
            Holder.INSTANCE = null;
        }
    }

}
