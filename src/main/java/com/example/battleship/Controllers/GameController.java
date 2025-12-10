package com.example.battleship.Controllers;

import com.example.battleship.Model.Board.Board;
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
    private Stack<IShip> ships = new Stack<>();

    // ================= FLEET ORDER =================
    private int[] fleet = {4,3,3,2,2,2,1,1,1,1};
    private int shipIndex = 0;

    private SpriteSheet carrierSheet = new SpriteSheet(
            getClass().getResource("/sprites/carrier.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private SpriteSheet battleshipSheet = new SpriteSheet(
            getClass().getResource("/sprites/battleship.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private SpriteSheet submarineSheet = new SpriteSheet(
            getClass().getResource("/sprites/submarine.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );

    private SpriteSheet destroyerSheet = new SpriteSheet(
            getClass().getResource("/sprites/destroyer.png").toExternalForm(),
            WIDTH_CELL, HEIGHT_CELL
    );


    public void setStage(Stage stage) {
        this.stage = stage;
    }


    private void setupBackgroundVideo() {
        try {
            // Limpiar primero, sin tomar en cuenta botones
            videoContainer.getChildren().removeIf(node -> node instanceof MediaView);

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            String videoPath = getClass().getResource("/Battleship-Videos/Game.mp4").toExternalForm();
            Media media = new Media(videoPath);
            mediaPlayer = new MediaPlayer(media);

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
            mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
            mediaView.setPreserveRatio(false);


            mediaView.setMouseTransparent(true);


            videoContainer.getChildren().add(0, mediaView);

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });

            mediaPlayer.setOnReady(() -> {
                System.out.println(" Video Running Correctly"); //mensajes por consola quiero saber que lo que pasa
                mediaPlayer.play();
            });

            mediaPlayer.setVolume(0.3);

        } catch (Exception e) {
            System.err.println(" Error while trying playing the video: " + e.getMessage()); //mensajes por consola quiero saber que lo que pasa
            e.printStackTrace();
            videoContainer.setStyle("-fx-background-color: #001a33;");
        }
    }


    public void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gPlayer = playerCanvas.getGraphicsContext2D();
        gEnemy = enemyCanvas.getGraphicsContext2D();
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
        if(currentShipSize == 0) return;

        int col = (int)(e.getX() / WIDTH_CELL);
        int row = (int)(e.getY() / HEIGHT_CELL);

        if(!fits(row,col,currentShipSize)) return;

        for(int i=0;i<currentShipSize;i++){
            board.setCell(row + (vertical?i:0),col + (vertical?0:i), 1);
        }

        switch (currentShipSize){
            case 1:
                ships.add(new Frigate(col, row, vertical? IShip.Direction.DOWN: IShip.Direction.RIGHT));
                break;
            case 2:
                ships.add(new Submarine(col, row, vertical? IShip.Direction.DOWN: IShip.Direction.RIGHT));
                break;
            case 3:
                ships.add(new Destroyer(col, row, vertical? IShip.Direction.DOWN: IShip.Direction.RIGHT));
                break;
            case 4:
                ships.add(new AircraftCarrier(col, row, vertical? IShip.Direction.DOWN: IShip.Direction.RIGHT));
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

        for(int i = 0; i < ships.size(); i++) {
            IShip ship = ships.get(i);
            int col = ship.getCol();
            int row = ship.getRow();
            int size = ships.size();
            IShip.Direction direction = ship.getDirection();


        }

/*        for(int r=0;r<SIZE;r++){
            for(int c=0;c<SIZE;c++){
                if(board.getCell(r,c) == 1){
                    gPlayer.fillRect(c*WIDTH_CELL,r*HEIGHT_CELL,WIDTH_CELL,HEIGHT_CELL);
                }
            }
        }*/
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

    public class BoardRenderer {

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

}