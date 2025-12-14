package com.example.battleship.Model.Game;

import com.example.battleship.Model.AI.SmartAI;
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
 * Main controller of the Battleship game.
 * <p>
 * This class manages players, turn order, boards, ship placement,
 * shooting logic, AI interaction, and overall game state.
 * </p>
 *
 * <p>
 * It implements {@link IGame} and encapsulates the full gameplay lifecycle:
 * setup, playing phase, and game termination.
 * </p>
 */
public class Game implements IGame {

    /** Human player instance. */
    private Human human;

    /** Machine (AI) player instance. */
    private Machine machine;

    /** Stores all players in the game. */
    private final List<Object> players;

    /** Queue controlling turn rotation. */
    private final Queue<Object> turnQueue;

    /** Fleet belonging to the machine player. */
    private List<IShip> machineFleet = new ArrayList<>();

    /** Fleet belonging to the human player. */
    private List<IShip> humanFleet = new ArrayList<>();

    /** Index of the currently active player. */
    private int currentPlayerIndex;

    /** Indicates whether the game has ended. */
    private boolean gameOver;

    /** Board used by the machine player. */
    private Board machineBoard;

    /** Board used by the human player. */
    private Board humanBoard;

    /** Current game state. */
    private GameState currentState;

    /** Tracks human shots to avoid duplicates. */
    private Set<String> humanShots = new HashSet<>();

    /** Tracks machine shots to avoid duplicates. */
    private Set<String> machineShots = new HashSet<>();

    /** Smart AI instance controlling machine behavior. */
    private final SmartAI smartAI = new SmartAI();

    /**
     * Represents the possible states of the game.
     */
    public enum GameState {
        SETUP,
        PLAYING,
        FINISHED
    }

    /**
     * Represents the result of a shot.
     */
    public enum ShotResult {
        MISS,
        HIT,
        SUNK,
        ALREADY_SHOT,
        INVALID
    }

    /**
     * Creates a new game instance and initializes players and boards.
     */
    public Game(String username) {
        this.players = new ArrayList<>();
        this.turnQueue = new LinkedList<>();
        this.machineBoard = new Board();
        this.humanBoard = new Board();
        this.currentState = GameState.SETUP;
        initializePlayers(username);
    }

    /**
     * Initializes human and machine players and adds them to the turn queue.
     */
    private void initializePlayers(String username) {
        human = new Human(username);
        players.add(human);
        turnQueue.add(human);

        machine = new Machine("CPU-");
        players.add(machine);
        turnQueue.add(machine);
    }

    /**
     * Starts the game after ship placement is completed.
     *
     * @throws InvalidGameStateException if the human fleet is empty
     */
    public void startGame() {
        if (humanFleet.isEmpty()) {
            throw new InvalidGameStateException("Player must place their ships first.");
        }

        smartAI.reset();
        currentState = GameState.PLAYING;
        currentPlayerIndex = 0;
    }

    /**
     * Checks if it is currently the human player's turn.
     *
     * @return {@code true} if human is the active player
     */
    public boolean isHumanTurn() {
        return getCurrentPlayer() == human;
    }

    /**
     * Checks if it is currently the machine player's turn.
     *
     * @return {@code true} if machine is the active player
     */
    public boolean isMachineTurn() {
        return getCurrentPlayer() == machine;
    }

    /**
     * Returns the currently active player.
     *
     * @return the active player or {@code null} if none exists
     */
    @Override
    public Object getCurrentPlayer() {
        List<Object> activePlayers = getActivePlayers();
        if (activePlayers.isEmpty()) { return null; }
        if (currentPlayerIndex >= activePlayers.size()) { currentPlayerIndex = 0; }
        return activePlayers.get(currentPlayerIndex);
    }

    /**
     * Returns a list of all active players.
     *
     * @return list of players
     */
    public List<Object> getActivePlayers() {
        return players.stream().toList();
    }

    /**
     * Advances the turn to the next player.
     * If only one player remains, the game ends.
     */
    @Override
    public void advanceTurn() {
        currentPlayerIndex++;

        // Check for winner after each turn
        if (getActivePlayers().size() == 1) {
            gameOver = true;
        }
    }

    // ================= SHIPS ACCOMMODATION =================

    /**
     * Places a human ship on the human board.
     *
     * @param ship ship to place
     * @throws InvalidPositionException if placement is invalid
     */
    public void placeHumanShip(IShip ship) throws InvalidPositionException {
        int[] d = calculateDisplacement(ship.getDirection());
        validateShipPlacement(ship, d[0], d[1], humanBoard);
        applyShipToBoard(ship, d[0], d[1], humanBoard);
        humanFleet.add(ship);
    }

    /**
     * Validates that a ship placement is within bounds and collision-free.
     *
     * @param ship ship to validate
     * @param dx row displacement
     * @param dy column displacement
     * @param board target board
     * @throws InvalidPositionException if placement is invalid
     */
    private void validateShipPlacement(IShip ship, int dx, int dy, Board board) throws InvalidPositionException {
        int row = ship.getRow();
        int col = ship.getCol();
        int size = ship.getShipSize();

        for (int i = 0; i < size; i++) {
            int r = row + dx * i;
            int c = col + dy * i;

            if (r < 0 || r >= 10 || c < 0 || c >= 10) {
                throw new InvalidPositionException("Out of bounds");
            }

            if (board.getCell(r, c) == 1) {
                throw new InvalidPositionException("Collision with another ship");
            }
        }
    }

    /**
     * Applies a ship placement to a board.
     */
    private void applyShipToBoard(IShip ship, int dx, int dy, Board board) {
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
     * Converts a ship direction into row and column displacement.
     *
     * @param dir ship direction
     * @return displacement array [dr, dc]
     */
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
     * Places a ship on the machine board.
     *
     * @param ship ship to place
     * @throws InvalidPositionException if placement is invalid
     */
    public void placeShip(IShip ship) throws InvalidPositionException {
        int[] d = calculateDisplacement(ship.getDirection());
        validateShipPlacement(ship, d[0], d[1], machineBoard);
        applyShipToBoard(ship, d[0], d[1], machineBoard);
    }

    /**
     * Generates the complete fleet using randomized coordinates.
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
     * Attempts to place a ship at a specific position using random directions.
     */
    private boolean tryPlaceShipAt(String type, int row, int col) {
        List<IShip.Direction> dirs = shuffleDirections();

        for (IShip.Direction dir : dirs) {
            IShip ship = createShip(type, row, col, dir);

            try {
                placeShip(ship);
                machineFleet.add(ship);
                return true;
            } catch (InvalidPositionException ignored) {}
        }

        return false;
    }

    /**
     * Shuffles available ship directions.
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
     * Creates a ship instance based on type.
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

    // =============== FIRING SYSTEM =======================

    /**
     * Executes a shot performed by the human player.
     */
    public ShotResult executeHumanShot(int row, int col) {
        if (!isHumanTurn()) {
            throw new InvalidGameStateException("It is not your turn");
        }

        if (currentState != GameState.PLAYING) {
            throw new InvalidGameStateException("The game has not started");
        }

        // Validate range 0-9
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return ShotResult.INVALID;
        }

        String key = row + "," + col;
        if (humanShots.contains(key)) {
            return ShotResult.ALREADY_SHOT;
        }

        humanShots.add(key);
        return processShot(row, col, machineBoard, machineFleet);
    }

    /**
     * Executes a shot performed by the machine player.
     *
     * @return array [row, col, result.ordinal()]
     */
    public int[] executeMachineShot() {
        if (!isMachineTurn()) {
            throw new InvalidGameStateException("It is not the machine's turn");
        }

        // Get shot and ensure it has not been used
        int[] shot;
        int maxAttempts = 100; // Prevent infinite loop
        int attempts = 0;

        do {
            shot = smartAI.getNextShot();
            attempts++;

            String key = shot[0] + "," + shot[1];

            if (!machineShots.contains(key)) {
                break; // Valid shot
            }

            System.out.println("⚠️ [AI] Already shot at (" + shot[0] + "," + shot[1] + "), retrying...");

            if (attempts >= maxAttempts) {
                System.err.println("❌ AI stuck, selecting random cell...");
                shot = getRandomUnusedCell();
                break;
            }

        } while (true);

        int row = shot[0];
        int col = shot[1];

        System.out.println("🤖 [AI] " + smartAI.getDebugInfo());
        System.out.println("🤖 [AI] Shooting at: (" + row + "," + col + ") [attempt " + attempts + "]");

        machineShots.add(row + "," + col);

        ShotResult result = processShot(row, col, humanBoard, humanFleet);

        boolean hit = (result == ShotResult.HIT || result == ShotResult.SUNK);
        boolean sunk = (result == ShotResult.SUNK);

        smartAI.registerResult(row, col, hit, sunk);

        System.out.println("🤖 [AI] Result: " + result +
                " (hit=" + hit + ", sunk=" + sunk + ")");

        if (sunk) {
            long sunkenCount = humanFleet.stream().filter(IShip::isSunken).count();
            System.out.println("🤖 [AI] Total player ships sunk: " + sunkenCount);
        }

        return new int[]{row, col, result.ordinal()};
    }

    // Helper method to get a random unused cell
    private int[] getRandomUnusedCell() {
        List<int[]> available = new ArrayList<>();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                String key = row + "," + col;
                if (!machineShots.contains(key)) {
                    available.add(new int[]{row, col});
                }
            }
        }

        if (available.isEmpty()) {
            return new int[]{0, 0}; // Extreme fallback
        }

        return available.get(new Random().nextInt(available.size()));
    }

    /**
     * Processes a shot on a board.
     */
    private ShotResult processShot(int row, int col, Board board, List<IShip> fleet) {
        int cell = board.getCell(row, col);

        if (cell == 0) {
            board.setCell(row, col, 2);
            System.out.println("💦 Water at (" + row + "," + col + ")");
            return ShotResult.MISS;
        } else if (cell == 1) {
            board.setCell(row, col, 3);
            System.out.println("💥 Hit at (" + row + "," + col + ")");

            for (IShip ship : fleet) {
                if (isShotInsideShip(row, col, ship)) {
                    ship.registerHit();

                    if (ship.isSunken()) {
                        System.out.println("🔥 Ship sunk: " + ship.getClass().getSimpleName());
                        checkGameOver();
                        return ShotResult.SUNK;
                    }
                    return ShotResult.HIT;
                }
            }
            return ShotResult.HIT;
        }

        return ShotResult.ALREADY_SHOT;
    }

    /**
     * Determines whether a shot is inside a ship.
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
     * Calculates row displacement based on ship direction.
     */
    private int calculateDeltaRow(IShip ship) {
        return switch (ship.getDirection()) {
            case DOWN -> 1;
            case UP -> -1;
            default -> 0;
        };
    }

    /**
     * Calculates column displacement based on ship direction.
     */
    private int calculateDeltaCol(IShip ship) {
        return switch (ship.getDirection()) {
            case RIGHT -> 1;
            case LEFT -> -1;
            default -> 0;
        };
    }

    //================== GAME OVER VERIFICATION ==================

    /**
     * Checks whether the game has ended.
     */
    private void checkGameOver() {
        long humanShipsSunk = humanFleet.stream().filter(IShip::isSunken).count();
        long machineShipsSunk = machineFleet.stream().filter(IShip::isSunken).count();

        if (humanShipsSunk == humanFleet.size() ||
                machineShipsSunk == machineFleet.size()) {
            gameOver = true;
            currentState = GameState.FINISHED;
        }
    }

    /**
     * Determines whether the human player has won.
     */
    public boolean hasHumanWon() {
        return machineFleet.stream().allMatch(IShip::isSunken);
    }

    /**
     * Determines whether the machine player has won.
     */
    public boolean hasMachineWon() {
        return humanFleet.stream().allMatch(IShip::isSunken);
    }

    //================= ACCESS METHODS =======================

    /**
     * Returns all board coordinates occupied by a ship.
     */
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
            coords.add(new int[]{currentRow, currentCol}); // [row, col]
        }

        return coords;
    }

    /**
     * Prints all machine fleet ship coordinates.
     */
    @Override
    public void printFleetCoordinates() {
        for (IShip ship : machineFleet) {
            System.out.println("--- " + ship.getClass().getSimpleName() + " ---");

            for (int[] c : getShipCoordinates(ship)) {
                // c[0] = row (Y-axis)
                // c[1] = col (X-axis)
                System.out.println("Row=" + c[0] + "  Col=" + c[1]);
            }
            System.out.println();
        }
    }

    //================ OTHER FUNCTIONS =====================

    /**
     * Executes a full human turn.
     */
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
     * Validates that the player has not already fired at the given cell.
     */
    private void validateNotRepeatedShot(Human player, int row, int col) {
        if (player.alreadyShot(row, col)) {
            System.out.println("You already shot at that position.");
            throw new InvalidShotException("You already shot at that position");
        }
    }

    /**
     * Registers a player's shot.
     */
    private void registerPlayerShot(Human player, int row, int col) {
        player.shoot(row, col);
    }

    /**
     * Checks if a cell represents water.
     */
    private boolean isWater(int cell) {
        return cell == 0;
    }

    /**
     * Handles a miss.
     */
    private boolean handleWater(Board board, int row, int col) {
        board.setCell(row, col, 2);
        System.out.println("Miss!");
        return false; // Turn ends
    }

    /**
     * Checks if a cell contains a ship.
     */
    private boolean isShip(int cell) {
        return cell == 1;
    }

    /**
     * Handles a successful ship hit.
     */
    private boolean handleShipHit(Board board, int row, int col) {
        board.setCell(row, col, 3);
        System.out.println("Hit!");

        for (IShip ship : machineFleet) {
            if (ValidateShot(row, col, ship)) {
                if (ship.isSunken()) {
                    System.out.println("🔥 You sunk a " + ship.getClass().getSimpleName());
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Validates whether a shot hits a ship.
     */
    public boolean ValidateShot(int row, int col, IShip ship) {
        if (isShotInsideShip(row, col, ship)) {
            ship.registerHit();
            return true;
        }
        return false;
    }

    /**
     * Executes the full human play cycle.
     */
    public void executeHumanPlay(Board board, Human human, int row, int col) {

        if (getCurrentPlayer() != human) {
            throw new InvalidGameStateException("It is not your turn");
        }

        boolean successfulShot = playTurn(board, human, row, col);

        if (!successfulShot) {
            System.out.println("End of player's turn.");
            advanceTurn();
        }
    }

    /**
     * Executes the machine's turn.
     */
    public void executeMachinePlay(){
        advanceTurn();
    }

    // ================ DEBUGGING =======================

    /**
     * Prints the visual state of a board.
     */
    public void printBoardState(String boardName, Board board) {
        System.out.println("\n=== " + boardName + " ===");
        System.out.print("   ");
        for (int c = 0; c < 10; c++) {
            System.out.print(c + " ");
        }
        System.out.println();

        for (int r = 0; r < 10; r++) {
            System.out.print(r + "| ");
            for (int c = 0; c < 10; c++) {
                int cell = board.getCell(r, c);
                String symbol = switch(cell) {
                    case 0 -> "·"; // Water
                    case 1 -> "■"; // Ship
                    case 2 -> "○"; // Miss
                    case 3 -> "X"; // Hit
                    default -> "?";
                };
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    //================== GETTERS =======================

    @Override
    public boolean isGameOver() {return gameOver;}

    @Override
    public List<Object> getPlayers() {return players;}

    @Override
    public Board getMachineBoard(){return machineBoard;}

    public Board getHumanBoard() {return humanBoard;}

    @Override
    public List<IShip> getMachineFleet() {return machineFleet;}

    @Override
    public Human getHuman(){ return human;}

    @Override
    public GameState getCurrentState() {return currentState;}

    public List<IShip> getHumanFleet() { return humanFleet; }

    public Set<String> getHumanShots() { return humanShots; }

    public Set<String> getMachineShots() { return machineShots; }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }

    // Setter methods for restoring game state
    public void setHumanBoard(Board board) { this.humanBoard = board; }
    public void setMachineBoard(Board board) { this.machineBoard = board; }
    public void setHumanFleet(List<IShip> fleet) { this.humanFleet = fleet; }
    public void setMachineFleet(List<IShip> fleet) { this.machineFleet = fleet; }
    public void setHumanShots(Set<String> shots) { this.humanShots = shots; }
    public void setMachineShots(Set<String> shots) { this.machineShots = shots; }
    public void setCurrentPlayerIndex(int index) { this.currentPlayerIndex = index; }
    public void setCurrentState(GameState state) { this.currentState = state; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public SmartAI getSmartAI() { return smartAI; }
}
