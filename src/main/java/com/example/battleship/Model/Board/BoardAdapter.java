package com.example.battleship.Model.Board;

/**
 * Abstract adapter class for {@link IBoard} that provides default implementations
 * of all interface methods by delegating to a wrapped Board instance.
 *
 * <p>This adapter allows subclasses to override only the methods they need to customize,
 * without being forced to implement all interface methods. This is useful when you want
 * to add behavior (logging, validation, etc.) to only specific board operations.</p>
 * @author Battleship Development Team
 * @version 1.0
 * @see IBoard
 * @see Board
 * @since 1.0
 */
public abstract class BoardAdapter implements IBoard {


    /**
     * Sets a value in the board at the given coordinates.
     * <p>Default implementation delegates to the wrapped board.</p>
     *
     * @param row   0-based row index (0..9)
     * @param col   0-based column index (0..9)
     * @param value value to set for the cell
     */
    @Override
    public void setCell(int row, int col, int value) {
        // Default nothing
    }

    /**
     * Returns the value at the given coordinates.
     * <p>Default implementation delegates to the wrapped board.</p>
     *
     * @param row 0-based row index (0..9)
     * @param col 0-based column index (0..9)
     * @return the integer stored at the cell
     */
    @Override
    public int getCell(int row, int col) {
        return 0;
    }

    /**
     * Prints the board to standard output.
     * <p>Default implementation delegates to the wrapped board.</p>
     */
    @Override
    public void printBoard() {
        // Default nothing
    }

    /**
     * Returns the wrapped Board instance.
     * Useful if you need direct access to the underlying Board object.
     *
     * @return the wrapped Board instance
     */
    protected Board getBoard() {
        return null;
    }
}