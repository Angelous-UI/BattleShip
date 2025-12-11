/*
package com.example.battleship.Model.Board;

*/
/**
 * Adapter class for Board that implements IBoard.
 * This adapter wraps a Board instance and delegates all calls to it.
 *
 * Useful for:
 * - Adding additional logic without modifying the original Board class
 * - Logging or debugging board operations
 * - Converting between different coordinate systems
 * - Testing with mock implementations
 *//*

public abstract class BoardAdapter implements IBoard {
    private final Board board;

    */
/**
     * Creates a new BoardAdapter wrapping a new Board instance.
     *//*

    public BoardAdapter() {
        this.board = new Board();
    }

    */
/**
     * Sets a value in the board at the given coordinates.
     *
     * @param row   0-based row index (0..9)
     * @param col   0-based column index (0..9)
     * @param value value to set for the cell
     *//*

    @Override
    public void setCell(int row, int col, int value) {
        board.setCell(row, col, value);
    }

    */
/**
     * Returns the value at the given coordinates.
     *
     * @param row 0-based row index (0..9)
     * @param col 0-based column index (0..9)
     * @return the integer stored at the cell
     *//*

    @Override
    public int getCell(int row, int col) {
        return board.getCell(row, col);
    }

    */
/**
     * Prints the board to standard output.
     *//*

    @Override
    public void printBoard() {
        board.printBoard();
    }

    */
/**
     * Returns the wrapped Board instance.
     * Useful if you need direct access to the Board object.
     *
     * @return the wrapped Board
     *//*

    public Board getBoard() {
        return board;
    }
}
*/
