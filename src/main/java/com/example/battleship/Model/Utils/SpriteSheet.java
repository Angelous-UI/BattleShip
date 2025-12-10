package com.example.battleship.Model.Utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Utility class to slice a sprite sheet image into smaller sprite segments and
 * optionally rotate them for vertical orientation.
 *
 * The class expects an Image resource located by the provided path and uses a
 * simple slicing strategy: the full width is divided by the number of parts to
 * obtain each segment's width.
 */
public class SpriteSheet {

    private final Image sheet;
    private final int cellWidth;
    private final int cellHeight;

    /**
     * Creates a SpriteSheet wrapper for the image at the provided path.
     *
     * @param path      image path resource accepted by JavaFX Image
     * @param cellWidth suggested cell width (not used by current slicing logic but kept for future use)
     * @param cellHeight suggested cell height (not used by current slicing logic but kept for future use)
     */
    public SpriteSheet(String path, int cellWidth, int cellHeight) {
        this.sheet = new Image(path);
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    /**
     * Cuts the ship image into individual segments. If the ship is vertical, each
     * extracted segment is rotated 90 degrees clockwise so the returned images
     * match the vertical orientation.
     *
     * @param parts    number of segments (ship size)
     * @param vertical true for vertical ships, false for horizontal ships
     * @return an array of WritableImage objects, one per ship segment
     */
    public WritableImage[] getSlices(int parts, boolean vertical) {
        WritableImage[] slices = new WritableImage[parts];
        PixelReader reader = sheet.getPixelReader();

        // Obtener dimensiones de la imagen original
        int imgWidth = (int) sheet.getWidth();
        int imgHeight = (int) sheet.getHeight();

        System.out.println("📐 Imagen total: " + imgWidth + "x" + imgHeight);
        System.out.println("   Cortando en " + parts + " partes (" +
                (vertical ? "VERTICAL" : "HORIZONTAL") + ")");

        for (int i = 0; i < parts; i++) {
            int x, y, width, height;

            if (vertical) {
                // Barco vertical: cortar la imagen horizontal en segmentos
                // y rotarlos 90 grados

                // Calcular el ancho de cada segmento de la imagen horizontal
                int segmentWidth = imgWidth / parts;

                x = i * segmentWidth;
                y = 0;
                width = segmentWidth;
                height = imgHeight;

                // Extraer el segmento
                WritableImage segment = new WritableImage(reader, x, y, width, height);

                // Rotar el segmento 90 grados
                slices[i] = rotateImage(segment, 90);

            } else {
                // Barco horizontal: cortar directamente en segmentos

                // Calcular el ancho de cada segmento
                int segmentWidth = imgWidth / parts;

                x = i * segmentWidth;
                y = 0;
                width = segmentWidth;
                height = imgHeight;

                slices[i] = new WritableImage(reader, x, y, width, height);
            }

            System.out.println("   └─ Segmento " + i + ": desde (" + x + "," + y + ") " +
                    "tamaño=" + width + "x" + height);
        }

        return slices;
    }

    /**
     * Rotates a WritableImage 90 degrees clockwise.
     *
     * @param source  source image to rotate
     * @param degrees rotation degrees (only 90 is supported by caller use)
     * @return rotated WritableImage
     */
    private WritableImage rotateImage(WritableImage source, int degrees) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();

        // Crear nueva imagen con dimensiones invertidas
        WritableImage rotated = new WritableImage(height, width);

        PixelReader reader = source.getPixelReader();
        var writer = rotated.getPixelWriter();

        // Rotar 90 grados en sentido horario
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Mapeo: (x,y) -> (height-1-y, x)
                writer.setArgb(height - 1 - y, x, reader.getArgb(x, y));
            }
        }

        return rotated;
    }
}