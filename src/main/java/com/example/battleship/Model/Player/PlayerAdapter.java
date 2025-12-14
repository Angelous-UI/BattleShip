package com.example.battleship.Model.Player;

import java.util.List;

/**
 * Abstract adapter class for {@link IPlayer} that provides default implementations
 * of all interface methods by delegating to a wrapped IPlayer instance.
 *
 * <p>This adapter allows subclasses to override only the methods they need to customize,
 * without being forced to implement all interface methods. This is useful when you want
 * to add behavior (statistics, logging, validation) to only specific player operations.</p>
 *
 *
 * @author Battleship Development Team
 * @version 1.0
 * @see IPlayer
 * @see Human
 * @see Machine
 * @since 1.0
 */
public abstract class PlayerAdapter implements IPlayer {

    /**
     * Registers a shot made by this player.
     * <p>Default implementation delegates to the wrapped player.</p>
     *
     * @param positionX 1-based column (x) coordinate of the shot
     * @param positionY 1-based row (y) coordinate of the shot
     */
    @Override
    public void shoot(int positionX, int positionY) {
        // Default nothing
    }

    /**
     * Checks whether this player has already shot at a specific coordinate.
     * <p>Default implementation delegates to the wrapped player.</p>
     *
     * @param x 1-based column coordinate
     * @param y 1-based row coordinate
     * @return true if the player previously fired at (x, y)
     */
    @Override
    public boolean alreadyShot(int x, int y) {
        return false;
    }

    /**
     * Returns whether the player has been eliminated from the game.
     * <p>Default implementation delegates to the wrapped player.</p>
     *
     * @return true when eliminated, false otherwise
     */
    @Override
    public boolean isEliminated() {
        return false;
    }

    /**
     * Returns the player's display name.
     * <p>Default implementation delegates to the wrapped player.</p>
     *
     * @return name of the player
     */
    @Override
    public String getName() {
        return "Player";
    }

    /**
     * Returns the list of shots this player has made.
     * Each shot is represented as an int[] of length 2: {x, y}.
     * <p>Default implementation delegates to the wrapped player.</p>
     *
     * @return mutable list of shot coordinates
     */
    @Override
    public List<int[]> getShots() {
        return null;
    }

    /**
     * Returns the wrapped IPlayer instance.
     * Useful if you need direct access to the underlying Player object.
     *
     * @return the wrapped IPlayer instance
     */
    protected IPlayer getPlayer() {
        return null;
    }
}