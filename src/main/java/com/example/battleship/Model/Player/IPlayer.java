package com.example.battleship.Model.Player;

import java.util.List;

public interface IPlayer {

    void shoot(int positionX, int positionY);

    boolean alreadyShot(int x, int y);

    boolean isEliminated();

    String getName();

    List<int[]> getShots();
}
