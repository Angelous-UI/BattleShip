package com.example.battleship.Model.Game;

import com.example.battleship.Model.Coordinates.Coordinates;
import com.example.battleship.Model.Board.Board;
import com.example.battleship.Model.Exceptions.InvalidGameStateException;
import com.example.battleship.Model.Exceptions.InvalidPositionException;
import com.example.battleship.Model.Exceptions.InvalidShotException;
import com.example.battleship.Model.Player.Human;
import com.example.battleship.Model.Player.Machine;
import com.example.battleship.Model.Ship.*;

import java.util.*;

/**
 * Represents the main controller of the Battleship game. Manages players,
 * turn order, the board, ship placement, firing logic, and overall game state.
 *
 * <p>This class implements the {@link IGame} interface and contains all core
 * gameplay logic such as turn progression, ship placement validation, hit
 * detection, and fleet generation.</p>
 */

public class Game implements IGame {
    /**
     * Human player instance.
     */
    private Human human;
    private Machine machine;
    /**
     * Stores all active players (human and CPU).
     */
    private final List<Object> players;
    /**
     * Queue controlling the turn rotation between players.
     */
    private final Queue<Object> turnQueue;
    /**
     * List containing every ship currently placed in the game.
     */
    private final List<IShip> fleet = new ArrayList<>();
    /**
     * Current turn index for the active player list.
     */
    private int currentPlayerIndex;
    /**
     * Indicates whether the game has ended.
     */
    private boolean gameOver;
    /**
     * Main game board where ships and shots are stored.
     */
    private final Board board;
    private final Board playerBoard;

    /**
     * Creates a new game instance and initializes players and the board.
     */
    public Game() {
        this.players = new ArrayList<>();
        this.turnQueue = new LinkedList<>();
        this.board = new Board();
        this.playerBoard = new Board();
        initializePlayers();
    }

    /**
     * Initializes the human and machine players and adds them to the turn queue.
     */

    private void initializePlayers() {
        human = new Human("You");
        players.add(human);
        turnQueue.add(human);

        machine = new Machine("CPU-");
        players.add(machine);
        turnQueue.add(machine);
    }

    /**
     * Returns the player whose turn is currently active.
     *
     * @return the current player, or {@code null} if no active players exist
     */
    @Override
    public Object getCurrentPlayer() {
        List<Object> activePlayers = getActivePlayers();

        if (activePlayers.isEmpty()) {
            return null;
        }

        if (currentPlayerIndex >= activePlayers.size()) {
            currentPlayerIndex = 0;
        }

        return activePlayers.get(currentPlayerIndex);
    }

    /**
     * Retrieves a list of all active players.
     *
     * @return an immutable list of players
     */
    public List<Object> getActivePlayers() {
        return players.stream().toList();
    }

    /**
     * Advances to the next player's turn. If only one player remains,
     * the game is marked as finished.
     */
    @Override
    public void advanceTurn() {
        currentPlayerIndex++;

        // Check for winner after each turn
        if (getActivePlayers().size() == 1) {
            gameOver = true;
        }
    }

    /**
     * Attempts to place a ship on the board. Validates boundaries and collisions.
     *
     * @param ship the ship to be placed
     * @throws InvalidPositionException if the ship overlaps or goes out of bounds
     */
    public void placeShip(IShip ship) throws InvalidPositionException {
        int[] d = calculateDisplacement(ship.getDirection());
        validateShipPlacement(ship, d[0], d[1]);
        applyShipToBoard(ship, d[0], d[1]);
    }


    /**
     * Converts the ship direction into row/column displacement values.
     *
     * @param dir the direction of the ship
     * @return an array containing vertical and horizontal displacement
     */
    // Y también corregir calculateDisplacement
    private int[] calculateDisplacement(IShip.Direction dir) {
        int dr = 0, dc = 0;

        switch (dir) {
            case RIGHT -> dc = 1;
            case LEFT -> dc = -1;
            case DOWN -> dr = 1;
            case UP -> dr = -1;
        }

        return new int[]{dr, dc};
    }

    /**
     * Ensures the ship can be placed without leaving the board or colliding.
     *
     * @param ship the ship to validate
     * @param dx   row displacement
     * @param dy   column displacement
     * @throws InvalidPositionException if placement is invalid
     */
    private void validateShipPlacement(IShip ship, int dx, int dy) throws InvalidPositionException {
        int row = ship.getRow();
        int col = ship.getCol();
        int size = ship.getShipSize();

        for (int i = 0; i < size; i++) {
            int r = row + dx * i;
            int c = col + dy * i;

            if (r < 1 || r > 10 || c < 1 || c > 10)
                throw new InvalidPositionException("No valid position");

            if (board.getCell(r, c) == 1)
                throw new InvalidPositionException("Ship collision");
        }
    }

    /**
     * Places the ship on the board by marking its cells as ship positions.
     *
     * @param ship the ship to place
     * @param dx   row displacement
     * @param dy   column displacement
     */
    private void applyShipToBoard(IShip ship, int dx, int dy) {
        int row = ship.getRow();
        int col = ship.getCol();
        int size = ship.getShipSize();

        for (int i = 0; i < size; i++) {
            int r = row + dx * i;
            int c = col + dy * i;
            board.setCell(r, c, 1);
        }
    }
    /**
     * Generates the full fleet for the game using random coordinates.
     * Automatically places each ship type based on predefined quantities.
     */
    @Override
    public void generateFleet(){
        Coordinates coordinates = new Coordinates();
        List<int[]> randomCoords = coordinates.getCoordinates();

        placeMultiple(randomCoords, 1, "AircraftCarrier");
        placeMultiple(randomCoords, 2, "Destroyer");
        placeMultiple(randomCoords, 3, "Submarine");
        placeMultiple(randomCoords, 4, "Frigate");
    }

    /**
     * Attempts to place multiple ships of the same type.
     *
     * @param coords list of random possible coordinates
     * @param count  number of ships to place
     * @param type   ship class name
     */
    private void placeMultiple(List<int[]> coords, int count, String type) {
        int placed = 0;

        for (int[] pos : coords) {
            if (placed == count) break;

            if (tryPlaceShipAt(type, pos[0], pos[1])) {
                placed++;
            }
        }

        System.out.println(type + " placed: " + placed);
    }

    /**
     * Tries to place a ship at a given position, testing randomized directions.
     *
     * @param type ship type name
     * @param row  board row
     * @param col  board column
     * @return {@code true} if the ship is successfully placed
     */
    private boolean tryPlaceShipAt(String type, int row, int col) {
        List<IShip.Direction> dirs = shuffleDirections();

        for (IShip.Direction dir : dirs) {
            IShip ship = createShip(type, row, col, dir);

            try {
                placeShip(ship);
                fleet.add(ship);
                return true;
            } catch (InvalidPositionException ignored) {}
        }

        return false;
    }

    /**
     * Randomizes ship placement directions.
     *
     * @return list of shuffled directions
     */
    private List<IShip.Direction> shuffleDirections() {
        List<IShip.Direction> dirs = new ArrayList<>(Arrays.asList(
                IShip.Direction.UP,
                IShip.Direction.DOWN,
                IShip.Direction.LEFT,
                IShip.Direction.RIGHT
        ));
        Collections.shuffle(dirs);
        return dirs;
    }

    /**
     * Instantiates a specific ship class based on type name.
     *
     * @param type ship class identifier
     * @param x    starting row
     * @param y    starting column
     * @param dir  orientation
     * @return a new ship instance
     */
    private IShip createShip(String type, int x, int y, IShip.Direction dir) {
        return switch (type) {
            case "AircraftCarrier" -> new AircraftCarrier(x, y, dir);
            case "Destroyer" -> new Destroyer(x, y, dir);
            case "Frigate" -> new Frigate(x, y, dir);
            case "Submarine" -> new Submarine(x, y, dir);
            default -> throw new IllegalArgumentException("Unknown ship type: " + type);
        };
    }



    /**
     * Executes the player's turn by firing at a coordinate.
     *
     * @param board  game board
     * @param player the human player performing the shot
     * @param row    target row
     * @param col    target column
     * @return {@code true} if the shot hits a ship
     * @throws InvalidShotException if the player shoots the same cell twice
     */
    // cambie en playturn, ahora coge row col
    @Override
    public boolean playTurn(Board board, Human player, int row, int col) {

        validateNotRepeatedShot(player, row, col);

        registerPlayerShot(player, row, col);

        int cell = board.getCell(row, col);

        if (isWater(cell)) {
            return handleWater(board, row, col);
        }

        if (isShip(cell)) {
            return handleShipHit(board, row, col);
        }

        return false;
    }

    /**
     * Ensures the player has not already fired at a given coordinate.
     *
     * @param player the player taking the shot
     * @param row    shot row
     * @param col    shot column
     * @throws InvalidShotException if the coordinate was already shot
     */
    private void validateNotRepeatedShot(Human player, int row, int col) {
        if (player.alreadyShot(row, col)) {
            System.out.println("Ya disparaste a esa posición.");
            throw new InvalidShotException("Ya disparaste a esa posición");
        }
    }

    /**
     * Registers the player's shot for tracking hit history.
     *
     * @param player player firing the shot
     * @param row    row fired at
     * @param col    column fired at
     */
    private void registerPlayerShot(Human player, int row, int col) {
        player.shoot(row, col);
    }

    /**
     * Determines whether the board cell represents water.
     *
     * @param cell the cell value
     * @return {@code true} if the cell is empty water
     */
    private boolean isWater(int cell) {
        return cell == 0;
    }

    /**
     * Handles a missed shot on the board.
     *
     * @param board the game board
     * @param row   missed row
     * @param col   missed column
     * @return always {@code false}, turn ends
     */
    private boolean handleWater(Board board, int row, int col) {
        board.setCell(row, col, 2);
        System.out.println("Agua!");
        return false; // turno termina
    }

    /**
     * Checks whether the board cell contains a ship.
     *
     * @param cell board cell value
     * @return {@code true} if the cell contains a ship
     */
    private boolean isShip(int cell) {
        return cell == 1;
    }

    /**
     * Handles a shot that hits a ship.
     *
     * @param board  game board
     * @param row    target row
     * @param col    target column
     * @return {@code true} if the player can shoot again
     */
    private boolean handleShipHit(Board board, int row, int col) {
        board.setCell(row, col, 3);
        System.out.println("¡Impacto!");

        for (IShip ship : fleet) {
            if (ValidateShot(row, col, ship)) {
                if (ship.isSunken()) {
                    System.out.println("🔥 Hundiste un " + ship.getClass().getSimpleName());
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Validates if the player’s recorded shots intersect a given ship.
     *
     * @param row the row
     * @param col the col
     * @param ship   the ship being validated
     * @return {@code true} if the ship was hit
     */
    public boolean ValidateShot(int row, int col, IShip ship) {
        if (isShotInsideShip(row, col, ship)) {
            ship.registerHit();
            return true;
        }
        return false;
    }


    /**
     * Determines if a shot coordinate lies within a ship’s coordinates.
     *
     * @param shotRow shot row
     * @param shotCol shot column
     * @param ship    target ship
     * @return {@code true} if the shot hits the ship
     */
    private boolean isShotInsideShip(int shotRow, int shotCol, IShip ship) {

        int dr = calculateDeltaRow(ship);
        int dc = calculateDeltaCol(ship);

        int baseRow = ship.getRow();
        int baseCol = ship.getCol();

        for (int i = 0; i < ship.getShipSize(); i++) {
            int currentRow = baseRow + dr * i;
            int currentCol = baseCol + dc * i;

            if (shotRow == currentRow && shotCol == currentCol) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines the row increment based on ship direction.
     *
     * @param ship the ship
     * @return row displacement
     */

    private int calculateDeltaRow(IShip ship) {
        return switch (ship.getDirection()) {
            case DOWN -> 1;
            case UP -> -1;
            default -> 0;
        };
    }

    /**
     * Determines the column increment based on ship direction.
     *
     * @param ship the ship
     * @return column displacement
     */
    private int calculateDeltaCol(IShip ship) {
        return switch (ship.getDirection()) {
            case RIGHT -> 1;
            case LEFT -> -1;
            default -> 0;
        };
    }


    /**
     * Executes the human player's entire turn, including validation and turn advancement.
     *
     * @param board the board where the shot occurs
     * @param human the player taking the turn
     * @param row   target row
     * @param col   target column
     */
    public void executeHumanPlay(Board board, Human human, int row, int col) {

        if (getCurrentPlayer() != human) {
            throw new InvalidGameStateException("No es tu turno");
        }

        boolean successfulShot = playTurn(board, human, row, col);

        if (!successfulShot) {
            System.out.println("Fin del turno del jugador.");
            advanceTurn();
        }
    }

    public void executeMachinePlay(){

        advanceTurn();
    }

    /**
     * Retrieves all the board coordinates occupied by a specific ship.
     *
     * @param ship the ship to inspect
     * @return a list of coordinates representing the ship
     */
    // cambio asi tmb
    public List<int[]> getShipCoordinates(IShip ship) {
        List<int[]> coords = new ArrayList<>();

        int row = ship.getRow();
        int col = ship.getCol();
        int size = ship.getShipSize();

        int dr = 0, dc = 0;

        switch (ship.getDirection()) {
            case RIGHT -> dc = 1;
            case LEFT  -> dc = -1;
            case DOWN  -> dr = 1;
            case UP    -> dr = -1;
        }

        for (int i = 0; i < size; i++) {
            int currentRow = row + dr * i;
            int currentCol = col + dc * i;
            coords.add(new int[]{currentRow, currentCol});  // [row, col]
        }

        return coords;
    }


    /**
     * Prints the coordinates of all ships currently placed in the fleet.
     */
    @Override
    public void printFleetCoordinates() {
        for (IShip ship : fleet) {
            System.out.println("--- " + ship.getClass().getSimpleName() + " ---");

            for (int[] c : getShipCoordinates(ship)) {
                // c[0] = row (fila) = Y
                // c[1] = col (columna) = X
                System.out.println("Fila=" + c[0] + "  Col=" + c[1]);
                // O si prefieres mantener X/Y:
                // System.out.println("Y=" + c[0] + "  X=" + c[1]);
            }
            System.out.println();
        }
    }

    /**
     * Returns whether the game has reached its end.
     *
     * @return {@code true} if the game is finished
     */
    @Override
    public boolean isGameOver() {
        return gameOver;
    }
    /**
     * Returns the list of all players in the game.
     *
     * @return the list of players
     */
    @Override
    public List<Object> getPlayers() {
        return players;
    }
    /**
     * Returns the game's main board.
     *
     * @return the board instance
     */
    @Override
    public Board getBoard(){return board;}

    /**
     * Retrieves the complete fleet of placed ships.
     *
     * @return the fleet list
     */
    @Override
    public List<IShip> getFleet() {
        return fleet;
    }

    @Override
    public Human getHuman(){ return human;}

}
