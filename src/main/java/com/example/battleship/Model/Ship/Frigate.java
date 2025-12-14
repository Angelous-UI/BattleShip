package com.example.battleship.Model.Ship;

import java.io.Serializable;

/**
 * Frigate: a ship occupying a single cell (size = 1).
 * Tracks hits and sunken state.
 */
public class Frigate implements IShip, Serializable {
    private boolean sunken;
    private int hitCount;
    private final int positionX;
    private final int positionY;
    private final Direction direction;
    private final int shipSize = 1;

    /**
     * Constructs a Frigate placed at (positionX, positionY) with the given direction.
     *
     * @param positionX 1-based column
     * @param positionY 1-based row
     * @param direction placement direction (unused for size=1 but kept for consistency)
     */
    public Frigate(int positionX, int positionY, Direction direction) {
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


    /**
     * Register a hit, mark as sunken if hit count reaches ship size.
     */
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