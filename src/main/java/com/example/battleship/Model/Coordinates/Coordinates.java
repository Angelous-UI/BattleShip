package com.example.battleship.Model.Coordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper utility to generate and shuffle board coordinates.
 * Coordinates are stored as int[] pairs in the form {row, col}.
 * Both row and col are 0-based and range between 0 and 9 inclusive.
 */
public class Coordinates {
    private final List<int[]> coordinates;

    /**
     * Constructs the Coordinates helper and generates + shuffles all board positions.
     */
    public Coordinates() {
        coordinates = new ArrayList<>();
        generate();
        shuffle();
    }

    /**
     * Generates all possible board positions (0..9 for both axes).
     * The order after generation is deterministic before shuffle is applied.
     */
    private void generate() {
        // generate all possible solutions (0..9, 0..9)
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                coordinates.add(new int[]{row, col});
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
    public List<int[]> getTenRandomCoordinates() {
        return coordinates.subList(0, 10);
    }

    /**
     * Returns the internal list of coordinates (shuffled).
     * Note: this returns a modifiable view; callers should not rely on immutability.
     *
     * @return the list of shuffled coordinate pairs [row, col] en formato 0-9
     */
    public List<int[]> getCoordinates() {
        return coordinates;
    }
}