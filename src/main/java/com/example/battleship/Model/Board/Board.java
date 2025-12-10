package com.example.battleship.Model.Board;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final int ROWS = 10;
    private final int COLS = 10;

    private final Map<String, Integer> board;


    public Board() {
        board = new HashMap<>();
        for (int r = 1; r <= ROWS; r++) {
            for (int c = 1; c <= COLS; c++) {
                String key = r + "," + c;
                board.put(key, 0);
            }
        }
    }

    public void setCell(int row, int col, int value) {
        String key = row + "," + col;
        board.put(key, value);
    }

    public int getCell(int row, int col) {
        String key = row + "," + col;
        return board.getOrDefault(key, 0);
    }

    public void printBoard() {
        for (int r = 1; r <= ROWS; r++) {
            for (int c = 1; c <= COLS; c++) {
                String key = r + "," + c;
                System.out.print(board.get(key) + " ");
            }
            System.out.println();
        }
    }
}
