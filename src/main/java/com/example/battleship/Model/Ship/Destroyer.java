package com.example.battleship.Model.Ship;

public class Destroyer implements IShip{
    private final boolean sunken;
    private boolean touched;
    private final int positionX;
    private final int positionY;
    private final Direction direction;

    public Destroyer( int positionX, int positionY, Direction direction){
        this.positionX = positionX;
        this.positionY = positionY;
        this.sunken = false;
        this.direction = direction;
    }

    @Override
    public boolean isSunken(){return sunken;}
    @Override
    public boolean isTouched(){return touched;}
    @Override
    public int getRow(){return positionY;}
    @Override
    public int getCol(){return positionX;}
    @Override
    public int getShipSize(){return 2;}
    @Override
    public Direction getDirection(){return direction;}

}
