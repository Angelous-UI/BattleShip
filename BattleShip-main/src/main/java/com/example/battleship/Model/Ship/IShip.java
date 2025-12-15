package com.example.battleship.Model.Ship;

import java.io.Serializable;

/**
 * Interface describing a ship placed on the board.
 * Implementations expose position, direction, size and hit tracking.
 */
public interface IShip extends Serializable {
/**
 * Cardinal directions used for ship placement and iteration.
 */
    enum Direction {
        RIGHT,
        LEFT,
        UP,
        DOWN;

    /**
     * Returns a random direction value.
     *
     * @return a random Direction
     */
        static Direction getRandomDirection() {
            return values()[(int) (Math.random() * values().length)];
        }
    }

    /**
     * Returns whether the ship has been sunk (enough hits recorded).
     *
     * @return true if sunken
     */
    boolean isSunken();
    /**
     * Registers a hit against the ship. Implementations should update internal hit counters.
     */
    void registerHit();
    /**
     * Returns the base row (y) where the ship was placed. Coordinates are 1-based.
     *
     * @return base row (y)
     */
    int getRow();
    /**
     * Returns the base column (x) where the ship was placed. Coordinates are 1-based.
     *
     * @return base column (x)
     */
    int getCol();
    /**
     * Returns the size (number of cells) occupied by the ship.
     *
     * @return ship size in cells
     */
    int getShipSize();
    /**
     * Returns the placement direction for the ship.
     *
     * @return Direction enum value
     */
    Direction getDirection();
    /**
     * Returns the number of registered hits this ship currently has.
     *
     * @return hit count
     */
    int getHitCount(); //Cuántos impatos tienen
}
