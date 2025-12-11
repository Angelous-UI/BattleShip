package com.example.battleship.Model.Adapters;

import com.example.battleship.Model.Ship.*;
import java.io.Serializable;


//Clase adaptadora que convierte objetos de IShip a un formato serializable pra guardar en archivos
public class ShipDataAdapter implements Serializable {
    private static final long serialVersionUID = 1L;

    private String shipType;
    private int positionX;
    private int positionY;
    private String direction;
    private int hitCount;
    private boolean sunken;
    private int shipSize;

    /**
     * Constructor vacío para deserialización
     */
    public ShipDataAdapter() {
    }


    //Acá es para srializacion
    public ShipDataAdapter(IShip ship) {
        this.shipType = ship.getClass().getSimpleName();
        this.positionX = ship.getCol();
        this.positionY = ship.getRow();
        this.direction = ship.getDirection().name();
        this.hitCount = ship.getHitCount();
        this.sunken = ship.isSunken();
        this.shipSize = ship.getShipSize();
    }


    public IShip toShip() {
        IShip.Direction dir = IShip.Direction.valueOf(direction);

        IShip ship = switch (shipType) {
            case "AircraftCarrier" -> new AircraftCarrier(positionX, positionY, dir);
            case "Submarine" -> new Submarine(positionX, positionY, dir);
            case "Destroyer" -> new Destroyer(positionX, positionY, dir);
            case "Frigate" -> new Frigate(positionX, positionY, dir);
            default -> throw new IllegalArgumentException("Tipo de barco desconocido: " + shipType);
        };

        // Restaurar los hits
        for (int i = 0; i < hitCount; i++) {
            ship.registerHit();
        }

        return ship;
    }

    public String getShipType(){
        return shipType;

    }
    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public boolean isSunken() {
        return sunken;
    }

    public void setSunken(boolean sunken) {
        this.sunken = sunken;
    }

    public int getShipSize() {
        return shipSize;
    }

    public void setShipSize(int shipSize) {
        this.shipSize = shipSize;
    }

    @Override
    public String toString() {
        return String.format("%s[pos=(%d,%d), dir=%s, hits=%d/%d, sunken=%s]",
                shipType, positionY, positionX, direction, hitCount, shipSize, sunken);
    }


}
