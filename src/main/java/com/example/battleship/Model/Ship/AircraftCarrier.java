package com.example.battleship.Model.Ship;

import java.io.Serializable;

/**
 * AircraftCarrier: a ship occupying four cells (size = 4).
 * Tracks registered hits and sunken state.
 */
public class AircraftCarrier implements IShip, Serializable {
    private boolean sunken;
    private int hitCount;
    private final int positionX;
    private final int positionY;
    private final Direction direction;
    private final int shipSize = 4;

    /**
     * Constructs an AircraftCarrier instance.
     *
     * @param positionX 1-based column
     * @param positionY 1-based row
     * @param direction placement direction
     */
    public AircraftCarrier(int positionX, int positionY, Direction direction) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.sunken = false;
        this.hitCount = 0;
        this.direction = direction;
    }

    /**
     * Checks whether the ship has been completely destroyed.
     *
     * @return true if hits >= size, false otherwise
     */
    @Override
    public boolean isSunken() {
        return sunken;
    }

    /**
     * Registers a hit on this ship.
     * Increments the hit counter by one.
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