package com.example.battleship.Model.Ship;

/**
 * Submarine: a ship occupying three cells (size = 3).
 * Maintains hit counting and sunken flag.
 */
public class Submarine implements IShip {
    private boolean sunken;
    private int hitCount;
    private final int positionX;
    private final int positionY;
    private Direction direction;
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