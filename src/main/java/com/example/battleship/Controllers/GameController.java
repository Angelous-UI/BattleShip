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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Alert;


import java.net.URL;
import java.util.*;

import static com.example.battleship.Model.Ship.IShip.Direction.DOWN;
import static com.example.battleship.Model.Ship.IShip.Direction.RIGHT;

public class GameController implements Initializable {

    @FXML
    private AnchorPane videoContainer;

    @FXML
    private Button GoBackButton;

    @FXML private Canvas playerCanvas;
    @FXML private Canvas enemyCanvas;

    private GraphicsContext gPlayer;
    private GraphicsContext gEnemy;

    private MediaPlayer mediaPlayer;
    private Stage stage;

    private final int WIDTH_CELL = 364/10;
    private final int HEIGHT_CELL = 301/10;

    private final int SIZE = 10;

    private int currentShipSize = 4;   // Comenzamos con portaaviones
    private boolean vertical = false;

    private Board board;
    private Board boardEnemy;
    List<IShip> enemyShips;
    List<int[]> coords;
    private BoardRenderer boardRenderer = new BoardRenderer();
    private Stack<IShip> ships = new Stack<>();

    // ================= FLEET ORDER =================
    private int[] fleet = {4,3,3,2,2,2,1,1,1,1};
    private int shipIndex = 0;
    private Game game;

    private SpriteSheet carrierSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/portaaviones.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private SpriteSheet frigateSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/fragata.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private SpriteSheet submarineSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/submarinos.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private SpriteSheet destroyerSheet = new SpriteSheet(
            getClass().getResource("/Battleship-Images/destructores.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );


    public void setStage(Stage stage) {
        this.stage = stage;
    }


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
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        game = new Game();
        game.generateFleet();

        gPlayer = playerCanvas.getGraphicsContext2D();
        gEnemy = enemyCanvas.getGraphicsContext2D();
        boardEnemy = game.getBoard();
        board = new Board();

        setupBackgroundVideo();
        addExplosionEffect(GoBackButton);

        playerCanvas.setOnMouseMoved(this::previewShip);
        playerCanvas.setOnMouseClicked(this::placeShip);
        playerCanvas.setOnMouseExited(e -> drawPlacedShips());

        playerCanvas.setFocusTraversable(true);
        playerCanvas.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.R || ke.getCode() == KeyCode.SPACE) {
                vertical = !vertical;
                drawPlacedShips();
            }
        });

        drawGrid(gPlayer);
        drawGrid(gEnemy);
        enemyCanvas.setOnMouseClicked(this::onEnemyClick);
        drawEnemyFleet();
    }

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

        // Validar si cabe antes de pintar
        if(!fits(row,col,currentShipSize)) return;

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

        for (int i = 0; i < currentShipSize; i++) {
            board.setCell(row + (vertical ? i : 0), col + (vertical ? 0 : i), 1);
        }

        switch (currentShipSize) {
            case 1:
                ships.add(new Frigate(col, row, vertical ? IShip.Direction.DOWN : IShip.Direction.RIGHT));
                break;
            case 2:
                ships.add(new Destroyer(col, row, vertical ? IShip.Direction.DOWN : IShip.Direction.RIGHT));
                break;
            case 3:
                ships.add(new Submarine(col, row, vertical ? IShip.Direction.DOWN : IShip.Direction.RIGHT));
                break;
            case 4:
                ships.add(new AircraftCarrier(col, row, vertical ? IShip.Direction.DOWN : IShip.Direction.RIGHT));
                break;
            default:
                break;
        }

        drawPlacedShips();
        advanceToNextShip();
    }

    private boolean fits(int row,int col,int size){
        for(int i=0;i<size;i++){
            int r = row + (vertical?i:0);
            int c = col + (vertical?0:i);

            if(r>=SIZE || c>=SIZE) return false;       // se sale
            if(board.getCell(r,c) == 1) return false;           // choca con otro
        }
        return true;
    }

    private void drawPlacedShips(){

        gPlayer.clearRect(0, 0, playerCanvas.getWidth(), playerCanvas.getHeight());

        drawGrid(gPlayer);

        gPlayer.setFill(Color.GRAY);

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
                }
            }
        }

/*        for(int r=0;r<SIZE;r++){
            for(int c=0;c<SIZE;c++){
                if(board.getCell(r,c) == 1){
                    gPlayer.fillRect(c*WIDTH_CELL,r*HEIGHT_CELL,WIDTH_CELL,HEIGHT_CELL);
                }
            }
        }*/
    }

    private void drawEnemyFleet() {
        enemyShips = game.getFleet();
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
                        (col - 1) * WIDTH_CELL,
                        (row - 1) * HEIGHT_CELL,
                        WIDTH_CELL,
                        HEIGHT_CELL
                );
            }
        }
    }

    private void advanceToNextShip(){
        shipIndex++;

        if(shipIndex >= fleet.length) {
            currentShipSize = 0; // señal de "sin barcos"
            playerCanvas.setOnMouseMoved(null);
            playerCanvas.setOnMouseClicked(null);
            System.out.println("🚢 Fleet");
            return;
        }

        // actualizar tamaño del próximo barco
        currentShipSize = fleet[shipIndex];
        System.out.println("Coloca el próximo barco (tamaño " + currentShipSize + ")");
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
            MainMenuView mainMenuView = new MainMenuView();

            Stage currentStage = (Stage) GoBackButton.getScene().getWindow();
            currentStage.close();


            mainMenuView.show();

        } catch (Exception e) {
            System.err.println("Error loading MainMenu view: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void onBackMenu() {
        stopVideo();
        System.out.println("Go back to main menu...");
        loadMainMenuView();
    }

    @FXML
    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ayuda - Batalla Naval");
        alert.setHeaderText("📋 Cómo jugar");

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

        alert.setContentText(rules);
        alert.getDialogPane().setMinWidth(550);
        alert.getDialogPane().setMinHeight(400);

        alert.showAndWait();
    }

    public static class BoardRenderer {

        private final int WIDTH_CELL = 364/10;
        private final int HEIGHT_CELL = 301/10;

        public void drawTile(GraphicsContext g, int x, int y, Image img){
            g.drawImage(img, x * WIDTH_CELL, y * HEIGHT_CELL, WIDTH_CELL, HEIGHT_CELL);
        }

        public void drawSplash(GraphicsContext g, int x, int y){
            g.setStroke(javafx.scene.paint.Color.CYAN);
            g.strokeLine(x* WIDTH_CELL, y* HEIGHT_CELL, (x+1)*WIDTH_CELL, (y+1)*HEIGHT_CELL);
            g.strokeLine((x+1)*WIDTH_CELL, y*HEIGHT_CELL, x*WIDTH_CELL, (y+1)*HEIGHT_CELL);
        }

        public void drawHit(GraphicsContext g, int x, int y){
            g.setFill(javafx.scene.paint.Color.rgb(255,0,0,0.6));
            g.fillOval(x*WIDTH_CELL+5, y*HEIGHT_CELL+5, WIDTH_CELL-10, HEIGHT_CELL-10);
        }

        public void drawSunk(GraphicsContext g, int x, int y){
            g.setFill(javafx.scene.paint.Color.rgb(150,0,0,0.9));
            g.fillRect(x*WIDTH_CELL, y*HEIGHT_CELL, WIDTH_CELL, HEIGHT_CELL);
        }
    }

    //puto ignacio
    private void handleBoardClick(Board enemyBoard, ImageView hitView, int row, int col){
        game.executeHumanPlay(enemyBoard, game.getHuman(), row, col);
    }

    private void onEnemyClick(MouseEvent event) {
        int col = (int)(event.getX() / WIDTH_CELL);
        int row = (int)(event.getY() / HEIGHT_CELL);

        int boardRow = row + 1;
        int boardCol = col + 1;

        System.out.println("🎯 Click en enemigo: row=" + boardRow + " col=" + boardCol);

        handleBoardClick(boardEnemy, null, boardRow, boardCol);
    }

}