package com.example.battleship.Model.Game;


public class GameStateHolder {
    private static Game savedGame = null;


    public static void saveGame(Game game) {
        savedGame = game;
        System.out.println("Juego guardado temporalmente en memoria");
    }


    public static Game getSavedGame() {
        return savedGame;
    }


    public static boolean hasSavedGame() {
        return savedGame != null &&
                savedGame.getCurrentState() == Game.GameState.PLAYING;
    }

    public static void clearSavedGame() {
        savedGame = null;
        System.out.println("Juego temporal eliminado");
    }
}