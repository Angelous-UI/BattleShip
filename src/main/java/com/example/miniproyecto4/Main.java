package com.example.miniproyecto4;

import com.example.miniproyecto4.Model.board.Board;
import com.example.miniproyecto4.Model.game.Game;
import com.example.miniproyecto4.Model.player.Human;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        Board board= new Board();
        Game game = new Game();
        Human player = new Human("a");
        game.generateFleet();
        game.getBoard().printBoard();
        game.playTurn(board, player, 2,5);
        game.playTurn(board, player, 3,7);
        game.playTurn(board, player, 1,8);
        game.printFleetCoordinates();

    }
}
