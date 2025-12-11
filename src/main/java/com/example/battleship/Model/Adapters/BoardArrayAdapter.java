package com.example.battleship.Model.Adapters;

import com.example.battleship.Model.Board.Board;

//Clase adaptadora que convierte board hasmap a un 2d faciltiando la serializacion, el procesamiento y visutalizacoin
public class BoardArrayAdapter {
    private final Board board;

    public BoardArrayAdapter(Board board) {
        this.board = board;
    }

    public int[][] toArray() {
        int[][] array = new int[10][10];

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                array[row][col] = board.getCell(row, col);
            }
        }

        return array;
    }

    public void fromArray(int[][] array) {
        if (array == null || array.length != 10 || array[0].length != 10) {
            throw new IllegalArgumentException("Array debe ser 10x10");
        }

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                board.setCell(row, col, array[row][col]);
            }
        }
    }

    public String toStringRepresentation() {
        StringBuilder sb = new StringBuilder();
        sb.append("   0 1 2 3 4 5 6 7 8 9\n");

        for (int row = 0; row < 10; row++) {
            sb.append(row).append("| ");
            for (int col = 0; col < 10; col++) {
                int cell = board.getCell(row, col);
                String symbol = switch (cell) {
                    case 0 -> "·";
                    case 1 -> "■";
                    case 2 -> "○";
                    case 3 -> "X";
                    default -> "?";
                };
                sb.append(symbol).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public int[] getCellTypeCount() {
        int[] counts = new int[4];

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cell = board.getCell(row, col);
                if (cell >= 0 && cell < 4) {
                    counts[cell]++;
                }
            }
        }

        return counts;
    }
}
