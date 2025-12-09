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
        Game game = new Game();
        Human player = new Human("a");

        game.generateFleet();

        System.out.println("=== TABLERO INICIAL ===");
        game.getBoard().printBoard();

        System.out.println("\n=== COORDENADAS DE LA FLOTA ===");
        game.printFleetCoordinates();

        System.out.println("\n=== DISPAROS ===");

        // Hundir el Submarine completo (fila 3, columnas 4,5,6)
        game.playTurn(game.getBoard(), player, 3, 4);  // Hit 1
        game.playTurn(game.getBoard(), player, 3, 5);  // Hit 2
        game.playTurn(game.getBoard(), player, 3, 6);  // Hit 3 - ¡HUNDIDO!

        System.out.println("\n=== TABLERO DESPUÉS DE DISPARAR ===");
        game.getBoard().printBoard();
    }
}