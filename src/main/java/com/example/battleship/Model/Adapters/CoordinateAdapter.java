package com.example.battleship.Model.Adapters;

public class CoordinateAdapter {

    private final int cellWidth;
    private final int cellHeight;

    public CoordinateAdapter(int cellWidth, int cellHeight) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    public int[] pixelToGrid(double pixelX, double pixelY) {
        int col = (int) (pixelX / cellWidth);
        int row = (int) (pixelY / cellHeight);

        col = Math.max(0, Math.min(9, col));
        row = Math.max(0, Math.min(9, row));

        return new int[]{row, col};
    }

    public double[] gridToPixel(int row, int col) {
        double pixelX = col * cellWidth;
        double pixelY = row * cellHeight;

        return new double[]{pixelX, pixelY};
    }

    public double[] gridToPixelCenter(int row, int col) {
        double centerX = col * cellWidth + cellWidth / 2.0;
        double centerY = row * cellHeight + cellHeight / 2.0;

        return new double[]{centerX, centerY};
    }

    public String gridToChessNotation(int row, int col) {
        if (row < 0 || row > 9 || col < 0 || col > 9) {
            throw new IllegalArgumentException("Coordenadas fuera de rango: (" + row + "," + col + ")");
        }

        char letter = (char) ('A' + col);
        int number = row + 1;

        return "" + letter + number;
    }

    public int[] chessNotationToGrid(String notation) {
        if (notation == null || notation.length() < 2) {
            throw new IllegalArgumentException("Notación inválida: " + notation);
        }

        char letterChar = notation.toUpperCase().charAt(0);
        String numberStr = notation.substring(1);

        int col = letterChar - 'A';
        int row = Integer.parseInt(numberStr) - 1;

        if (row < 0 || row > 9 || col < 0 || col > 9) {
            throw new IllegalArgumentException("Coordenadas fuera de rango: " + notation);
        }

        return new int[]{row, col};
    }

    public boolean isInsideBoard(double pixelX, double pixelY, double boardWidth, double boardHeight) {
        return pixelX >= 0 && pixelX < boardWidth &&
                pixelY >= 0 && pixelY < boardHeight;
    }

    public int manhattanDistance(int row1, int col1, int row2, int col2) {
        return Math.abs(row2 - row1) + Math.abs(col2 - col1);
    }

    public String toReadableString(int row, int col) {
        return String.format("Fila %d, Columna %d (%s)", row, col, gridToChessNotation(row, col));
    }
}
