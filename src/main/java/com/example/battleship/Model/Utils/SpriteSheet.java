package com.example.battleship.Model.Utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class SpriteSheet {

    private final Image sheet;
    private final int tileVerticalSize;
    private final int tileHorizontalSize;

    public SpriteSheet(String path, int tileVerticalSize, int tileHorizontalSize) {
        this.sheet = new Image(path);
        this.tileVerticalSize = tileVerticalSize;
        this.tileHorizontalSize = tileHorizontalSize;
    }

    public WritableImage[] getSlices(int parts, boolean vertical) {
        WritableImage[] slices = new WritableImage[parts];
        PixelReader reader = sheet.getPixelReader();

        for (int i = 0; i < parts; i++) {
            slices[i] = new WritableImage(
                    reader,
                    vertical ? 0 : i * tileHorizontalSize,   // si es horizontal, nos movemos a lo ancho
                    vertical ? i * tileVerticalSize : 0,   // si es vertical, recortamos hacia abajo
                    tileHorizontalSize,
                    tileVerticalSize
            );
        }

        return slices;
    }
}