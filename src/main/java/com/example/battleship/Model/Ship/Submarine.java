package com.example.battleship.Model.Ship;

import java.io.Serializable;

/**
 * Submarine: a ship occupying three cells (size = 3).
 * Maintains hit counting and sunken flag.
 */
public class Submarine implements IShip, Serializable {
    private boolean sunken;
    private int hitCount;
    private final int positionX;
    private final int positionY;
    private final Direction direction;
    private final int shipSize = 3;

    /**
     * Constructs a Submarine at the provided location and direction.
     *
     * @param positionX 1-based column
     * @param positionY 1-based row
     * @param direction placement direction
     */
    public Submarine(int positionX, int positionY, Direction direction) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.sunken = false;
        this.hitCount = 0;
        this.direction = direction;
    }

    @Override
    public boolean isSunken() {
        return sunken;
    }

    @Override
    public void registerHit() {
        hitCount++;
        if (hitCount >= shipSize) {
            sunken = true;
        }
    }

    /**
     * Returns the number of times this ship has been hit.
     *
     * @return the current hit count
     */
    @Override
    public int getHitCount() {
        return hitCount;
    }

    /**
     * Returns the row position of the ship on the board.
     *
     * @return the row index (Y coordinate)
     */
    @Override
    public int getRow() {
        return positionY;
    }

    /**
     * Returns the column position of the ship on the board.
     *
     * @return the column index (X coordinate)
     */
    @Override
    public int getCol() {
        return positionX;
    }

    /**
     * Returns the size (length) of the ship.
     *
     * @return the ship size
     */
    @Override
    public int getShipSize() {
        return shipSize;
    }

    /**
     * Returns the orientation of the ship.
     *
     * @return the ship direction
     */
    @Override
    public Direction getDirection() {
        return direction;
    }
}