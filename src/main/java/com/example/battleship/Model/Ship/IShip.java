package com.example.battleship.Model.Ship;

public interface IShip {
    enum Direction {
        RIGHT,
        LEFT,
        UP,
        DOWN;

        static Direction getRandomDirection() {
            return values()[(int) (Math.random() * values().length)];
        }
    }

    boolean isSunken();
    void registerHit();
    int getRow();
    int getCol();
    int getShipSize();
    Direction getDirection();
    int getHitCount(); //Cuántos impatos tienen
}
