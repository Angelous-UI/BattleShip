package com.example.battleship.Model.Player;

import java.util.ArrayList;
import java.util.List;

public class Machine implements IPlayer{
    private String name;
    private boolean eliminated;
    private List<int[]> shots = new ArrayList<>();

    public Machine(String name){
        this.name = name;
        this.eliminated = false;

    }
    @Override
    public void shoot(int positionX, int positionY){
        shots.add(new int[]{positionX, positionY});
    }
    @Override
    public boolean alreadyShot(int x, int y) {
        for (int[] s : shots) {
            if (s[0] == x && s[1] == y) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean isEliminated(){return eliminated;}
    @Override
    public String getName(){return name;}
    @Override
    public List<int[]> getShots() {
        return shots;
    }
}
