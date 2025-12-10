package com.example.battleship.Model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Human player implementation. Stores the player's name,
 * elimination state, and the list of shots the player made.
 */
public class Human implements IPlayer{
    private String name;
    private boolean eliminated;
    private List<int[]> shots = new ArrayList<>();

    /**
     * Constructs a Human player with the given name.
     *
     * @param name player's display name
     */
    public Human(String name){
        this.name = name;
        this.eliminated = false;

    }
    /**
     * Record a shot by adding the coordinate pair to internal storage.
     *
     * @param positionX 1-based column coordinate
     * @param positionY 1-based row coordinate
     */
    @Override
    public void shoot(int positionX, int positionY){
        shots.add(new int[]{positionX, positionY});
    }

    /**
     * Check whether the player already shot at the given coordinate.
     *
     * @param x 1-based column coordinate
     * @param y 1-based row coordinate
     * @return true if a matching shot exists
     */
    @Override
    public boolean alreadyShot(int x, int y) {
        for (int[] s : shots) {
            if (s[0] == x && s[1] == y) {
                return true;
            }
        }
        return false;
    }
    /**
     * Indicates whether this player has been eliminated.
     *
     * @return elimination state
     */
    @Override
    public boolean isEliminated(){return eliminated;}
    /**
     * Returns the player's name.
     *
     * @return the stored name string
     */
    @Override
    public String getName(){return name;}
    /**
     * Returns the list of shots. Each element is an int[] representing {x, y}.
     *
     * @return the shots list
     */
    @Override
    public List<int[]> getShots() {
        return shots;
    }

}
