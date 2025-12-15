package com.example.battleship.Controllers;

import com.example.battleship.Model.Board.Board;
import com.example.battleship.Model.Game.Game;
import com.example.battleship.Model.Game.GameState;
import com.example.battleship.Model.Player.PlayerData;
import com.example.battleship.Model.Serializable.SerializableFileHandler;
import com.example.battleship.Model.Ship.*;
import com.example.battleship.Model.TextFile.PlaneTextFileHandler;
import com.example.battleship.Model.Utils.SpriteSheet;
import com.example.battleship.Views.GameView;
import com.example.battleship.Views.MainMenuView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import com.example.battleship.Views.VictoryView;
import javafx.scene.media.MediaException;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller class for the main game view in the Battleship application.
 * <p>
 * Handles all game logic, user interactions, ship placement, shooting mechanics,
 * UI updates, animations, and media playback. Manages two game boards (player and enemy),
 * ship placement during setup phase, turn-based gameplay between human and AI,
 * visual feedback through animations and sprite rendering, and game state persistence.
 * </p>
 *
 * <p><b>Key responsibilities:</b></p>
 * <ul>
 *   <li>Initialize game boards and fleet generation</li>
 *   <li>Handle ship placement with drag preview and rotation</li>
 *   <li>Process player and AI shooting turns</li>
 *   <li>Render game boards with ships, hits, misses, and sunken ships</li>
 *   <li>Manage game state saving and loading</li>
 *   <li>Control background video playback</li>
 *   <li>Coordinate multithreaded AI execution</li>
 * </ul>
 *
 * @author Battleship Development Team
 * @version 1.0
 * @see Game
 * @see Board
 * @see IShip
 */
public class GameController implements Initializable {

    @FXML
    private AnchorPane videoContainer;

    @FXML
    private Button GoBackButton;

    @FXML
    private Canvas playerCanvas;

    @FXML
    private Canvas enemyCanvas;

    @FXML
    private Button toggleShipsButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label turnLabel;

    @FXML
    private Button helpButton;

    private boolean showEnemyShips = false;
    private boolean videoWarmedUp = false;

    private GraphicsContext gPlayer;
    private GraphicsContext gEnemy;
    private MediaPlayer mediaPlayer;
    private Stage stage;

    private final int WIDTH_CELL = 364 / 10;
    private final int HEIGHT_CELL = 301 / 10;
    private final int SIZE = 10;
    private int currentShipSize = 4;
    private boolean vertical = false;

    private Board board;
    private Board boardEnemy;
    List<IShip> enemyShips;
    List<int[]> coords;
    private Stack<IShip> ships = new Stack<>();

    private Image missImage;
    private Image hitImage;
    private Image explosionImage;

    private final int[] fleet = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
    private int shipIndex = 0;

    private Game game;
    private final BoardRenderer boardRenderer = new BoardRenderer();

    private ExecutorService gameExecutor;
    private ExecutorService aiExecutor;
    private volatile boolean isRunning = false;

    private SerializableFileHandler serializableHandler = new SerializableFileHandler();
    private PlaneTextFileHandler plainTextFileHandler;
    private PlayerData currentPlayerData;

    private final SpriteSheet carrierSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/portaaviones.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private final SpriteSheet frigateSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/fragata.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private final SpriteSheet submarineSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/submarinos.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private final SpriteSheet destroyerSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/destructores.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    /**
     * Sets the stage for this controller.
     *
     * @param stage the stage to be associated with this controller
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Initializes the game controller.
     * <p>
     * Sets up canvases, graphics contexts, executor services, loads images,
     * configures event handlers for ship placement, and draws initial grids.
     * </p>
     *
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        plainTextFileHandler = new PlaneTextFileHandler();

        if (gameExecutor != null && !gameExecutor.isShutdown()) {
            gameExecutor.shutdownNow();
        }
        if (aiExecutor != null && !aiExecutor.isShutdown()) {
            aiExecutor.shutdownNow();
        }

        gPlayer = playerCanvas.getGraphicsContext2D();
        gEnemy = enemyCanvas.getGraphicsContext2D();

        gameExecutor = Executors.newSingleThreadExecutor();
        aiExecutor = Executors.newSingleThreadExecutor();

        addExplosionEffect(GoBackButton);

        missImage = new Image(getClass().getResource("/Battleship-Images/12.png").toExternalForm());
        hitImage = new Image(getClass().getResource("/Battleship-Images/11.png").toExternalForm());
        explosionImage = new Image(getClass().getResource("/Battleship-Images/13.png").toExternalForm());

        playerCanvas.setOnMouseMoved(this::previewShip);
        playerCanvas.setOnMouseClicked(this::placeShip);
        playerCanvas.setOnMouseExited(e -> drawPlacedShips());

        playerCanvas.setFocusTraversable(true);
        playerCanvas.setOnKeyPressed(this::rotate);

        Platform.runLater(() -> playerCanvas.requestFocus());

        playerCanvas.setOnMouseClicked(e -> {
            playerCanvas.requestFocus();
            placeShip(e);
        });

        drawGrid(gPlayer);
        drawGrid(gEnemy);
    }

    /**
     * Called when the scene is ready for video initialization.
     * <p>
     * Delays video setup briefly to ensure smooth loading without blocking UI initialization.
     * </p>
     */
    public void onSceneReady() {
        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.millis(500));
            delay.setOnFinished(e -> setupBackgroundVideo());
            delay.play();
        });
    }

    /**
     * Initializes a new game session for the specified player.
     * <p>
     * Creates a new game instance, generates enemy fleet, loads player data,
     * and prepares the board for ship placement phase.
     * </p>
     *
     * @param playerName the name of the player starting the game
     */
    public void initializeNewGame(String playerName) {
        game = new Game(playerName);
        System.out.println(game.getHuman().getName());
        game.generateFleet();

        boardEnemy = game.getMachineBoard();

        showEnemyShips = false;
        redrawEnemyBoard();

        currentPlayerData = loadPlayerData(playerName);
        if (currentPlayerData == null) {
            currentPlayerData = new PlayerData(playerName);
        }

        updateStatusLabel("📍 Coloca: Barco de 4 celdas");
        turnLabel.setText("Colocando: 1/" + fleet.length);
    }

    /**
     * Loads player data from persistent storage.
     * <p>
     * Reads the player data file and searches for the specified player's statistics.
     * </p>
     *
     * @param playerName the name of the player whose data should be loaded
     * @return PlayerData object if found, null otherwise
     */
    private PlayerData loadPlayerData(String playerName) {
        try {
            String[] data = plainTextFileHandler.readFromFile("player_data.txt");

            for (int i = 0; i < data.length; i++) {
                String[] fields = data[i].split(",");
                if (fields.length >= 5 && fields[0].equals(playerName)) {
                    return PlayerData.fromCSV(fields);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not load player data");
        }
        return null;
    }

    /**
     * Saves current player data to persistent storage.
     * <p>
     * Updates existing player record or creates new entry in the player data file.
     * </p>
     */
    private void savePlayerData() {
        try {
            String[] allData = plainTextFileHandler.readFromFile("player_data.txt");
            StringBuilder content = new StringBuilder();

            boolean playerFound = false;

            for (String line : allData) {
                if (!line.trim().isEmpty()) {
                    String[] fields = line.split(",");
                    if (fields.length >= 5 && fields[0].equals(currentPlayerData.getName())) {
                        content.append(currentPlayerData.toCSV()).append("\n");
                        playerFound = true;
                    } else {
                        content.append(line).append("\n");
                    }
                }
            }

            if (!playerFound) {
                content.append(currentPlayerData.toCSV()).append("\n");
            }

            plainTextFileHandler.writeToFile("player_data.txt", content.toString());
            System.out.println("💾 Player data saved (plain text file)");

        } catch (Exception e) {
            System.err.println("❌ Error saving player data: " + e.getMessage());
        }
    }

    /**
     * Loads a previously saved game state.
     * <p>
     * Restores all game state including boards, fleets, shots, current player,
     * and game phase. Redraws boards and either continues ship placement or
     * resumes active gameplay.
     * </p>
     *
     * @param savedState the saved game state to restore
     * @param username the name of the player
     */
    public void loadSavedGame(GameState savedState, String username) {
        game = new Game(username);

        game.setHumanBoard(savedState.getHumanBoard());
        game.setMachineBoard(savedState.getMachineBoard());
        game.setHumanFleet(savedState.getHumanFleet());
        game.setMachineFleet(savedState.getMachineFleet());
        game.setHumanShots(savedState.getHumanShots());
        game.setMachineShots(savedState.getMachineShots());
        game.setCurrentPlayerIndex(savedState.getCurrentPlayerIndex());
        game.setCurrentState(savedState.getGamePhase());
        game.setGameOver(savedState.isGameOver());

        currentPlayerData = loadPlayerData(savedState.getPlayerName());
        if (currentPlayerData == null) {
            currentPlayerData = new PlayerData(savedState.getPlayerName());
        }

        boardEnemy = game.getMachineBoard();
        ships = new Stack<>();
        ships.addAll(savedState.getHumanFleet());

        drawPlacedShips();
        redrawEnemyBoard();

        if (!savedState.getHumanFleet().isEmpty() && savedState.getHumanFleet().size() == fleet.length) {
            startGame();
        } else {
            shipIndex = savedState.getHumanFleet().size();
            if (shipIndex < fleet.length) {
                currentShipSize = fleet[shipIndex];
                updateStatusLabel("📍 Coloca: Barco de " + currentShipSize + " celdas");
                turnLabel.setText("COLOCANDO: " + (shipIndex + 1) + "/" + fleet.length);
            }
        }
    }

    /**
     * Saves the current game state to persistent storage.
     * <p>
     * Creates a GameState snapshot with all current game data and serializes it
     * to a file named after the player. Does not save if the game has already ended.
     * </p>
     */
    private void saveGame() {
        try {
            GameState savedState = new GameState(
                    game.getHuman().getName(),
                    game.getHumanBoard(),
                    game.getMachineBoard(),
                    game.getHumanFleet(),
                    game.getMachineFleet(),
                    game.getHumanShots(),
                    game.getMachineShots(),
                    game.getCurrentPlayerIndex(),
                    game.getCurrentState(),
                    game.isGameOver()
            );

            if (game.hasHumanWon()) {
                return;
            }

            String playerName = game.getHuman().getName().toLowerCase().trim();
            String filename = "game_save_" + playerName + ".dat";
            serializableHandler.serialize(filename, savedState);
            System.out.println("💾 Game saved: " + filename);

        } catch (Exception e) {
            System.err.println("❌ Error saving game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles ship rotation during placement phase.
     * <p>
     * Toggles between horizontal and vertical orientation when R or SPACE is pressed.
     * </p>
     *
     * @param ke the key event
     */
    private void rotate(KeyEvent ke) {
        if (ke.getCode() == KeyCode.R || ke.getCode() == KeyCode.SPACE) {
            vertical = !vertical;
            drawPlacedShips();
        }
    }

    /**
     * Draws the grid overlay on a canvas.
     * <p>
     * Creates a 10x10 grid with semi-transparent white lines.
     * </p>
     *
     * @param g the graphics context to draw on
     */
    private void drawGrid(GraphicsContext g) {
        g.setFill(Color.web("TRANSPARENT"));
        g.fillRect(0, 0, 364, 301);

        g.setStroke(Color.rgb(255, 255, 255, 0.3));
        g.setLineWidth(1);

        for (int i = 0; i <= SIZE; i++) {
            g.strokeLine(i * WIDTH_CELL, 0, i * WIDTH_CELL, 301);
        }

        for (int i = 0; i <= SIZE; i++) {
            g.strokeLine(0, i * HEIGHT_CELL, 364, i * HEIGHT_CELL);
        }
    }

    /**
     * Shows a preview of the ship being placed as the mouse moves.
     * <p>
     * Displays a semi-transparent overlay of the ship if it fits at the current position.
     * </p>
     *
     * @param e the mouse event
     */
    private void previewShip(MouseEvent e) {
        drawPlacedShips();

        if (currentShipSize == 0) return;

        int col = (int) (e.getX() / WIDTH_CELL);
        int row = (int) (e.getY() / HEIGHT_CELL);

        if (!fits(row, col, currentShipSize)) return;

        gPlayer.setFill(Color.rgb(0, 255, 255, 0.35));
        for (int i = 0; i < currentShipSize; i++) {
            gPlayer.fillRect(
                    col * WIDTH_CELL + (vertical ? 0 : i * WIDTH_CELL),
                    row * HEIGHT_CELL + (vertical ? i * HEIGHT_CELL : 0),
                    WIDTH_CELL, HEIGHT_CELL
            );
        }
    }

    /**
     * Handles ship placement on player board.
     * <p>
     * Validates position, creates the appropriate ship type, adds it to the game,
     * and advances to the next ship in the fleet.
     * </p>
     *
     * @param e the mouse event
     */
    private void placeShip(MouseEvent e) {
        if (currentShipSize == 0) return;

        int col = (int) (e.getX() / WIDTH_CELL);
        int row = (int) (e.getY() / HEIGHT_CELL);

        if (!fits(row, col, currentShipSize)) return;

        IShip.Direction dir = vertical ? IShip.Direction.DOWN : IShip.Direction.RIGHT;
        IShip newShip = createShip(currentShipSize, col, row, dir);
        ships.add(newShip);

        try {
            assert newShip != null;
            game.placeHumanShip(newShip);
            drawPlacedShips();
            advanceToNextShip();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Factory method to create a ship based on size.
     *
     * @param size the size of the ship (1-4)
     * @param col the starting column
     * @param row the starting row
     * @param dir the direction of the ship
     * @return the created ship instance, or null if size is invalid
     */
    private IShip createShip(int size, int col, int row, IShip.Direction dir) {
        return switch (size) {
            case 4 -> new AircraftCarrier(col, row, dir);
            case 3 -> new Submarine(col, row, dir);
            case 2 -> new Destroyer(col, row, dir);
            case 1 -> new Frigate(col, row, dir);
            default -> null;
        };
    }

    /**
     * Checks if a ship of given size fits at the specified position.
     * <p>
     * Validates that the ship stays within bounds and doesn't overlap existing ships.
     * </p>
     *
     * @param row the starting row
     * @param col the starting column
     * @param size the size of the ship
     * @return true if the ship fits, false otherwise
     */
    private boolean fits(int row, int col, int size) {
        Board humanBoard = game.getHumanBoard();

        for (int i = 0; i < size; i++) {
            int r = row + (vertical ? i : 0);
            int c = col + (vertical ? 0 : i);

            if (r >= SIZE || c >= SIZE) return false;
            if (humanBoard.getCell(r, c) == 1) return false;
        }
        return true;
    }

    /**
     * Redraws all placed ships and shot markers on the player board.
     * <p>
     * Clears the canvas, draws the grid, renders all ships using appropriate sprites,
     * displays shot markers (misses and hits), and marks sunken ships.
     * </p>
     */
    private void drawPlacedShips() {
        gPlayer.clearRect(0, 0, playerCanvas.getWidth(), playerCanvas.getHeight());
        drawGrid(gPlayer);

        for (IShip ship : ships) {
            int col = ship.getCol();
            int row = ship.getRow();
            int size = ship.getShipSize();
            IShip.Direction game_direction = ship.getDirection();
            boolean direction = (game_direction == IShip.Direction.DOWN);

            WritableImage[] images = switch (size) {
                case 1 -> frigateSheet.getSlices(size, direction);
                case 2 -> destroyerSheet.getSlices(size, direction);
                case 3 -> submarineSheet.getSlices(size, direction);
                case 4 -> carrierSheet.getSlices(size, direction);
                default -> null;
            };

            System.out.println("🎨 Drawing ship: size=" + size +
                    " dir=" + (direction ? "VERTICAL" : "HORIZONTAL") +
                    " pos=(" + row + "," + col + ")" +
                    " images=" + (images != null ? images.length : "null"));

            if (images != null) {
                for (int j = 0; j < size; j++) {
                    int drawCol = col + (direction ? 0 : j);
                    int drawRow = row + (direction ? j : 0);

                    System.out.println("  └─ Segment " + j + " at (" + drawRow + "," + drawCol +
                            ") img=" + (images[j] != null ? "✓" : "✗") +
                            " imgSize=" + (images[j] != null ?
                            images[j].getWidth() + "x" + images[j].getHeight() : "null"));

                    boardRenderer.drawTile(gPlayer, drawCol, drawRow, images[j]);
                }
            }
        }

        Board humanBoard = game.getHumanBoard();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cell = humanBoard.getCell(row, col);

                if (cell == 2) {
                    gPlayer.drawImage(missImage,
                            col * WIDTH_CELL,
                            row * HEIGHT_CELL,
                            WIDTH_CELL, HEIGHT_CELL);
                } else if (cell == 3) {
                    gPlayer.drawImage(explosionImage,
                            col * WIDTH_CELL,
                            row * HEIGHT_CELL,
                            WIDTH_CELL, HEIGHT_CELL);
                }
            }
        }

        for (IShip ship : ships) {
            if (ship.isSunken()) {
                drawPlayerSunkenShip(ship);
            }
        }
    }

    /**
     * Draws markers for a sunken ship on the player board.
     *
     * @param ship the sunken ship to mark
     */
    private void drawPlayerSunkenShip(IShip ship) {
        List<int[]> coords = game.getShipCoordinates(ship);

        for (int[] coord : coords) {
            int row = coord[0];
            int col = coord[1];

            gPlayer.drawImage(
                    hitImage,
                    col * WIDTH_CELL,
                    row * HEIGHT_CELL,
                    WIDTH_CELL,
                    HEIGHT_CELL
            );
        }
    }

    /**
     * Advances to the next ship in the placement sequence.
     * <p>
     * Increments ship index, updates UI labels, and starts the game
     * when all ships have been placed.
     * </p>
     */
    private void advanceToNextShip() {
        shipIndex++;

        if (shipIndex >= fleet.length) {
            currentShipSize = 0;
            playerCanvas.setOnMouseMoved(null);
            playerCanvas.setOnMouseClicked(null);
            System.out.println("🚢 Complete Fleet. Starting Game...");

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> startGame());
            pause.play();
            return;
        }

        currentShipSize = fleet[shipIndex];

        updateStatusLabel("📍 Coloca: Barco de " + currentShipSize + " celdas");
        turnLabel.setText("Colocando: " + (shipIndex + 1) + "/" + fleet.length);

        System.out.println("Place next ship (size " + currentShipSize + ")");
    }

    /**
     * Starts the active gameplay phase.
     * <p>
     * Transitions from ship placement to combat, initializes turn order,
     * enables enemy board interaction, and updates UI.
     * </p>
     */
    private void startGame() {
        game.startGame();
        isRunning = true;

        Platform.runLater(() -> {
            updateStatusLabel("¡Juego iniciado! Es tu turno - Click en el tablero enemigo");
            turnLabel.setText("TURNO: Jugador");

            enemyCanvas.setOnMouseClicked(this::onPlayerShot);

            System.out.println("✅ Handler registered on enemyCanvas");
            System.out.println("✅ Game state: " + game.getCurrentState());
            System.out.println("✅ Is human turn? " + game.isHumanTurn());
        });
    }

    /**
     * Executes the AI's turn.
     * <p>
     * Calculates and executes a shot, updates AI strategy based on result,
     * and handles the outcome through the standard shot result processing.
     * </p>
     */
    private void executeMachineTurn() {
        System.out.println("🤖 === MACHINE TURN ===");

        Platform.runLater(() -> {
            updateStatusLabel("🤖 La máquina está pensando...");
            turnLabel.setText("TURNO: Máquina");
        });

        try {
            Thread.sleep(500 + new Random().nextInt(1000));

            int[] shot = game.executeMachineShot();
            int row = shot[0];
            int col = shot[1];
            Game.ShotResult result = Game.ShotResult.values()[shot[2]];

            boolean hit = (result == Game.ShotResult.HIT || result == Game.ShotResult.SUNK);
            boolean sunk = (result == Game.ShotResult.SUNK);

            game.getSmartAI().registerResult(row, col, hit, sunk);

            System.out.println("🤖 Machine shoots: (" + row + "," + col + ") → " + result);

            Platform.runLater(() -> {
                handleShotResult(result, row, col, false);
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ ERROR in machine turn: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("🤖 === END MACHINE TURN ===\n");
    }

    /**
     * Draws the enemy fleet on the enemy board.
     * <p>
     * Retrieves enemy ships and delegates to ship rendering method.
     * </p>
     */
    private void drawEnemyFleet() {
        enemyShips = game.getMachineFleet();
        drawEnemyShips(enemyShips);
    }

    /**
     * Renders enemy ships on the enemy board using appropriate sprites.
     * <p>
     * For each ship, selects the correct sprite sheet, extracts slices,
     * sorts coordinates, and draws each segment in order.
     * </p>
     *
     * @param ships the list of ships to draw
     */
    private void drawEnemyShips(List<IShip> ships) {
        for (IShip ship : ships) {
            SpriteSheet sheet = switch (ship.getClass().getSimpleName()) {
                case "AircraftCarrier" -> carrierSheet;
                case "Destroyer" -> destroyerSheet;
                case "Submarine" -> submarineSheet;
                case "Frigate" -> frigateSheet;
                default -> null;
            };

            if (sheet == null) continue;

            int size = ship.getShipSize();
            boolean vertical = (ship.getDirection() == IShip.Direction.UP ||
                    ship.getDirection() == IShip.Direction.DOWN);

            WritableImage[] slices = sheet.getSlices(size, vertical);

            List<int[]> coords = game.getShipCoordinates(ship);

            coords.sort((a, b) -> {
                if (vertical) {
                    return Integer.compare(a[0], b[0]);
                } else {
                    return Integer.compare(a[1], b[1]);
                }
            });

            for (int i = 0; i < size; i++) {
                int row = coords.get(i)[0];
                int col = coords.get(i)[1];

                gEnemy.drawImage(
                        slices[i],
                        col * WIDTH_CELL,
                        row * HEIGHT_CELL,
                        WIDTH_CELL,
                        HEIGHT_CELL
                );
            }
        }
    }

    /**
     * Legacy method for handling board clicks.
     * <p>
     * May be unused - replaced by direct shot handling methods.
     * </p>
     *
     * @param enemyBoard the board being clicked
     * @param row the row coordinate
     * @param col the column coordinate
     */
    private void handleBoardClick(Board enemyBoard, int row, int col) {
        game.executeHumanPlay(enemyBoard, game.getHuman(), row, col);
    }

    /**
     * Handles player shot when clicking on enemy board.
     * <p>
     * Validates turn state, calculates shot coordinates, executes shot
     * asynchronously, and processes the result.
     * </p>
     *
     * @param event the mouse event
     */
    private void onPlayerShot(MouseEvent event) {
        if (!game.isHumanTurn() || game.getCurrentState() != Game.GameState.PLAYING) {
            updateStatusLabel("¡No es tu turno!");
            return;
        }

        int col = (int) (event.getX() / WIDTH_CELL);
        int row = (int) (event.getY() / HEIGHT_CELL);

        System.out.println("🎯 Shot at: (" + row + "," + col + ")");

        CompletableFuture.runAsync(() -> {
            try {
                Game.ShotResult result = game.executeHumanShot(row, col);

                Platform.runLater(() -> {
                    handleShotResult(result, row, col, true);
                });

            } catch (Exception e) {
                Platform.runLater(() -> updateStatusLabel("Error: " + e.getMessage()));
            }
        }, gameExecutor);

        redrawEnemyBoard();
    }

    /**
     * Processes the result of a shot (from either player or AI).
     * <p>
     * Updates boards, displays appropriate messages, manages turn order,
     * handles game over conditions, and saves game state.
     * </p>
     *
     * @param result the outcome of the shot
     * @param row the row coordinate of the shot
     * @param col the column coordinate of the shot
     * @param isPlayer true if the shot was from the player, false if from AI
     */
    private void handleShotResult(Game.ShotResult result, int row, int col, boolean isPlayer) {
        System.out.println("📊 Processing result: " + result + " for " + (isPlayer ? "PLAYER" : "MACHINE"));

        if (isPlayer) {
            redrawEnemyBoard();
        } else {
            drawPlacedShips();
        }

        String target = isPlayer ? "enemy" : "player";

        switch (result) {
            case MISS -> {
                updateStatusLabel(isPlayer ?
                        "💦 Agua... Turno de la máquina" :
                        "💦 La máquina falló. ¡Tu turno!");

                game.advanceTurn();

                System.out.println("⏭️ Turn advanced. New turn: " +
                        (game.isHumanTurn() ? "PLAYER" : "MACHINE"));

                if (isPlayer) {
                    turnLabel.setText("TURNO: Máquina");
                    scheduleAITurn();
                } else {
                    turnLabel.setText("TURNO: Jugador");
                }
            }
            case HIT -> {
                updateStatusLabel(isPlayer ?
                        "💥 ¡Impacto! Dispara de nuevo" :
                        "💥 ¡La máquina te dio! Ella dispara de nuevo");

                System.out.println("🔄 Same turn continues");

                if (!isPlayer) {
                    scheduleAITurn();
                }
            }
            case SUNK -> {
                updateStatusLabel(isPlayer ?
                        "🔥 ¡Hundiste un barco! Dispara de nuevo" :
                        "🔥 ¡La máquina hundió tu barco! Ella sigue");

                System.out.println("🔄 Same turn continues (ship sunk)");

                if (!isPlayer) {
                    scheduleAITurn();
                }
            }
            case ALREADY_SHOT -> {
                System.out.println("⚠️ ALREADY_SHOT");
                updateStatusLabel(isPlayer ?
                        "Ya disparaste ahí, intenta de nuevo" :
                        "La máquina disparó a una celda repetida");

                if (!isPlayer) {
                    System.out.println("   🤖 AI shot repeated cell, retrying...");
                    scheduleAITurn();
                }
            }

            case INVALID -> {
                System.out.println("❌ INVALID");
                updateStatusLabel(isPlayer ?
                        "Disparo inválido" :
                        "La máquina hizo un disparo inválido");

                if (!isPlayer) {
                    System.out.println("   🤖 AI made invalid shot, retrying...");
                    scheduleAITurn();
                }
            }
        }

        if (game.isGameOver()) {
            System.out.println("🏁 GAME OVER");
            serializableHandler.delete("game_save.dat");
            endGame();
            return;
        }

        saveGame();
    }

    /**
     * Schedules the AI's turn with appropriate delay and error handling.
     * <p>
     * Disables player interaction, waits briefly, executes machine turn,
     * and re-enables interaction when it's the player's turn again.
     * </p>
     */
    private void scheduleAITurn() {
        System.out.println("🤖 Scheduling machine turn...");
        System.out.println("   Game state: " + game.getCurrentState());

        enemyCanvas.setOnMouseClicked(null);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);

                if (game.getCurrentState() != Game.GameState.PLAYING) {
                    System.out.println("❌ Game is not active");
                    Platform.runLater(() -> {
                        enemyCanvas.setOnMouseClicked(this::onPlayerShot);
                    });
                    return;
                }

                System.out.println("✅ Executing machine turn...");
                executeMachineTurn();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("❌ Thread interrupted");
            } catch (Exception e) {
                System.err.println("❌ ERROR in scheduleAITurn: " + e.getMessage());
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    System.out.println("🔄 Finally block - checking turn...");
                    System.out.println("   Is human turn? " + game.isHumanTurn());

                    if (game.isHumanTurn() && game.getCurrentState() == Game.GameState.PLAYING) {
                        System.out.println("✅ Re-enabling player clicks");
                        enemyCanvas.setOnMouseClicked(this::onPlayerShot);
                    } else if (game.isMachineTurn() && game.getCurrentState() == Game.GameState.PLAYING) {
                        System.out.println("🤖 Still machine's turn, continuing...");
                    }
                });
            }
        }, aiExecutor);
    }

    /**
     * Ends the game and displays results.
     * <p>
     * Stops executors, counts statistics, updates player data, deletes save file,
     * closes current window, and opens victory/defeat screen.
     * </p>
     */
    private void endGame() {
        // Prevenir múltiples llamadas
        if (!isRunning) {
            System.out.println("⚠️ endGame ya fue llamado, ignorando");
            return;
        }

        isRunning = false;

        System.out.println("🏁 === ENDING GAME ===");

        // Deshabilitar interacción inmediatamente
        Platform.runLater(() -> {
            if (enemyCanvas != null) {
                enemyCanvas.setOnMouseClicked(null);
            }
            if (playerCanvas != null) {
                playerCanvas.setOnMouseClicked(null);
                playerCanvas.setOnMouseMoved(null);
            }
        });

        // Detener executors de forma segura
        if (gameExecutor != null && !gameExecutor.isShutdown()) {
            gameExecutor.shutdownNow();
            try {
                if (!gameExecutor.awaitTermination(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.err.println("⚠️ gameExecutor did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (aiExecutor != null && !aiExecutor.isShutdown()) {
            aiExecutor.shutdownNow();
            try {
                if (!aiExecutor.awaitTermination(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.err.println("⚠️ aiExecutor did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Calcular estadísticas
        int playerShipsSunk = (int) game.getMachineFleet().stream()
                .filter(IShip::isSunken)
                .count();

        int machineShipsSunk = (int) ships.stream()
                .filter(IShip::isSunken)
                .count();

        int playerMisses = 0;
        int machineMisses = 0;

        Board machineBoard = game.getMachineBoard();
        Board humanBoard = game.getHumanBoard();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (machineBoard.getCell(row, col) == 2) playerMisses++;
                if (humanBoard.getCell(row, col) == 2) machineMisses++;
            }
        }

        boolean playerWon = game.hasHumanWon();

        // Actualizar datos del jugador
        currentPlayerData.incrementGamesPlayed();
        currentPlayerData.addShots(game.getHumanShots().size());

        int hits = 0;
        for (String shot : game.getHumanShots()) {
            String[] coords = shot.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            if (machineBoard.getCell(row, col) == 3) {
                hits++;
            }
        }
        currentPlayerData.addHits(hits);

        if (playerWon) {
            currentPlayerData.incrementGamesWon();
        }

        savePlayerData();

        // Guardar valores finales para el siguiente paso
        final int finalPlayerShipsSunk = playerShipsSunk;
        final int finalMachineShipsSunk = machineShipsSunk;
        final int finalPlayerMisses = playerMisses;
        final int finalMachineMisses = machineMisses;
        final int finalHits = hits;
        final int finalTotalShots = game.getHumanShots().size();
        final String finalPlayerName = currentPlayerData.getName();

        // Eliminar archivo de guardado
        String playerName = finalPlayerName.toLowerCase().trim();
        String filename = "game_save_" + playerName + ".dat";
        serializableHandler.delete(filename);
        System.out.println("🗑️ Save file deleted: " + filename);

        // Esperar un momento antes de la transición
        Platform.runLater(() -> {
            try {
                System.out.println("🎬 Preparing victory/defeat screen...");

                // Detener el video del juego
                stopVideo();

                // Obtener el stage actual
                Stage currentStage = (Stage) videoContainer.getScene().getWindow();

                // Crear la vista de victoria/derrota
                VictoryView victoryView = VictoryView.getInstance();

                // Configurar las estadísticas
                victoryView.getController().setGameStats(
                        finalPlayerName,
                        playerWon,
                        finalPlayerShipsSunk,
                        finalMachineShipsSunk,
                        finalTotalShots,
                        finalHits,
                        finalPlayerMisses,
                        finalMachineMisses
                );

                // IMPORTANTE: Mostrar la nueva ventana ANTES de cerrar la actual
                // Esto evita que la app se cierre completamente
                victoryView.show();

                // Dar tiempo para que se renderice la nueva ventana
                PauseTransition delay = new PauseTransition(Duration.millis(150));
                delay.setOnFinished(e -> {
                    // Ahora sí cerrar la ventana del juego
                    currentStage.close();
                    System.out.println("✅ Game window closed, Victory window opened");
                });
                delay.play();

            } catch (Exception e) {
                System.err.println("❌ Error ending game: " + e.getMessage());
                e.printStackTrace();
            }
        });

        System.out.println("🏁 === END GAME COMPLETE ===");
    }

    /**
     * Redraws the enemy board with current state.
     * <p>
     * Clears canvas, draws grid, conditionally shows enemy ships (if debug mode enabled),
     * draws all shot markers, and marks sunken ships.
     * </p>
     */
    private void redrawEnemyBoard() {
        gEnemy.clearRect(0, 0, enemyCanvas.getWidth(), enemyCanvas.getHeight());

        drawGrid(gEnemy);

        if (showEnemyShips) {
            List<IShip> enemyFleet = game.getMachineFleet();
            List<IShip> activeShips = enemyFleet.stream()
                    .filter(ship -> !ship.isSunken())
                    .toList();

            drawEnemyShips(activeShips);
        }

        drawAllShotMarkers();
    }

    /**
     * Draws all shot markers on the enemy board.
     * <p>
     * Iterates through all cells, displays misses (water) and hits (explosions),
     * and marks sunken ships with special indicators.
     * </p>
     */
    private void drawAllShotMarkers() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cell = boardEnemy.getCell(row, col);

                if (cell == 2) {
                    gEnemy.drawImage(missImage,
                            col * WIDTH_CELL,
                            row * HEIGHT_CELL,
                            WIDTH_CELL, HEIGHT_CELL);
                } else if (cell == 3) {
                    gEnemy.drawImage(explosionImage,
                            col * WIDTH_CELL,
                            row * HEIGHT_CELL,
                            WIDTH_CELL, HEIGHT_CELL);
                }
            }
        }

        List<IShip> enemyFleet = game.getMachineFleet();
        for (IShip ship : enemyFleet) {
            if (ship.isSunken()) {
                drawSunkenShip(ship);
            }
        }
    }

    /**
     * Draws special markers for a sunken enemy ship.
     *
     * @param ship the sunken ship to mark
     */
    private void drawSunkenShip(IShip ship) {
        List<int[]> coords = game.getShipCoordinates(ship);

        for (int[] coord : coords) {
            int row = coord[0];
            int col = coord[1];

            gEnemy.drawImage(
                    hitImage,
                    col * WIDTH_CELL,
                    row * HEIGHT_CELL,
                    WIDTH_CELL,
                    HEIGHT_CELL
            );
        }
    }

    /**
     * Updates the status label with a new message.
     *
     * @param text the message to display
     */
    private void updateStatusLabel(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    /**
     * Sets up and plays the background video.
     * <p>
     * Loads the game video, configures looping, performs preloading optimization
     * to prevent initial stuttering, handles errors, and starts playback.
     * </p>
     */
    private void setupBackgroundVideo() {
        Platform.runLater(() -> {
            try {
                System.out.println("🎬 Starting video setup...");

                videoContainer.getChildren().removeIf(n -> n instanceof MediaView);

                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    } catch (Exception e) {
                        System.err.println("⚠️ Error cleaning up mediaPlayer: " + e.getMessage());
                    }
                    mediaPlayer = null;
                }

                Media media = new Media(
                        getClass().getResource("/Battleship-Videos/Game.mp4").toExternalForm()
                );

                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(0.3);

                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
                mediaView.setPreserveRatio(false);
                mediaView.setMouseTransparent(true);

                videoContainer.getChildren().add(0, mediaView);

                mediaPlayer.setOnReady(() -> {
                    System.out.println("✅ Video ready - Starting preload");

                    mediaPlayer.pause();

                    mediaPlayer.seek(Duration.seconds(0.5));

                    PauseTransition loadBuffer = new PauseTransition(Duration.millis(300));
                    loadBuffer.setOnFinished(e1 -> {
                        System.out.println("🔄 Buffer loaded, returning to start...");

                        mediaPlayer.seek(Duration.ZERO);

                        PauseTransition finalWait = new PauseTransition(Duration.millis(200));
                        finalWait.setOnFinished(e2 -> {
                            System.out.println("▶️ Playing video (preloaded)");
                            mediaPlayer.play();
                        });
                        finalWait.play();
                    });
                    loadBuffer.play();
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("❌ Video error: " + mediaPlayer.getError());
                    mediaPlayer.getError().printStackTrace();
                });

                mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                    System.out.println("📊 Video status: " + oldStatus + " → " + newStatus);
                });

            } catch (Exception e) {
                System.err.println("❌ Error loading video: " + e.getMessage());
                e.printStackTrace();
                videoContainer.setStyle("-fx-background-color: #001a33;");
            }
        });
    }

    /**
     * Stops and disposes of the background video player.
     */
    public void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    /**
     * Adds explosion effect and shake animation to a button.
     * <p>
     * Attaches click handler that triggers particle effects, button shake,
     * and executes the appropriate action after a delay.
     * </p>
     *
     * @param button the button to enhance with effects
     */
    private void addExplosionEffect(Button button) {
        button.setOnMouseClicked(event -> {
            event.consume();

            double centerX = button.getLayoutX() + button.getWidth() / 2;
            double centerY = button.getLayoutY() + button.getHeight() / 2;

            createExplosion(centerX, centerY);
            shakeButton(button);

            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> {
                if (button == GoBackButton) {
                    onBackMenu();
                }
            });
            pause.play();
        });
    }

    /**
     * Creates a particle-based explosion animation.
     * <p>
     * Generates multiple colored particles that radiate outward from the center,
     * fading and shrinking as they travel.
     * </p>
     *
     * @param x the x-coordinate of the explosion center
     * @param y the y-coordinate of the explosion center
     */
    private void createExplosion(double x, double y) {
        if (videoContainer == null) {
            System.out.println("videoContainer is null, explosion cancelled");
            return;
        }
        Random random = new Random();
        int particleCount = 30;

        for (int i = 0; i < particleCount; i++) {
            Circle particle = new Circle(3);
            particle.setFill(Color.rgb(255, random.nextInt(100) + 50, 0));
            particle.setLayoutX(x);
            particle.setLayoutY(y);

            videoContainer.getChildren().add(particle);

            double angle = Math.toRadians(random.nextInt(360));
            double distance = random.nextDouble() * 100 + 50;
            double targetX = x + Math.cos(angle) * distance;
            double targetY = y + Math.sin(angle) * distance;

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(particle.layoutXProperty(), x),
                            new KeyValue(particle.layoutYProperty(), y),
                            new KeyValue(particle.opacityProperty(), 1.0),
                            new KeyValue(particle.radiusProperty(), 3)
                    ),
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(particle.layoutXProperty(), targetX),
                            new KeyValue(particle.layoutYProperty(), targetY),
                            new KeyValue(particle.opacityProperty(), 0),
                            new KeyValue(particle.radiusProperty(), 0)
                    )
            );

            timeline.play();
            timeline.setOnFinished(e -> videoContainer.getChildren().remove(particle));
        }
    }

    /**
     * Applies a shaking animation to a button.
     * <p>
     * Creates a rapid back-and-forth motion to simulate impact.
     * </p>
     *
     * @param button the button to shake
     */
    private void shakeButton(Button button) {
        Timeline shake = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(button.translateXProperty(), 0),
                        new KeyValue(button.translateYProperty(), 0)
                ),
                new KeyFrame(Duration.millis(50),
                        new KeyValue(button.translateXProperty(), -5),
                        new KeyValue(button.translateYProperty(), 3)
                ),
                new KeyFrame(Duration.millis(100),
                        new KeyValue(button.translateXProperty(), 5),
                        new KeyValue(button.translateYProperty(), -3)
                ),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(button.translateXProperty(), -3),
                        new KeyValue(button.translateYProperty(), 2)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(button.translateXProperty(), 0),
                        new KeyValue(button.translateYProperty(), 0)
                )
        );
        shake.play();
    }

    /**
     * Loads the main menu view.
     * <p>
     * Saves current game if active, stops executors and video, closes current stage,
     * and creates new main menu instance.
     * </p>
     */
    private void loadMainMenuView() {
        try {
            isRunning = false;

            if (gameExecutor != null) {
                gameExecutor.shutdownNow();
            }
            if (aiExecutor != null) {
                aiExecutor.shutdownNow();
            }

            if (game != null && game.getCurrentState() == Game.GameState.PLAYING) {
                saveGame();
                System.out.println("💾 Game saved before returning to menu");
            }

            stopVideo();

            Stage currentStage = (Stage) GoBackButton.getScene().getWindow();

            GameView.deleteInstance();
            MainMenuView.deleteInstance();

            MainMenuView mainMenuView = MainMenuView.getInstance();

            currentStage.close();

        } catch (Exception e) {
            System.err.println("Error loading MainMenu view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the back to menu button action.
     * <p>
     * Stops game execution and loads the main menu.
     * </p>
     */
    @FXML
    private void onBackMenu() {
        isRunning = false;

        if (gameExecutor != null) {
            gameExecutor.shutdownNow();
        }
        if (aiExecutor != null) {
            aiExecutor.shutdownNow();
        }

        stopVideo();
        System.out.println("Going back to main menu...");
        loadMainMenuView();
    }

    /**
     * Displays the help dialog with game instructions.
     * <p>
     * Shows fleet composition, controls, and gameplay rules in a styled alert.
     * </p>
     */
    @FXML
    private void showHelp() {
        shakeButton(helpButton);
        createExplosion(helpButton.getLayoutX() + helpButton.getWidth() / 2,
                helpButton.getLayoutY() + helpButton.getHeight() / 2);

        String Title = "Ayuda - Batalla Naval";
        String Header = "📋 Cómo jugar";
        String rules = """
            Primero coloca todos los barcos (el juego avanza automáticamente al siguiente tipo):
            
            🚢 Flota disponible:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            1. Portaaviones:  1 unidad  —  4 casillas
            2. Submarinos:    2 unidades — 3 casillas c/u
            3. Destructores:  3 unidades — 2 casillas c/u
            4. Fragatas:      4 unidades — 1 casilla c/u
            
            ⌨️ Controles:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • Presiona 'R' para cambiar la orientación
            • Click en el tablero para colocar el barco
            
            ⚔️ Cuando termines de colocar todos los barcos,
            podrás comenzar a atacar al enemigo.
            """;
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, Title, Header, rules, "my-info-alert");
        alert.showAndWait();
    }

    /**
     * Creates a styled alert dialog with custom CSS.
     *
     * @param type the type of alert
     * @param title the alert title
     * @param header the alert header text
     * @param content the alert content text
     * @param styleClass the CSS style class to apply
     * @return the configured Alert instance
     */
    private Alert createStyledAlert(Alert.AlertType type, String title, String header, String content, String styleClass) {
        Alert alert = new Alert(type);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.setGraphic(null);
        alert.getDialogPane().setMinWidth(550);
        alert.getDialogPane().setMinHeight(400);

        DialogPane dialogPane = alert.getDialogPane();
        URL css = GameController.this.getClass().getResource("/Styles.css");
        if (css != null) {
            dialogPane.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("⚠️ CSS not found at: /Styles.css");
        }
        dialogPane.getStyleClass().add(styleClass);

        return alert;
    }

    /**
     * Toggles visibility of enemy ships on the board.
     * <p>
     * Debug feature that allows viewing enemy ship positions.
     * Updates button text accordingly.
     * </p>
     */
    @FXML
    private void toggleEnemyShips() {
        showEnemyShips = !showEnemyShips;

        addExplosionEffect(toggleShipsButton);

        redrawEnemyBoard();

        if (showEnemyShips) {
            toggleShipsButton.setText("Ocultar Barcos");
            System.out.println("👁️ Showing enemy ships");
        } else {
            toggleShipsButton.setText("Mostrar Barcos");
            System.out.println("🙈 Hiding enemy ships");
        }
    }

    /**
     * Utility class for rendering board tiles.
     * <p>
     * Handles drawing images at specific grid coordinates with proper scaling.
     * </p>
     */
    public static class BoardRenderer {

        private final int WIDTH_CELL = 364 / 10;
        private final int HEIGHT_CELL = 301 / 10;

        /**
         * Draws an image tile at the specified grid coordinates.
         *
         * @param g the graphics context to draw on
         * @param x the column coordinate (0-9)
         * @param y the row coordinate (0-9)
         * @param img the image to draw
         */
        public void drawTile(GraphicsContext g, int x, int y, Image img) {
            g.drawImage(img, x * WIDTH_CELL, y * HEIGHT_CELL, WIDTH_CELL, HEIGHT_CELL);
        }
    }
}