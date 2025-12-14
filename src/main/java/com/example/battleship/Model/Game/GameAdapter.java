package com.example.battleship.Model.Game;

import com.example.battleship.Model.AI.SmartAI;
import com.example.battleship.Model.Board.Board;
import com.example.battleship.Model.Exceptions.InvalidPositionException;
import com.example.battleship.Model.Exceptions.InvalidShotException;
import com.example.battleship.Model.Player.Human;
import com.example.battleship.Model.Ship.IShip;

import java.util.List;

/**
 * Abstract adapter class that provides default implementations for the {@link IGame} interface.
 *
 * <p>This class follows the Adapter design pattern, providing empty or minimal
 * implementations of all interface methods. Subclasses can override only the methods
 * they need, reducing boilerplate code.</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * public class CustomGame extends GameAdapter {
 *     &#64;Override
 *     public void generateFleet() {
 *         // Custom fleet generation logic
 *     }
 *
 *     &#64;Override
 *     public boolean playTurn(Board board, Human player, int row, int col) {
 *         // Custom turn logic
 *         return false;
 *     }
 * }
 * </pre>
 *
 * <p><b>Design Pattern:</b> Adapter Pattern</p>
 * <p><b>Purpose:</b> Simplifies implementation of {@link IGame} by providing default behaviors</p>
 *
 * @author Battleship Development Team
 * @version 1.0
 * @see IGame
 */
public abstract class GameAdapter implements IGame {

    /**
     * Default implementation that does nothing.
     * Subclasses should override this to implement fleet generation logic.
     */
    @Override
    public void generateFleet() {
        // Default: Do nothing
    }

    /**
     * Default implementation that always throws an {@link UnsupportedOperationException}.
     * Subclasses must override this to provide ship placement logic.
     *
     * @param ship the ship to place
     * @throws InvalidPositionException if placement is invalid (not thrown by default)
     * @throws UnsupportedOperationException always thrown by default implementation
     */
    @Override
    public void placeShip(IShip ship) throws InvalidPositionException {
        throw new UnsupportedOperationException("placeShip() not implemented");
    }

    /**
     * Default implementation that always returns {@code false}.
     * Subclasses should override this to implement turn logic.
     *
     * @param board  the game board
     * @param player the player performing the action
     * @param row    target row
     * @param col    target column
     * @return {@code false} by default
     * @throws InvalidShotException if the shot is invalid (not thrown by default)
     */
    @Override
    public boolean playTurn(Board board, Human player, int row, int col) throws InvalidShotException {
        return false;
    }

    /**
     * Default implementation that does nothing.
     * Subclasses should override this to implement turn advancement logic.
     */
    @Override
    public void advanceTurn() {
        // Default: Do nothing
    }

    /**
     * Default implementation that always returns {@code false}.
     * Subclasses should override this to check game-over conditions.
     *
     * @return {@code false} by default
     */
    @Override
    public boolean isGameOver() {
        return false;
    }

    /**
     * Default implementation that returns {@code null}.
     * Subclasses must override this to return the actual game board.
     *
     * @return {@code null} by default
     */
    @Override
    public Board getMachineBoard() {
        return null;
    }

    /**
     * Default implementation that returns {@code null}.
     * Subclasses should override this to return the current player.
     *
     * @return {@code null} by default
     */
    @Override
    public Object getCurrentPlayer() {
        return null;
    }

    /**
     * Default implementation that returns {@code null}.
     * Subclasses must override this to return the player list.
     *
     * @return {@code null} by default
     */
    @Override
    public List<Object> getPlayers() {
        return null;
    }

    /**
     * Default implementation that does nothing.
     * Subclasses can override this to print fleet coordinates.
     */
    @Override
    public void printFleetCoordinates() {
        // Default: Do nothing
    }

    /**
     * Default implementation that returns {@code null}.
     * Subclasses must override this to return the machine's fleet.
     *
     * @return {@code null} by default
     */
    @Override
    public List<IShip> getMachineFleet() {
        return null;
    }

    /**
     * Default implementation that returns {@code null}.
     * Subclasses must override this to return the human player instance.
     *
     * @return {@code null} by default
     */
    @Override
    public Human getHuman() {
        return null;
    }

    /**
     * Default implementation that returns {@code null}.
     * Subclasses should override this to return the current game state.
     *
     * @return {@code null} by default
     */
    @Override
    public Game.GameState getCurrentState() {
        return null;
    }

    /**
     * Default implementation that returns {@code null}.
     * Subclasses must override this to return the AI instance.
     *
     * @return {@code null} by default
     */
    @Override
    public SmartAI getSmartAI() {
        return null;
    }
}