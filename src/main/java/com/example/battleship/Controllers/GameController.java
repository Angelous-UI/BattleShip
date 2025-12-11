package com.example.battleship.Controllers;

import com.example.battleship.Model.Board.Board;
import com.example.battleship.Model.Game.Game;
import com.example.battleship.Model.Ship.*;
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
import com.example.battleship.Model.Game.GameStateHolder;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameController implements Initializable {

    @FXML private AnchorPane videoContainer;
    @FXML private Button GoBackButton;
    @FXML private Canvas playerCanvas;
    @FXML private Canvas enemyCanvas;
    @FXML private Button toggleShipsButton;
    @FXML private Label statusLabel;
    @FXML private Label turnLabel;

    private boolean showEnemyShips = true;

    private GraphicsContext gPlayer;
    private GraphicsContext gEnemy;

    private MediaPlayer mediaPlayer;
    private Stage stage;

    private final int WIDTH_CELL = 364/10;
    private final int HEIGHT_CELL = 301/10;

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

    // ================= FLEET ORDER =================
    private final int[] fleet = {4,3,3,2,2,2,1,1,1,1};
    private int shipIndex = 0;

    // ================= GAME ========================
    private Game game;
    private final BoardRenderer boardRenderer = new BoardRenderer();

    // ================= THREADS =====================
    private ExecutorService gameExecutor;
    private ExecutorService aiExecutor;
    private volatile boolean isRunning = false;

    // ================= SPRITES =====================
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


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (gameExecutor != null && !gameExecutor.isShutdown()) {
            gameExecutor.shutdownNow();
        }
        if (aiExecutor != null && !aiExecutor.isShutdown()) {
            aiExecutor.shutdownNow();
        }

        game = new Game();
        game.generateFleet();

        gPlayer = playerCanvas.getGraphicsContext2D();
        gEnemy = enemyCanvas.getGraphicsContext2D();

        gameExecutor = Executors.newSingleThreadExecutor();
        aiExecutor = Executors.newSingleThreadExecutor();

        setupBackgroundVideo();
        addExplosionEffect(GoBackButton);

        missImage = new Image(getClass().getResource("/Battleship-Images/12.png").toExternalForm());
        hitImage = new Image(getClass().getResource("/Battleship-Images/11.png").toExternalForm());
        explosionImage = new Image(getClass().getResource("/Battleship-Images/13.png").toExternalForm());

        // Verificar si hay un juego guardado temporalmente
        if (GameStateHolder.hasSavedGame()) {
            // Cargar el juego guardado
            game = GameStateHolder.getSavedGame();

            // Restaurar referencias
            boardEnemy = game.getMachineBoard();
            ships.clear();
            ships.addAll(game.getHumanFleet());

            // Redibujar los grids
            drawGrid(gPlayer);
            drawGrid(gEnemy);
            drawPlacedShips();
            drawEnemyFleet();
            redrawEnemyBoard();

            // Configurar handlers
            if (game.getCurrentState() == Game.GameState.PLAYING) {
                enemyCanvas.setOnMouseClicked(this::onPlayerShot);

                updateStatusLabel(game.isHumanTurn() ?
                        "¡Tu turno! Click en el tablero enemigo" :
                        "Turno de la máquina");
                turnLabel.setText("TURNO: " + (game.isHumanTurn() ? "Jugador" : "Máquina"));

                // Si es turno de la máquina, programar su disparo
                if (game.isMachineTurn()) {
                    scheduleAITurn();
                }
            }

            System.out.println("✅ Partida continuada desde memoria");
        } else {
            // Iniciar nuevo juego normalmente
            game = new Game();
            game.generateFleet();

            currentShipSize = fleet[0];

            boardEnemy = game.getMachineBoard();

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
            drawEnemyFleet();

            System.out.println("🆕 Nueva partida iniciada");
        }
    }

    private void rotate(KeyEvent ke){
        if (ke.getCode() == KeyCode.R || ke.getCode() == KeyCode.SPACE) {
            vertical = !vertical;
            drawPlacedShips();
        }
    }

    
    // =========== PLAYER GRID ====================

    private void drawGrid(GraphicsContext g) {
        g.setFill(Color.web("TRANSPARENT"));
        g.fillRect(0,0,364,301);

        g.setStroke(Color.rgb(255,255,255,0.3));
        g.setLineWidth(1);

        // Vertical Lines
        for (int i = 0; i <= SIZE; i++) {
            g.strokeLine(i*WIDTH_CELL, 0, i*WIDTH_CELL, 301);
        }

        // Horizontal Lines
        for (int i = 0; i <= SIZE; i++) {
            g.strokeLine(0, i*HEIGHT_CELL, 364, i*HEIGHT_CELL);
        }
    }

    private void previewShip(MouseEvent e) {
        drawPlacedShips();

        if(currentShipSize == 0) return;

        int col = (int)(e.getX() / WIDTH_CELL);
        int row = (int)(e.getY() / HEIGHT_CELL);

        if(!fits(row, col, currentShipSize)) return;

        gPlayer.setFill(Color.rgb(0,255,255,0.35));
        for(int i=0;i<currentShipSize;i++){
            gPlayer.fillRect(
                    col * WIDTH_CELL + (vertical?0:i*WIDTH_CELL),
                    row * HEIGHT_CELL + (vertical?i*HEIGHT_CELL:0),
                    WIDTH_CELL, HEIGHT_CELL
            );
        }
    }

    private void placeShip(MouseEvent e) {
        if (currentShipSize == 0) return;

        int col = (int) (e.getX() / WIDTH_CELL);
        int row = (int) (e.getY() / HEIGHT_CELL);

        if (!fits(row, col, currentShipSize)) return;

/*        for (int i = 0; i < currentShipSize; i++) {
            board.setCell(row + (vertical ? i : 0), col + (vertical ? 0 : i), 1);
        }*/
        IShip.Direction dir = vertical ? IShip.Direction.DOWN : IShip.Direction.RIGHT;
        IShip newShip = createShip(currentShipSize, col, row, dir);
        ships.add(newShip);

        try {
            assert newShip != null;
            game.placeHumanShip(newShip);
            drawPlacedShips();
            advanceToNextShip();
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    private IShip createShip(int size, int col, int row, IShip.Direction dir) {
        return switch (size) {
            case 4 -> new AircraftCarrier(col, row, dir);
            case 3 -> new Submarine(col, row, dir);
            case 2 -> new Destroyer(col, row, dir);
            case 1 -> new Frigate(col, row, dir);
            default -> null;
        };
    }

    private boolean fits(int row,int col,int size){
        Board humanBoard = game.getHumanBoard();

        for(int i=0;i<size;i++){
            int r = row + (vertical?i:0);
            int c = col + (vertical?0:i);

            if(r>=SIZE || c>=SIZE) return false;
            if(humanBoard.getCell(r,c) == 1) return false;
        }
        return true;
    }

    private void drawPlacedShips(){
        gPlayer.clearRect(0, 0, playerCanvas.getWidth(), playerCanvas.getHeight());
        drawGrid(gPlayer);

        for(IShip ship : ships) {
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
                // Get the correct sprite sheet for this ship size
            };

            System.out.println("🎨 Dibujando barco: size=" + size +
                    " dir=" + (direction ? "VERTICAL" : "HORIZONTAL") +
                    " pos=(" + row + "," + col + ")" +
                    " images=" + (images != null ? images.length : "null"));

            // Draw each segment of the ship
            if (images != null) {
                for (int j = 0; j < size; j++) {
                    int drawCol = col + (direction ? 0 : j);
                    int drawRow = row + (direction ? j : 0);

                    System.out.println("  └─ Segmento " + j + " en (" + drawRow + "," + drawCol +
                            ") img=" + (images[j] != null ? "✓" : "✗") +
                            " imgSize=" + (images[j] != null ?
                            images[j].getWidth() + "x" + images[j].getHeight() : "null"));

                    boardRenderer.drawTile(gPlayer, drawCol, drawRow, images[j]);
                    System.out.println("Cuba motilado asqueroso");
                }
            }
        }


        // ========== DIBUJAR MARCADORES DE DISPAROS ==========
        Board humanBoard = game.getHumanBoard();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cell = humanBoard.getCell(row, col);

                // Agua fallada (Miss)
                if (cell == 2) {
                    gPlayer.drawImage(missImage,
                            col * WIDTH_CELL,
                            row * HEIGHT_CELL,
                            WIDTH_CELL, HEIGHT_CELL);
                }
                // Impacto en barco (Hit)
                else if (cell == 3) {
                    gPlayer.drawImage(explosionImage,
                            col * WIDTH_CELL,
                            row * HEIGHT_CELL,
                            WIDTH_CELL, HEIGHT_CELL);
                }
            }
        }

        // ========== DIBUJAR BARCOS HUNDIDOS ==========
        for (IShip ship : ships) {
            if (ship.isSunken()) {
                drawPlayerSunkenShip(ship);
            }
        }
    }

    private void drawPlayerSunkenShip(IShip ship) {
        List<int[]> coords = game.getShipCoordinates(ship);

        for (int[] coord : coords) {
            int row = coord[0];
            int col = coord[1];

            // Dibujar el marcador de barco hundido
            gPlayer.drawImage(
                    hitImage,
                    col * WIDTH_CELL,
                    row * HEIGHT_CELL,
                    WIDTH_CELL,
                    HEIGHT_CELL
            );
        }
    }

    private void advanceToNextShip(){
        shipIndex++;

        if(shipIndex >= fleet.length) {
            currentShipSize = 0;
            playerCanvas.setOnMouseMoved(null);
            playerCanvas.setOnMouseClicked(null);
            System.out.println("🚢 Complete Fleet. Starting Game...");

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> startGame());
            pause.play();
            return;
        }

        // ✅ ACTUALIZAR currentShipSize DESPUÉS del check pero ANTES de usarlo
        currentShipSize = fleet[shipIndex];

        updateStatusLabel("📍 Coloca: Barco de " + currentShipSize + " celdas");
        turnLabel.setText("COLOCANDO: " + (shipIndex + 1) + "/" + fleet.length);

        System.out.println("Coloca el próximo barco (tamaño " + currentShipSize + ")");
    }

    // ================ GAME START ========================

    private void startGame() {
        game.startGame();
        isRunning = true;

        Platform.runLater(() -> {
            updateStatusLabel("¡Juego iniciado! Es tu turno - Click en el tablero enemigo");
            turnLabel.setText("TURNO: Jugador");

            // ⚠️ ESTE ES EL PROBLEMA CRÍTICO
            enemyCanvas.setOnMouseClicked(this::onPlayerShot);

            // 🔍 AGREGAR DEBUGGING:
            System.out.println("✅ Handler registrado en enemyCanvas");
            System.out.println("✅ Game state: " + game.getCurrentState());
            System.out.println("✅ Is human turn? " + game.isHumanTurn());
        });

       // gameExecutor.submit(this::gameLoop);
    }

    private void executeMachineTurn() {
        System.out.println("🤖 === TURNO DE LA MÁQUINA ===");

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

            System.out.println("🤖 Máquina dispara: (" + row + "," + col + ") → " + result);

            Platform.runLater(() -> {
                handleShotResult(result, row, col, false);
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ ERROR en turno máquina: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("🤖 === FIN TURNO MÁQUINA ===\n");
    }
    
    // =============== ENEMY GRID =========================

    private void drawEnemyFleet() {
        enemyShips = game.getMachineFleet();
        drawEnemyShips(enemyShips);
    }

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

            // Coords del barco
            List<int[]> coords = game.getShipCoordinates(ship);

            // 🔥 ORDENAR COORDENADAS 🔥
            coords.sort((a, b) -> {
                if (vertical) {
                    return Integer.compare(a[0], b[0]); // fila
                } else {
                    return Integer.compare(a[1], b[1]); // columna
                }
            });

            // Dibujar las piezas en orden
            for (int i = 0; i < size; i++) {
                int row = coords.get(i)[0];
                int col = coords.get(i)[1];

                gEnemy.drawImage(
                        slices[i],
                        col * WIDTH_CELL,  // ✅ Sin ajuste
                        row * HEIGHT_CELL,  // ✅ Sin ajuste
                        WIDTH_CELL,
                        HEIGHT_CELL
                );
            }
        }
    }

    private void handleBoardClick(Board enemyBoard, int row, int col){
        game.executeHumanPlay(enemyBoard, game.getHuman(), row, col);
    }

    private void onPlayerShot(MouseEvent event) {
        if (!game.isHumanTurn() || game.getCurrentState() != Game.GameState.PLAYING) {
            updateStatusLabel("¡No es tu turno!");
            return;
        }

        int col = (int)(event.getX() / WIDTH_CELL);
        int row = (int)(event.getY() / HEIGHT_CELL);

        // ✅ Ahora usamos directamente row y col (0-9)
        System.out.println("🎯 Disparo en: (" + row + "," + col + ")");

        CompletableFuture.runAsync(() -> {
            try {
                // ✅ Pasamos row, col directamente (0-9)
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

    private void handleShotResult(Game.ShotResult result, int row, int col, boolean isPlayer) {
        System.out.println("📊 Procesando resultado: " + result + " para " + (isPlayer ? "JUGADOR" : "MÁQUINA"));

        if (isPlayer) {
            redrawEnemyBoard();
        } else {
            drawPlacedShips();
        }

        String target = isPlayer ? "enemigo" : "jugador";

        switch (result) {
            case MISS -> {
                updateStatusLabel(isPlayer ?
                        "💦 Agua... Turno de la máquina" :
                        "💦 La máquina falló. ¡Tu turno!");

                game.advanceTurn();

                System.out.println("⏭️ Turno avanzado. Nuevo turno: " +
                        (game.isHumanTurn() ? "JUGADOR" : "MÁQUINA"));

                if (isPlayer) {
                    turnLabel.setText("TURNO: Máquina");
                    // ✅ ACTIVAR TURNO DE LA MÁQUINA
                    scheduleAITurn();
                } else {
                    turnLabel.setText("TURNO: Jugador");
                }
            }
            case HIT -> {
                updateStatusLabel(isPlayer ?
                        "💥 ¡Impacto! Dispara de nuevo" :
                        "💥 ¡La máquina te dio! Ella dispara de nuevo");

                System.out.println("🔄 Mismo turno continúa");

                // ✅ Si la máquina acertó, debe seguir disparando
                if (!isPlayer) {
                    scheduleAITurn();
                }
            }
            case SUNK -> {
                updateStatusLabel(isPlayer ?
                        "🔥 ¡Hundiste un barco! Dispara de nuevo" :
                        "🔥 ¡La máquina hundió tu barco! Ella sigue");

                System.out.println("🔄 Mismo turno continúa (barco hundido)");

                // ✅ Si la máquina hundió un barco, debe seguir disparando
                if (!isPlayer) {
                    scheduleAITurn();
                }
            }
            case ALREADY_SHOT -> {
                updateStatusLabel("Ya disparaste ahí");
                System.out.println("⚠️ Disparo repetido");
            }
        }

        if (game.isGameOver()) {
            System.out.println("🏁 JUEGO TERMINADO");
            endGame();
        }
    }

    private void scheduleAITurn() {
        System.out.println("🤖 Programando turno de la máquina...");

        // Deshabilitar clicks del jugador temporalmente
        enemyCanvas.setOnMouseClicked(null);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000); // Pausa dramática

                if (!game.isMachineTurn()) {
                    System.out.println("❌ Ya no es turno de la máquina");
                    Platform.runLater(() -> {
                        enemyCanvas.setOnMouseClicked(this::onPlayerShot);
                    });
                    return;
                }

                executeMachineTurn();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // ✅ REACTIVAR CLICKS SI ES TURNO DEL JUGADOR
                Platform.runLater(() -> {
                    if (game.isHumanTurn() && game.getCurrentState() == Game.GameState.PLAYING) {
                        System.out.println("✅ Reactivando clicks del jugador");
                        enemyCanvas.setOnMouseClicked(this::onPlayerShot);
                    }
                });
            }
        }, aiExecutor);
    }

    private void endGame() {
        isRunning = false;

        Platform.runLater(() -> {
            String message = game.hasHumanWon() ?
                    "¡VICTORIA! Hundiste toda la flota enemiga" :
                    "DERROTA. La máquina hundió toda tu flota";

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin del juego");
            alert.setHeaderText(message);
            alert.setContentText("¿Quieres volver al menú principal?");
            alert.showAndWait();

            onBackMenu();
        });
    }

    private void redrawEnemyBoard() {
        // Limpia el canvas
        gEnemy.clearRect(0, 0, enemyCanvas.getWidth(), enemyCanvas.getHeight());

        // Redibuja la cuadrícula
        drawGrid(gEnemy);

        // ⭐ SOLO dibuja los barcos si showEnemyShips es true
        if (showEnemyShips) {
            // Dibuja solo los barcos que NO están hundidos
            List<IShip> enemyFleet = game.getMachineFleet();
            List<IShip> activeShips = enemyFleet.stream()
                    .filter(ship -> !ship.isSunken())
                    .toList();

            drawEnemyShips(activeShips);
        }

        // Dibuja todos los marcadores de disparos (SIEMPRE)
        drawAllShotMarkers();
    }

    private void drawAllShotMarkers() {
        // ✅ Ahora iteramos de 0 a 9
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cell = boardEnemy.getCell(row, col);

                // Agua fallada
                if (cell == 2) {
                    gEnemy.drawImage(missImage,
                            col * WIDTH_CELL,  // ✅ Sin ajuste
                            row * HEIGHT_CELL,  // ✅ Sin ajuste
                            WIDTH_CELL, HEIGHT_CELL);
                }
                // Impacto en barco
                else if (cell == 3) {
                    gEnemy.drawImage(explosionImage,
                            col * WIDTH_CELL,  // ✅ Sin ajuste
                            row * HEIGHT_CELL,  // ✅ Sin ajuste
                            WIDTH_CELL, HEIGHT_CELL);
                }
            }
        }

        // Dibuja escombros de barcos hundidos
        List<IShip> enemyFleet = game.getMachineFleet();
        for (IShip ship : enemyFleet) {
            if (ship.isSunken()) {
                drawSunkenShip(ship);
            }
        }
    }

    private void drawSunkenShip(IShip ship) {
        List<int[]> coords = game.getShipCoordinates(ship);

        for (int[] coord : coords) {
            int row = coord[0];
            int col = coord[1];

            gEnemy.drawImage(
                    hitImage,
                    col * WIDTH_CELL,  // ✅ Sin ajuste
                    row * HEIGHT_CELL,  // ✅ Sin ajuste
                    WIDTH_CELL,
                    HEIGHT_CELL
            );
        }
    }

    private void updateStatusLabel(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    // ============= MEDIA MANAGEMENT ===================

    private void setupBackgroundVideo() {
        Platform.runLater(() -> {
            try {
                System.out.println("1. Starting video");

                videoContainer.getChildren().removeIf(node -> node instanceof MediaView);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }

                String videoPath = getClass().getResource("/Battleship-Videos/Game.mp4").toExternalForm();
                System.out.println("2. Video path: " + videoPath);

                Media media = new Media(videoPath);
                mediaPlayer = new MediaPlayer(media);

                System.out.println("3. MediaPlayer created");

                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
                mediaView.setPreserveRatio(false);
                mediaView.setMouseTransparent(true);

                videoContainer.getChildren().add(0, mediaView);
                System.out.println("4. MediaView added. childs in videoContainer: " + videoContainer.getChildren().size());

                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("ERROR in mediaPlayer: " + mediaPlayer.getError());
                    // Intentar recargar
                    mediaPlayer.dispose();
                    setupBackgroundVideo();
                });

                mediaPlayer.setOnReady(() -> {
                    System.out.println("5. Video Ready");
                    mediaPlayer.play();
                    System.out.println("6. Running video");

                    if (stage != null && !stage.isShowing()) {
                        stage.show();
                    }
                });

                mediaPlayer.setVolume(0.3);

            } catch (Exception e) {
                System.err.println("EXCEPCIÓN en setupBackgroundVideo: " + e.getMessage());
                e.printStackTrace();
                videoContainer.setStyle("-fx-background-color: #001a33;");
                if (stage != null) {
                    stage.show();
                }
            }
        });
    }


    public void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

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

    private void createExplosion(double x, double y) {
        if (videoContainer == null) {
            System.out.println("menuPane2 == null se cancela la explosuion");
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

    private void loadMainMenuView() {
        try {

            isRunning = false;

            if (gameExecutor != null) {gameExecutor.shutdownNow();}
            if (aiExecutor != null) {aiExecutor.shutdownNow();}

            stopVideo();

            Stage currentStage = (Stage) GoBackButton.getScene().getWindow();

            GameView.deleteInstance(); // Limpia la instancia actual de Game
            MainMenuView.deleteInstance(); // Limpia cualquier instancia previa del MainMenu

            MainMenuView mainMenuView = MainMenuView.getInstance(); // Crea nueva instancia del MainMenu

            currentStage.close(); // Cierra la ventana del juego

        } catch (Exception e) {
            System.err.println("Error loading MainMenu view: " + e.getMessage());
            e.printStackTrace();
        }
    }
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
        System.out.println("Go back to main menu...");
        loadMainMenuView();
    }



    @FXML
    private void showHelp() {
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
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION,Title,Header,rules,"my-info-alert");
        alert.showAndWait();

    }

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
            System.err.println("⚠️ CSS not found at: " + "/Styles.css");
        }
        dialogPane.getStyleClass().add(styleClass);

        return alert;
    }

    @FXML
    private void toggleEnemyShips() {
        showEnemyShips = !showEnemyShips;
        redrawEnemyBoard();

        if (showEnemyShips) {
            toggleShipsButton.setText("Ocultar Barcos");
            System.out.println("👁️ Mostrando barcos enemigos");
        } else {
            toggleShipsButton.setText("Mostrar Barcos");
            System.out.println("🙈 Ocultando barcos enemigos");
        }
    }

    public static class BoardRenderer {

        private final int WIDTH_CELL = 364/10;
        private final int HEIGHT_CELL = 301/10;

        public void drawTile(GraphicsContext g, int x, int y, Image img){
            g.drawImage(img, x * WIDTH_CELL, y * HEIGHT_CELL, WIDTH_CELL, HEIGHT_CELL);
        }

        public void drawSplash(GraphicsContext g, int x, int y){
            g.setStroke(Color.CYAN);
            g.strokeLine(x* WIDTH_CELL, y* HEIGHT_CELL, (x+1)*WIDTH_CELL, (y+1)*HEIGHT_CELL);
            g.strokeLine((x+1)*WIDTH_CELL, y*HEIGHT_CELL, x*WIDTH_CELL, (y+1)*HEIGHT_CELL);
        }

        public void drawHit(GraphicsContext g, int x, int y){
            g.setFill(Color.rgb(255,0,0,0.6));
            g.fillOval(x*WIDTH_CELL+5, y*HEIGHT_CELL+5, WIDTH_CELL-10, HEIGHT_CELL-10);
        }

        public void drawSunk(GraphicsContext g, int x, int y){
            g.setFill(Color.rgb(150,0,0,0.9));
            g.fillRect(x*WIDTH_CELL, y*HEIGHT_CELL, WIDTH_CELL, HEIGHT_CELL);
        }
    }

}