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
     * The wrapped Board instance that handles the actual storage and operations.
     */
    private final Board board;

    /**
     * Creates a new BoardAdapter wrapping a new Board instance.
     * The internal board is initialized with a 10x10 grid of zeros.
     */
    public BoardAdapter() {
        this.board = new Board();
    }

    /**
     * Creates a new BoardAdapter wrapping an existing Board instance.
     * This constructor allows you to adapt an already-created board.
     *
     * @param board the board instance to wrap and adapt
     * @throws NullPointerException if board is null
     */
    public BoardAdapter(Board board) {
        if (board == null) {
            throw new NullPointerException("Board cannot be null");
        }
        this.board = board;
    }

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
        board.setCell(row, col, value);
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
        return board.getCell(row, col);
    }

    /**
     * Prints the board to standard output.
     * <p>Default implementation delegates to the wrapped board.</p>
     */
    @Override
    public void printBoard() {
        board.printBoard();
    }

    /**
     * Returns the wrapped Board instance.
     * Useful if you need direct access to the underlying Board object.
     *
     * @return the wrapped Board instance
     */
    protected Board getBoard() {
        return board;
    }
}