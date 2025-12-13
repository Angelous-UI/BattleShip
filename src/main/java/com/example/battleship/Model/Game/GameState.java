package com.example.battleship.Model.Game;

import com.example.battleship.Model.Board.Board;
import com.example.battleship.Model.Ship.IShip;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Represents a serializable snapshot of the game state.
 * <p>
 * This class is used to persist and restore the current game progress,
 * including boards, fleets, shot history, turn information, and game phase.
 * </p>
 *
 * <p>
 * It implements {@link Serializable} to allow saving the game state
 * to disk and loading it later.
 * </p>
 */
public class GameState implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** Name of the human player. */
    private String playerName;

    /** Board belonging to the human player. */
    private Board humanBoard;

    /** Board belonging to the machine player. */
    private Board machineBoard;

    /** Fleet of ships owned by the human player. */
    private List<IShip> humanFleet;

    /** Fleet of ships owned by the machine player. */
    private List<IShip> machineFleet;

    /** Set containing all shots performed by the human player. */
    private Set<String> humanShots;

    /** Set containing all shots performed by the machine player. */
    private Set<String> machineShots;

    /** Index of the current player turn. */
    private int currentPlayerIndex;

    /** Current phase of the game (renamed to avoid name conflict). */
    private Game.GameState gamePhase;  // Renamed to avoid conflict

    /** Indicates whether the game has ended. */
    private boolean gameOver;

    /**
     * Creates a complete snapshot of the current game state.
     *
     * @param playerName         name of the human player
     * @param humanBoard         human player's board
     * @param machineBoard       machine player's board
     * @param humanFleet         human player's fleet
     * @param machineFleet       machine player's fleet
     * @param humanShots         shots fired by the human player
     * @param machineShots       shots fired by the machine player
     * @param currentPlayerIndex index of the active player
     * @param gamePhase          current game phase
     * @param gameOver           whether the game has finished
     */
    public GameState(String playerName, Board humanBoard, Board machineBoard,
                     List<IShip> humanFleet, List<IShip> machineFleet,
                     Set<String> humanShots, Set<String> machineShots,
                     int currentPlayerIndex, Game.GameState gamePhase, boolean gameOver) {
        this.playerName = playerName;
        this.humanBoard = humanBoard;
        this.machineBoard = machineBoard;
        this.humanFleet = humanFleet;
        this.machineFleet = machineFleet;
        this.humanShots = humanShots;
        this.machineShots = machineShots;
        this.currentPlayerIndex = currentPlayerIndex;
        this.gamePhase = gamePhase;  // Renamed
        this.gameOver = gameOver;
    }

    // ===================== GETTERS =====================

    /**
     * Returns the name of the human player.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns the human player's board.
     *
     * @return human board
     */
    public Board getHumanBoard() {
        return humanBoard;
    }

    /**
     * Returns the machine player's board.
     *
     * @return machine board
     */
    public Board getMachineBoard() {
        return machineBoard;
    }

    /**
     * Returns the human player's fleet.
     *
     * @return list of human ships
     */
    public List<IShip> getHumanFleet() {
        return humanFleet;
    }

    /**
     * Returns the machine player's fleet.
     *
     * @return list of machine ships
     */
    public List<IShip> getMachineFleet() {
        return machineFleet;
    }

    /**
     * Returns the set of shots fired by the human player.
     *
     * @return human shot history
     */
    public Set<String> getHumanShots() {
        return humanShots;
    }

    /**
     * Returns the set of shots fired by the machine player.
     *
     * @return machine shot history
     */
    public Set<String> getMachineShots() {
        return machineShots;
    }

    /**
     * Returns the index of the current player.
     *
     * @return current player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Returns the current game phase.
     *
     * @return game phase
     */
    public Game.GameState getGamePhase() {  // Renamed
        return gamePhase;
    }

    /**
     * Indicates whether the game has ended.
     *
     * @return {@code true} if the game is over
     */
    public boolean isGameOver() {
        return gameOver;
    }
}
