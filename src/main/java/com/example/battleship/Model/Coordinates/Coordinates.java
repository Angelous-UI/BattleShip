package com.example.battleship.Model.Coordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Coordinates {
    private final List<int[]> coordinates; // guarda pares {x,y}

    public Coordinates() {
        coordinates = new ArrayList<>();
        generate();
        shuffle();
    }

    private void generate() {
        // genera TODAS las posiciones posibles (1..10, 1..10)
        for (int x = 1; x <= 10; x++) {
            for (int y = 1; y <= 10; y++) {
                coordinates.add(new int[]{x, y});
            }
        }
    }

    private void shuffle() {
        Collections.shuffle(coordinates);
    }

    // devuelve una lista con las primeras 10 coordenadas aleatorias
    public List<int[]> getTenRandomCoordinates() {
        return coordinates.subList(0, 10);
    }

    public List<int[]> getCoordinates() {
        return coordinates;
    }
}
