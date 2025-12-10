package com.example.battleship.Model.Ship;

public class Frigate implements IShip {
    private boolean sunken;
    private int hitCount;
    private final int positionX;
    private final int positionY;
    private final Direction direction;
    private final int shipSize = 1;

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

    @Override
    public void registerHit() {
        hitCount++;
        if (hitCount >= shipSize) {
            sunken = true;
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