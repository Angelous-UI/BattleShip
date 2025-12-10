package com.example.battleship.Model.Ship;

/**
 * AircraftCarrier: a ship occupying four cells (size = 4).
 * Tracks registered hits and sunken state.
 */
public class AircraftCarrier implements IShip {
    private boolean sunken;
    private int hitCount;
    private final int positionX;
    private final int positionY;
    private Direction direction;
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

    @Override
    public void rotate() {
        switch (direction) {
            case RIGHT -> direction = Direction.DOWN;
            case DOWN -> direction = Direction.RIGHT;
            case LEFT -> direction = Direction.UP;
            case UP -> direction = Direction.LEFT;
        }
    }

    @Override
    public int getHitCount() {
        return hitCount;
    }

    @Override
    public int getRow() {
        return positionY;
    }

    @Override
    public int getCol() {
        return positionX;
    }

    @Override
    public int getShipSize() {
        return shipSize;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }
}