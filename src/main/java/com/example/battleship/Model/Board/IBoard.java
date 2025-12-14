package com.example.battleship.Model.Board;

/**
 * Represents the interface for a game board in battleship
 * Provides methods to modify and retrieve values from board cells
 */

public interface IBoard {

    /**
     * Sets the value of a specific cell in the board
     * @param row the row index of the cell
     * @param col the column index of the cell
     * @param value the value to assign to the cell
     */
    void setCell(int row, int col, int value);

    /**
     * Retrieves the value stored at a specific board cell.
     *
     * @param row the row index of the cell
     * @param col the column index of the cell
     * @return the value stored at the specified cell, or 0 if empty
     */
    int getCell(int row, int col);

    /**
     * Prints the board
     */
    void printBoard();
}
