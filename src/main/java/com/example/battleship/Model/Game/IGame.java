package com.example.battleship.Model.Game;
import com.example.battleship.Model.Board.Board;
import com.example.battleship.Model.Exceptions.InvalidPositionException;
import com.example.battleship.Model.Exceptions.InvalidShotException;
import com.example.battleship.Model.Player.Human;
import com.example.battleship.Model.Ship.IShip;

import java.util.List;
/**
 * Defines the core behaviors required for any Battleship game implementation.
 * Provides methods for turn management, ship placement, fleet handling, and
 * board interaction.
 */
public interface IGame {
    /**
     * Generates and automatically places the full fleet of ships onto the board.
     */
    void generateFleet();

    /**
     * Attempts to manually place a ship on the board.
     *
     * @param ship the ship to place
     * @throws InvalidPositionException if the ship placement is invalid
     */

    void placeShip(IShip ship) throws  InvalidPositionException;

    /**
     * Executes a single turn of the game by processing a shot fired at a
     * specific board coordinate.
     *
     * @param board  the board where the shot is fired
     * @param player the player performing the shot
     * @param row    target row
     * @param col    target column
     * @return {@code true} if the shot hit a ship, allowing the player to shoot again
     * @throws InvalidShotException if the shot is repeated or invalid
     */

    boolean playTurn(Board board, Human player, int row, int col) throws InvalidShotException;

    /**
     * Advances the turn to the next player.
     */
    void advanceTurn();

    /**
     * Indicates whether the game has concluded.
     *
     * @return {@code true} if the game is over
     */
    boolean isGameOver();

    /**
     * Returns the game board.
     *
     * @return the board instance
     */
    Board getMachineBoard();

    /**
     * Returns the player whose turn is currently active.
     *
     * @return the active player
     */
    Object getCurrentPlayer();

    /**
     * Retrieves all players currently participating in the game.
     *
     * @return a list of players
     */
    List<Object> getPlayers();

    /**
     * Prints the board coordinates of every ship in the fleet.
     */
    void printFleetCoordinates();
    /**
     * Returns the list of ships currently placed on the board.
     *
     * @return the list of ships
     */
    public List<IShip> getMachineFleet();

    public Human getHuman();


    Game.GameState getCurrentState();
}