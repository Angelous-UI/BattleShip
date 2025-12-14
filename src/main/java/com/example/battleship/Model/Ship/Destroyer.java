package com.example.battleship.Model.Ship;

import java.io.Serializable;

/**
 * Destroyer: a ship occupying two cells (size = 2).
 * Keeps track of hits and sunken state.
 */
public class Destroyer extends ShipAdapter implements Serializable {
    private boolean sunken;
    private int hitCount;
    private final int positionX;
    private final int positionY;
    private final Direction direction;
    private final int shipSize = 2;



    /**
     * Constructs a Destroyer.
     *
     * @param positionX 1-based column
     * @param positionY 1-based row
     * @param direction placement direction
     */
    public Destroyer(int positionX, int positionY, Direction direction) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.sunken = false;
        this.hitCount = 0;
        this.direction = direction;
    }

    /**
     * Indicates whether the ship has been sunk.
     *
     * @return {@code true} if the ship is sunk, {@code false} otherwise
     */
    @Override
    public boolean isSunken() {
        return sunken;
    }

    /**
     * Registers a hit on the ship.
     * <p>
     * Each hit increments the hit counter. When the number of hits
     * reaches or exceeds the ship size, the ship is marked as sunk.
     * </p>
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