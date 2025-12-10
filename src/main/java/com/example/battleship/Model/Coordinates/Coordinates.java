package com.example.battleship.Model.Coordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Helper utility to generate and shuffle board coordinates.
 * Coordinates are stored as int[] pairs in the form {x, y}.
 * Both x and y are 1-based and range between 1 and 10 inclusive.
 */
public class Coordinates {
    private final List<int[]> coordinates; // guarda pares {x,y}

    /**
     * Constructs the Coordinates helper and generates + shuffles all board positions.
     */
    public Coordinates() {
        coordinates = new ArrayList<>();
        generate();
        shuffle();
    }

    /**
     * Generates all possible board positions (1..10 for both axes).
     * The order after generation is deterministic before shuffle is applied.
     */
    private void generate() {
        // genera TODAS las posiciones posibles (1..10, 1..10)
        for (int x = 1; x <= 10; x++) {
            for (int y = 1; y <= 10; y++) {
                coordinates.add(new int[]{x, y});
            }
        }
    }

    /**
     * Randomly shuffles the list of coordinates in place.
     */
    private void shuffle() {
        Collections.shuffle(coordinates);
    }


    /**
     * Returns a sublist with the first ten shuffled coordinates.
     * Useful when you need a small batch of random positions.
     *
     * @return a list view of the first 10 coordinate pairs
     */
    // devuelve una lista con las primeras 10 coordenadas aleatorias
    public List<int[]> getTenRandomCoordinates() {
        return coordinates.subList(0, 10);
    }

    /**
     * Returns the internal list of coordinates (shuffled).
     * Note: this returns a modifiable view; callers should not rely on immutability.
     *
     * @return the list of shuffled coordinate pairs
     */
    public List<int[]> getCoordinates() {
        return coordinates;
    }
}
