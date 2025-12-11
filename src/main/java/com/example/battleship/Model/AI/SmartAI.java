package com.example.battleship.Model.AI;

import java.util.*;

/**
 * Inteligencia Artificial AVANZADA para Battleship.
 *
 * Estrategias implementadas:
 * 1. HUNT MODE: Usa heatmap de probabilidad basado en barcos restantes
 * 2. TARGET MODE: Persigue inteligentemente con múltiples estrategias
 * 3. Considera tamaño de barcos restantes para optimizar búsqueda
 * 4. Evita disparar a celdas imposibles después de hundir barcos
 */
public class SmartAI {

    private enum Mode {
        HUNT,    // Buscando barcos
        TARGET   // Persiguiendo un barco encontrado
    }

    private Mode currentMode = Mode.HUNT;

    // Cola prioritaria de objetivos (ordenados por prioridad)
    private final PriorityQueue<ScoredCell> targetQueue = new PriorityQueue<>();

    // Celdas que forman parte del barco actual siendo perseguido
    private final List<int[]> currentShipHits = new ArrayList<>();

    // Historial de disparos
    private final Set<String> shotHistory = new HashSet<>();

    // Tablero conocido (0=desconocido, 1=agua, 2=impacto, 3=hundido)
    private final int[][] knownBoard = new int[10][10];

    // Barcos restantes por tamaño [1, 2, 3, 4]
    private final int[] remainingShips = {4, 3, 2, 1}; // Fragatas, Destructores, Submarinos, Portaaviones

    // Mapa de calor (probabilidad de que haya un barco)
    private final int[][] heatMap = new int[10][10];

    private static class ScoredCell implements Comparable<ScoredCell> {
        int row, col, score;

        ScoredCell(int row, int col, int score) {
            this.row = row;
            this.col = col;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredCell other) {
            return Integer.compare(other.score, this.score); // Mayor score = mayor prioridad
        }
    }

    private enum Direction {
        NORTH(-1, 0), SOUTH(1, 0), EAST(0, 1), WEST(0, -1);

        final int dr, dc;

        Direction(int dr, int dc) {
            this.dr = dr;
            this.dc = dc;
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
     */
    public int[] getNextShot() {
        int[] shot;

        if (currentMode == Mode.TARGET) {
            shot = getTargetModeShot();
            if (shot == null) {
                // No hay más targets válidos, volver a HUNT
                currentMode = Mode.HUNT;
                shot = getHuntModeShot();
            }
        } else {
            shot = getHuntModeShot();
        }

        shotHistory.add(shot[0] + "," + shot[1]);
        return shot;
    }

    /**
     * Registra el resultado de un disparo.
     */
    public void registerResult(int row, int col, boolean hit, boolean sunk) {
        if (!hit) {
            knownBoard[row][col] = 1; // Agua
            return;
        }

        // Impacto
        knownBoard[row][col] = 2;
        currentShipHits.add(new int[]{row, col});

        if (sunk) {
            handleSunkShip();
        } else {
            // Cambiar a TARGET mode y generar objetivos inteligentes
            currentMode = Mode.TARGET;
            generateSmartTargets();
        }
    }

    /**
     * Maneja cuando se hunde un barco.
     */
    private void handleSunkShip() {
        // Determinar tamaño del barco hundido
        int shipSize = currentShipHits.size();

        // Marcar como hundido y actualizar barcos restantes
        for (int[] hit : currentShipHits) {
            knownBoard[hit[0]][hit[1]] = 3;
        }

        // Reducir contador de barcos restantes
        if (shipSize >= 1 && shipSize <= 4) {
            remainingShips[shipSize - 1]--;
            System.out.println("🤖 [IA] Hundí un barco de tamaño " + shipSize +
                    ". Restantes: " + Arrays.toString(remainingShips));
        }

        // Marcar celdas adyacentes al barco hundido como imposibles
        markAdjacentAsImpossible();

        // Reset
        currentShipHits.clear();
        targetQueue.clear();
        currentMode = Mode.HUNT;
    }

    /**
     * Marca las celdas adyacentes a un barco hundido como agua (no puede haber barcos pegados).
     */
    private void markAdjacentAsImpossible() {
        Set<String> toMark = new HashSet<>();

        for (int[] hit : currentShipHits) {
            // Marcar todas las 8 direcciones (incluidas diagonales)
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;

                    int newRow = hit[0] + dr;
                    int newCol = hit[1] + dc;

                    if (isValidCell(newRow, newCol) && knownBoard[newRow][newCol] == 0) {
                        toMark.add(newRow + "," + newCol);
                    }
                }
            }
        }

        // Marcar como agua
        for (String cell : toMark) {
            String[] parts = cell.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            knownBoard[r][c] = 1;
        }
    }

    /**
     * Genera objetivos inteligentes basados en los impactos actuales.
     */
    private void generateSmartTargets() {
        targetQueue.clear();

        if (currentShipHits.size() == 1) {
            // Solo un impacto - agregar las 4 direcciones con alta prioridad
            int[] hit = currentShipHits.get(0);

            for (Direction dir : Direction.values()) {
                int newRow = hit[0] + dir.dr;
                int newCol = hit[1] + dir.dc;

                if (isValidCell(newRow, newCol) && !hasBeenShot(newRow, newCol)) {
                    targetQueue.add(new ScoredCell(newRow, newCol, 100));
                }
            }
        } else {
            // Múltiples impactos - determinar orientación y priorizar extremos
            Direction orientation = determineOrientation();

            if (orientation != null) {
                // Encontrar los extremos de la línea de impactos
                int[] firstHit = findExtreme(orientation);
                int[] lastHit = findExtreme(orientation.opposite());

                // Priorizar celdas en los extremos
                addExtremeCells(firstHit, orientation.opposite(), 200);
                addExtremeCells(lastHit, orientation, 200);
            }
        }
    }

    /**
     * Determina la orientación del barco basándose en los impactos.
     */
    private Direction determineOrientation() {
        if (currentShipHits.size() < 2) return null;

        int[] first = currentShipHits.get(0);
        int[] second = currentShipHits.get(1);

        if (first[0] == second[0]) {
            // Misma fila = horizontal
            return first[1] < second[1] ? Direction.EAST : Direction.WEST;
        } else if (first[1] == second[1]) {
            // Misma columna = vertical
            return first[0] < second[0] ? Direction.SOUTH : Direction.NORTH;
        }

        return null;
    }

    /**
     * Encuentra el extremo de los impactos en una dirección.
     */
    private int[] findExtreme(Direction dir) {
        int[] extreme = currentShipHits.get(0);

        for (int[] hit : currentShipHits) {
            if (dir == Direction.NORTH && hit[0] < extreme[0]) extreme = hit;
            else if (dir == Direction.SOUTH && hit[0] > extreme[0]) extreme = hit;
            else if (dir == Direction.WEST && hit[1] < extreme[1]) extreme = hit;
            else if (dir == Direction.EAST && hit[1] > extreme[1]) extreme = hit;
        }

        return extreme;
    }

    /**
     * Agrega celdas en un extremo con alta prioridad.
     */
    private void addExtremeCells(int[] extreme, Direction dir, int score) {
        int newRow = extreme[0] + dir.dr;
        int newCol = extreme[1] + dir.dc;

        if (isValidCell(newRow, newCol) && !hasBeenShot(newRow, newCol)) {
            targetQueue.add(new ScoredCell(newRow, newCol, score));
        }
    }

    /**
     * Obtiene un disparo en modo TARGET.
     */
    private int[] getTargetModeShot() {
        while (!targetQueue.isEmpty()) {
            ScoredCell cell = targetQueue.poll();

            if (!hasBeenShot(cell.row, cell.col) && knownBoard[cell.row][cell.col] == 0) {
                return new int[]{cell.row, cell.col};
            }
        }

        return null;
    }

    /**
     * Obtiene un disparo en modo HUNT usando mapa de calor.
     */
    private int[] getHuntModeShot() {
        updateHeatMap();

        // Encontrar la celda con mayor probabilidad
        int maxHeat = -1;
        List<int[]> bestCells = new ArrayList<>();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (hasBeenShot(row, col) || knownBoard[row][col] != 0) continue;

                int heat = heatMap[row][col];

                if (heat > maxHeat) {
                    maxHeat = heat;
                    bestCells.clear();
                    bestCells.add(new int[]{row, col});
                } else if (heat == maxHeat) {
                    bestCells.add(new int[]{row, col});
                }
            }
        }

        if (bestCells.isEmpty()) {
            return getRandomAvailableCell();
        }

        // Elegir aleatoriamente entre las mejores
        return bestCells.get(new Random().nextInt(bestCells.size()));
    }

    /**
     * Actualiza el mapa de calor basándose en los barcos restantes.
     */
    private void updateHeatMap() {
        // Resetear
        for (int i = 0; i < 10; i++) {
            Arrays.fill(heatMap[i], 0);
        }

        // Para cada tamaño de barco restante
        for (int size = 1; size <= 4; size++) {
            if (remainingShips[size - 1] <= 0) continue;

            int count = remainingShips[size - 1];

            // Probar todas las posiciones posibles
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    // Horizontal
                    if (canPlaceShip(row, col, size, true)) {
                        for (int i = 0; i < size; i++) {
                            heatMap[row][col + i] += count;
                        }
                    }

                    // Vertical
                    if (canPlaceShip(row, col, size, false)) {
                        for (int i = 0; i < size; i++) {
                            heatMap[row + i][col] += count;
                        }
                    }
                }
            }
        }
    }

    /**
     * Verifica si un barco puede colocarse en una posición.
     */
    private boolean canPlaceShip(int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;

            if (!isValidCell(r, c)) return false;
            if (knownBoard[r][c] == 1 || knownBoard[r][c] == 3) return false; // Agua o hundido
        }

        return true;
    }

    /**
     * Obtiene una celda aleatoria disponible.
     */
    private int[] getRandomAvailableCell() {
        List<int[]> available = new ArrayList<>();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (!hasBeenShot(row, col) && knownBoard[row][col] == 0) {
                    available.add(new int[]{row, col});
                }
            }
        }

        return available.isEmpty() ? new int[]{0, 0} :
                available.get(new Random().nextInt(available.size()));
    }

    private boolean hasBeenShot(int row, int col) {
        return shotHistory.contains(row + "," + col);
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < 10 && col >= 0 && col < 10;
    }

    /**
     * Reinicia la IA.
     */
    public void reset() {
        currentMode = Mode.HUNT;
        targetQueue.clear();
        currentShipHits.clear();
        shotHistory.clear();

        // Resetear barcos: 4 fragatas, 3 destructores, 2 submarinos, 1 portaaviones
        remainingShips[0] = 4;
        remainingShips[1] = 3;
        remainingShips[2] = 2;
        remainingShips[3] = 1;

        for (int i = 0; i < 10; i++) {
            Arrays.fill(knownBoard[i], 0);
            Arrays.fill(heatMap[i], 0);
        }
    }

    public String getDebugInfo() {
        return String.format(
                "Mode: %s | Queue size: %d | Current hits: %d | Ships: %s",
                currentMode,
                targetQueue.size(),
                currentShipHits.size(),
                Arrays.toString(remainingShips)
        );
    }
}