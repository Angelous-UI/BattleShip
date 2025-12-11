package com.example.battleship.Model.Board;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the {@link IBoard} interface using a HashMap-based
 * grid system. Each cell is identified by a string key formatted as "row,col".
 *
 * ⚠️ IMPORTANTE: Este Board ahora usa coordenadas 0-indexed (0-9)
 * para ser consistente con el sistema de canvas del GameController.
 */
public class Board implements IBoard{
    private final int ROWS = 10;
    private final int COLS = 10;

    private final Map<String, Integer> board;

    /**
     * Creates a 10x10 board and initializes all cells to 0.
     * Coordenadas van de 0 a 9 (0-indexed)
     */
    public Board() {
        board = new HashMap<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                String key = r + "," + c;
                board.put(key, 0);
            }
        }
    }

    /**
     * Sets a value in the board at the given coordinates.
     *
     * @param row   0-based row index (0..9)
     * @param col   0-based column index (0..9)
     * @param value value to set for the cell (see class doc for meanings)
     */
    @Override
    public void setCell(int row, int col, int value) {
        String key = row + "," + col;
        board.put(key, value);
    }

    /**
     * Returns the value at the given coordinates.
     *
     * @param row 0-based row index (0..9)
     * @param col 0-based column index (0..9)
     * @return the integer stored at the cell, or 0 if the position is not present
     */
    @Override
    public int getCell(int row, int col) {
        String key = row + "," + col;
        return board.getOrDefault(key, 0);
    }

    /**
     * Prints the board to standard output as 10 rows of numbers.
     * This method is intended for debugging and quick visual inspection.
     */
    @Override
    public void printBoard() {
        System.out.println("\n  0 1 2 3 4 5 6 7 8 9");
        for (int r = 0; r < ROWS; r++) {
            System.out.print(r + " ");
            for (int c = 0; c < COLS; c++) {
                String key = r + "," + c;
                System.out.print(board.get(key) + " ");
            }
            System.out.println();
        }
    }
}