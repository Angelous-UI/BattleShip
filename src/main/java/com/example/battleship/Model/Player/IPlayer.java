package com.example.battleship.Model.Player;

import java.util.List;

/**
 * Player interface used by Human and Machine implementations.
 * A player is responsible for tracking its own shots and elimination state.
 */
public interface IPlayer {

    /**
     * Register a shot made by this player.
     *
     * @param positionX 1-based column (x) coordinate of the shot
     * @param positionY 1-based row (y) coordinate of the shot
     */
    void shoot(int positionX, int positionY);

    /**
     * Check whether this player has already shot at a specific coordinate.
     *
     * @param x 1-based column coordinate
     * @param y 1-based row coordinate
     * @return true if the player previously fired at (x, y)
     */
    boolean alreadyShot(int x, int y);

    /**
     * Returns whether the player has been eliminated from the game.
     *
     * @return true when eliminated
     */
    boolean isEliminated();

    /**
     * Returns the player's display name.
     *
     * @return name of the player
     */
    String getName();

    /**
     * Returns the list of shots this player has made.
     * Each shot is represented as an int[] of length 2: {x, y}.
     *
     * @return mutable list of shot coordinates
     */
    List<int[]> getShots();
}
