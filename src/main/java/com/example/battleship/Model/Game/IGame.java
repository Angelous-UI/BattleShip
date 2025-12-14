package com.example.battleship.Model.Game;
import com.example.battleship.Model.AI.SmartAI;
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

    /**
     * Retrieves the human player instance participating in the game.
     *
     * <p>This method provides access to the human player object, which contains
     * player-specific data such as name, shot history, and game statistics.</p>
     *
     * @return the human player instance, or {@code null} if no human player exists
     */
    Human getHuman();

    /**
     * Returns the current state of the game.
     *
     * <p>The game state indicates which phase the game is currently in:</p>
     * <ul>
     *   <li>{@link Game.GameState#SETUP} - Players are placing their ships</li>
     *   <li>{@link Game.GameState#PLAYING} - Active gameplay with turn-based shooting</li>
     *   <li>{@link Game.GameState#FINISHED} - Game has concluded with a winner</li>
     * </ul>
     *
     * <p>This method is useful for:
     * <ul>
     *   <li>Determining which UI elements to display</li>
     *   <li>Validating whether certain actions are allowed</li>
     *   <li>Implementing state-specific game logic</li>
     * </ul>
     * </p>
     *
     * @return the current {@link Game.GameState}, or {@code null} if not initialized
     * @see Game.GameState
     */
    Game.GameState getCurrentState();

    /**
     * Retrieves the AI instance controlling the machine player's behavior.
     *
     * <p>This method provides direct access to the {@link SmartAI} object that
     * manages the computer opponent's decision-making, including:</p>
     * <ul>
     *   <li>Shot selection strategies (HUNT and TARGET modes)</li>
     *   <li>Hit tracking and ship pursuit logic</li>
     *   <li>Probability-based targeting using heat maps</li>
     * </ul>
     *
     * <p><b>Common use cases:</b></p>
     * <ul>
     *   <li>Registering shot results: {@code getSmartAI().registerResult(row, col, hit, sunk)}</li>
     *   <li>Requesting next AI move: {@code getSmartAI().getNextShot()}</li>
     *   <li>Resetting AI state: {@code getSmartAI().reset()}</li>
     *   <li>Debugging AI behavior: {@code getSmartAI().getDebugInfo()}</li>
     * </ul>
     *
     * @return the {@link SmartAI} instance, or {@code null} if AI is not initialized
     * @see SmartAI
     * @see SmartAI#getNextShot()
     * @see SmartAI#registerResult(int, int, boolean, boolean)
     */
    SmartAI getSmartAI();
}