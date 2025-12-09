package com.example.miniproyecto4.Model.game;

import com.example.miniproyecto4.Model.Coordinates.Coordinates;
import com.example.miniproyecto4.Model.board.Board;
import com.example.miniproyecto4.Model.exceptions.InvalidPositionException;
import com.example.miniproyecto4.Model.exceptions.InvalidShotException;
import com.example.miniproyecto4.Model.player.Human;
import com.example.miniproyecto4.Model.player.Machine;
import com.example.miniproyecto4.Model.ship.*;

import java.util.*;

public class Game {
    private Human human;
    private final List<Object> players;
    private final Queue<Object> turnQueue;
    private List<IShip> fleet = new ArrayList<>();
    private int currentPlayerIndex;
    private boolean gameOver;
    private final Board board;

    public Game() {
        this.players = new ArrayList<>();
        this.turnQueue = new LinkedList<>();
        this.board = new Board();
        initializePlayers();
    }

    private void initializePlayers() {
        Human human = new Human("You");
        players.add(human);
        turnQueue.add(human);

        Machine machine = new Machine("CPU-");
        players.add(machine);
        turnQueue.add(machine);
    }

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

    public List<Object> getActivePlayers() {
        return players.stream().toList();
    }

    public void advanceTurn() {
        currentPlayerIndex++;

        // Check for winner after each turn
        if (getActivePlayers().size() == 1) {
            gameOver = true;
        }
    }

    public void placeShip(IShip ship) throws InvalidPositionException {
        int[] d = calculateDisplacement(ship.getDirection());
        validateShipPlacement(ship, d[0], d[1]);
        applyShipToBoard(ship, d[0], d[1]);
    }

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

    public void generateFleet(){
        Coordinates coordinates = new Coordinates();
        List<int[]> randomCoords = coordinates.getCoordinates();

        placeMultiple(randomCoords, 1, "AircraftCarrier");
        placeMultiple(randomCoords, 2, "Destroyer");
        placeMultiple(randomCoords, 3, "Submarine");
        placeMultiple(randomCoords, 4, "Frigate");
    }

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

    private IShip createShip(String type, int x, int y, IShip.Direction dir) {
        return switch (type) {
            case "AircraftCarrier" -> new AircraftCarrier(x, y, dir);
            case "Destroyer" -> new Destroyer(x, y, dir);
            case "Frigate" -> new Frigate(x, y, dir);
            case "Submarine" -> new Submarine(x, y, dir);
            default -> throw new IllegalArgumentException("Unknown ship type: " + type);
        };
    }


    // cambie en playturn, ahora coge row col
    public boolean playTurn(Board board, Human player, int row, int col){
        // 1. Revisar si ya disparó ahí
        if (player.alreadyShot(row, col)) {
            System.out.println("Ya disparaste a esa posición.");
            throw new InvalidShotException("Ya disparaste a esa posición");
        }

        // Registrar el disparo
        player.shoot(row, col);

        int cell = board.getCell(row, col);  // Ahora es consistente

        if (cell == 0) {
            board.setCell(row, col, 2);
            System.out.println("Agua!");
        }
        else if (cell == 1) {
            board.setCell(row, col, 3);
            System.out.println("¡Impacto!");

            for (IShip ship : fleet) {
                if (ValidateShot(player, ship)) {
                    if (ship.isSunken()) {
                        System.out.println("🔥 Hundiste un " + ship.getClass().getSimpleName());
                    }
                    return true;
                }
            }
        }
        return false;
    }


    // cambios asi tmb
    public Boolean ValidateShot(Human player, IShip ship){
        for (int[] shot : player.getShots()) {
            int shotRow = shot[0];  // fila
            int shotCol = shot[1];  // columna

            int shipRow = ship.getRow();
            int shipCol = ship.getCol();

            int dr = 0, dc = 0;  // delta row, delta col

            switch (ship.getDirection()) {
                case RIGHT -> dc = 1;   // RIGHT mueve en columnas
                case LEFT -> dc = -1;
                case DOWN -> dr = 1;    // DOWN mueve en filas
                case UP -> dr = -1;
            }

            for (int i = 0; i < ship.getShipSize(); i++) {
                int currentRow = shipRow + dr * i;
                int currentCol = shipCol + dc * i;

                if (shotRow == currentRow && shotCol == currentCol) {
                    ship.registerHit();
                    return true;
                }
            }
        }

        return false;
    }

    // cambio asi tmb
    public void executeHumanPlay(Board board, Human human, int row, int col){
        boolean successfulShot = playTurn(board, human, row, col);

        if (!successfulShot) {
            System.out.println("Fin del turno del jugador.");
            advanceTurn();
        }
    }

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

    public boolean isGameOver() {
        return gameOver;
    }
    public List<Object> getPlayers() {
        return players;
    }
    public Board getBoard(){return board;}

}
