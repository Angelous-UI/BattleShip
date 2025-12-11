package com.example.battleship.Model.AI;

import java.util.*;

/**
 * Inteligencia Artificial mejorada para el juego Battleship.
 *
 * Estrategias implementadas:
 * 1. HUNT MODE: Busca barcos usando patrón de tablero de ajedrez
 * 2. TARGET MODE: Persigue barcos cuando encuentra uno
 * 3. Prioriza direcciones inteligentemente
 */
public class SmartAI {

    private enum Mode {
        HUNT,    // Buscando barcos
        TARGET   // Persiguiendo un barco encontrado
    }

    private Mode currentMode = Mode.HUNT;

    // Pila de coordenadas a atacar (cuando está en TARGET mode)
    private final Stack<int[]> targetStack = new Stack<>();

    // Último impacto registrado
    private int[] lastHit = null;

    // Primera celda del barco actual que está siendo perseguido
    private int[] firstHit = null;

    // Dirección en la que se está persiguiendo
    private Direction currentDirection = null;

    // Historial de disparos
    private final Set<String> shotHistory = new HashSet<>();

    // Tablero conocido (0=desconocido, 1=agua, 2=impacto, 3=hundido)
    private final int[][] knownBoard = new int[10][10];

    private enum Direction {
        NORTH, SOUTH, EAST, WEST;

        int[] getDelta() {
            return switch(this) {
                case NORTH -> new int[]{-1, 0};
                case SOUTH -> new int[]{1, 0};
                case EAST -> new int[]{0, 1};
                case WEST -> new int[]{0, -1};
            };
        }

        Direction opposite() {
            return switch(this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case EAST -> WEST;
                case WEST -> EAST;
            };
        }
    }

    /**
     * Obtiene el próximo disparo inteligente.
     * @return coordenadas [row, col] del próximo disparo
     */
    public int[] getNextShot() {
        int[] shot;

        if (currentMode == Mode.TARGET && !targetStack.isEmpty()) {
            shot = getTargetModeShot();
        } else {
            shot = getHuntModeShot();
            currentMode = Mode.HUNT;
        }

        shotHistory.add(shot[0] + "," + shot[1]);
        return shot;
    }

    /**
     * Registra el resultado de un disparo para ajustar la estrategia.
     * @param row fila del disparo
     * @param col columna del disparo
     * @param hit true si fue impacto
     * @param sunk true si hundió el barco
     */
    public void registerResult(int row, int col, boolean hit, boolean sunk) {
        if (!hit) {
            // Miss - marcar como agua
            knownBoard[row][col] = 1;
            return;
        }

        // Hit - marcar como impacto
        knownBoard[row][col] = 2;
        lastHit = new int[]{row, col};

        if (sunk) {
            // Barco hundido - resetear modo
            markShipAsSunk();
            currentMode = Mode.HUNT;
            targetStack.clear();
            firstHit = null;
            currentDirection = null;
            lastHit = null;
        } else {
            // Impacto pero no hundido - cambiar a TARGET mode
            currentMode = Mode.TARGET;

            if (firstHit == null) {
                // Primer impacto en este barco
                firstHit = new int[]{row, col};
                addAdjacentCells(row, col);
            } else {
                // Segundo o más impactos - determinar dirección
                if (currentDirection == null) {
                    currentDirection = determineDirection(firstHit, lastHit);
                }

                // Agregar siguiente celda en la dirección actual
                addNextInDirection(row, col, currentDirection);

                // También intentar en dirección opuesta desde el primer hit
                addNextInDirection(firstHit[0], firstHit[1], currentDirection.opposite());
            }
        }
    }

    /**
     * Obtiene un disparo en modo HUNT (búsqueda).
     * Usa patrón de tablero de ajedrez para mayor eficiencia.
     */
    private int[] getHuntModeShot() {
        // Patrón de tablero de ajedrez (solo celdas donde puede empezar un barco)
        List<int[]> checkerboardCells = new ArrayList<>();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                // Solo celdas donde (row + col) es par
                if ((row + col) % 2 == 0 && !hasBeenShot(row, col)) {
                    checkerboardCells.add(new int[]{row, col});
                }
            }
        }

        // Si ya disparamos a todas las celdas del patrón, usar cualquier celda
        if (checkerboardCells.isEmpty()) {
            return getRandomAvailableCell();
        }

        // Elegir aleatoriamente del patrón
        return checkerboardCells.get(new Random().nextInt(checkerboardCells.size()));
    }

    /**
     * Obtiene un disparo en modo TARGET (persecución).
     */
    private int[] getTargetModeShot() {
        while (!targetStack.isEmpty()) {
            int[] target = targetStack.pop();

            if (!hasBeenShot(target[0], target[1])) {
                return target;
            }
        }

        // Si no hay más targets, volver a HUNT
        currentMode = Mode.HUNT;
        return getHuntModeShot();
    }

    /**
     * Agrega las 4 celdas adyacentes a la pila de targets.
     */
    private void addAdjacentCells(int row, int col) {
        for (Direction dir : Direction.values()) {
            int[] delta = dir.getDelta();
            int newRow = row + delta[0];
            int newCol = col + delta[1];

            if (isValidCell(newRow, newCol) && !hasBeenShot(newRow, newCol)) {
                targetStack.push(new int[]{newRow, newCol});
            }
        }
    }

    /**
     * Agrega la siguiente celda en una dirección específica.
     */
    private void addNextInDirection(int row, int col, Direction dir) {
        int[] delta = dir.getDelta();
        int newRow = row + delta[0];
        int newCol = col + delta[1];

        if (isValidCell(newRow, newCol) && !hasBeenShot(newRow, newCol)) {
            // Agregar al tope de la pila (prioridad alta)
            targetStack.push(new int[]{newRow, newCol});
        }
    }

    /**
     * Determina la dirección entre dos impactos.
     */
    private Direction determineDirection(int[] first, int[] second) {
        int rowDiff = second[0] - first[0];
        int colDiff = second[1] - first[1];

        if (rowDiff < 0) return Direction.NORTH;
        if (rowDiff > 0) return Direction.SOUTH;
        if (colDiff > 0) return Direction.EAST;
        if (colDiff < 0) return Direction.WEST;

        return null;
    }

    /**
     * Marca todas las celdas del barco hundido.
     */
    private void markShipAsSunk() {
        // Marcar todas las celdas con impacto como hundidas
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (knownBoard[row][col] == 2) {
                    knownBoard[row][col] = 3; // Hundido
                }
            }
        }
    }

    /**
     * Obtiene una celda aleatoria disponible.
     */
    private int[] getRandomAvailableCell() {
        List<int[]> available = new ArrayList<>();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (!hasBeenShot(row, col)) {
                    available.add(new int[]{row, col});
                }
            }
        }

        if (available.isEmpty()) {
            // Esto no debería pasar en un juego normal
            return new int[]{0, 0};
        }

        return available.get(new Random().nextInt(available.size()));
    }

    /**
     * Verifica si ya se disparó a una celda.
     */
    private boolean hasBeenShot(int row, int col) {
        return shotHistory.contains(row + "," + col);
    }

    /**
     * Verifica si una celda es válida (dentro del tablero).
     */
    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < 10 && col >= 0 && col < 10;
    }

    /**
     * Reinicia la IA para un nuevo juego.
     */
    public void reset() {
        currentMode = Mode.HUNT;
        targetStack.clear();
        lastHit = null;
        firstHit = null;
        currentDirection = null;
        shotHistory.clear();

        for (int i = 0; i < 10; i++) {
            Arrays.fill(knownBoard[i], 0);
        }
    }

    /**
     * Obtiene información de debug del estado actual de la IA.
     */
    public String getDebugInfo() {
        return String.format(
                "Mode: %s | Targets in stack: %d | Last hit: %s | Direction: %s",
                currentMode,
                targetStack.size(),
                lastHit != null ? Arrays.toString(lastHit) : "null",
                currentDirection
        );
    }
}
