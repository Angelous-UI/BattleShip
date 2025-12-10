package com.example.battleship.Model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple machine (CPU) player implementation.
 * It mirrors the Human implementation and stores its own shots and state.
 */
public class Machine implements IPlayer{
    private String name;
    private boolean eliminated;
    private List<int[]> shots = new ArrayList<>();

    /**
     * Constructs a Machine player with the given name.
     *
     * @param name CPU display name
     */
    public Machine(String name){
        this.name = name;
        this.eliminated = false;

    }
    /**
     * Record a shot by the machine.
     *
     * @param positionX 1-based column coordinate
     * @param positionY 1-based row coordinate
     */
    @Override
    public void shoot(int positionX, int positionY){
        shots.add(new int[]{positionX, positionY});
    }

    /**
     * Check whether this machine has already shot at (x,y).
     *
     * @param x 1-based column
     * @param y 1-based row
     * @return true if a previous shot matches
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
     * Returns whether this machine has been eliminated.
     *
     * @return elimination state
     */
    @Override
    public boolean isEliminated(){return eliminated;}
    /**
     * Returns the machine's display name.
     *
     * @return name string
     */
    @Override
    public String getName(){return name;}
    /**
     * Returns the list of shots performed by the machine.
     *
     * @return list of int[] coordinates {x,y}
     */
    @Override
    public List<int[]> getShots() {
        return shots;
    }
}
