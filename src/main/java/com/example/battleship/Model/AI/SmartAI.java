package com.example.battleship.Model.AI;

import java.util.*;

/**
 * Advanced Artificial Intelligence for the Battleship game.
 *
 * <p>This AI uses a hybrid strategy composed of two main modes:</p>
 * <ul>
 *   <li><b>HUNT</b>: probabilistic search using a heat map when no active hits exist.</li>
 *   <li><b>TARGET</b>: focused attack mode when one or more ships have been hit.</li>
 * </ul>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Tracks hits belonging to multiple ships simultaneously.</li>
 *   <li>Uses BFS to correctly identify and process sunk ships.</li>
 *   <li>Maintains an internal knowledge board of the enemy grid.</li>
 *   <li>Uses a priority queue to select optimal target cells.</li>
 *   <li>Employs a probability heat map based on remaining ship sizes.</li>
 * </ul>
 *
 * <p>The board is assumed to be 10x10 and uses 0-based indexing.</p>
 *
 * @author Battleship Development Team
 * @version 2.0
 */
public class SmartAI {

    /**
     * Represents the current operational mode of the AI.
     */
    private enum Mode {
        /** General search mode with no active hits */
        HUNT,
        /** Focused attack mode when at least one hit exists */
        TARGET
    }

    /** Current operational mode */
    private Mode currentMode = Mode.HUNT;

    /** Priority queue containing candidate target cells */
    private final PriorityQueue<ScoredCell> targetQueue = new PriorityQueue<>();

    /** List of all active hits (may belong to multiple ships) */
    private final List<int[]> currentShipHits = new ArrayList<>();

    /** History of all fired shots to prevent duplicates */
    private final Set<String> shotHistory = new HashSet<>();

    /**
     * Internal knowledge board.
     * <ul>
     *   <li>0 = unknown</li>
     *   <li>1 = water/miss</li>
     *   <li>2 = hit</li>
     *   <li>3 = sunk</li>
     * </ul>
     */
    private final int[][] knownBoard = new int[10][10];

    /** Remaining ships indexed by size: [0]=frigates(1), [1]=destroyers(2), [2]=submarines(3), [3]=carriers(4) */
    private final int[] remainingShips = {4, 3, 2, 1};

    /** Probability heat map used during HUNT mode */
    private final int[][] heatMap = new int[10][10];

    /**
     * Represents a candidate cell to shoot at along with its priority score.
     *
     * <p>Instances of this class are stored inside a {@link PriorityQueue}
     * so that higher-scored cells are selected first.</p>
     */
    private static class ScoredCell implements Comparable<ScoredCell> {
        /** Row index of the cell */
        int row;

        /** Column index of the cell */
        int col;

        /** Priority score (higher means more desirable) */
        int score;

        /**
         * Constructs a scored cell.
         *
         * @param row row index (0–9)
         * @param col column index (0–9)
         * @param score priority score
         */
        ScoredCell(int row, int col, int score) {
            this.row = row;
            this.col = col;
            this.score = score;
        }

        /**
         * Compares two cells by score in descending order.
         *
         * @param other the cell to compare with
         * @return negative if this cell has lower priority, positive if higher
         */
        @Override
        public int compareTo(ScoredCell other) {
            return Integer.compare(other.score, this.score);
        }
    }

    /**
     * Cardinal directions used for grid exploration.
     */
    private enum Direction {

        /** Upward direction (decreasing row) */
        NORTH(-1, 0),

        /** Downward direction (increasing row) */
        SOUTH(1, 0),

        /** Right direction (increasing column) */
        EAST(0, 1),

        /** Left direction (decreasing column) */
        WEST(0, -1);

        /** Row delta */
        final int dr;

        /** Column delta */
        final int dc;

        /**
         * Constructs a direction with row and column deltas.
         *
         * @param dr row delta
         * @param dc column delta
         */
        Direction(int dr, int dc) {
            this.dr = dr;
            this.dc = dc;
        }

        /**
         * Returns the opposite direction.
         *
         * @return opposite direction
         */
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
     * Determines the next shot to fire based on the current AI state.
     *
     * <p>The method prioritizes:</p>
     * <ol>
     *   <li>Queued high-priority targets</li>
     *   <li>Adjacent exploration around active hits</li>
     *   <li>Nearby fallback exploration</li>
     *   <li>Probabilistic hunt mode using a heat map</li>
     * </ol>
     *
     * @return an integer array {row, col} representing the next shot coordinates
     */
    public int[] getNextShot() {
        System.out.println("\n🎯 [AI] ========== GETTING SHOT ==========");
        System.out.println("   Total active hits: " + currentShipHits.size());
        if (!currentShipHits.isEmpty()) {
            System.out.print("   Positions: ");
            for (int[] hit : currentShipHits) {
                System.out.print("(" + hit[0] + "," + hit[1] + ") ");
            }
            System.out.println();
        }

        int[] shot = null;

        if (!currentShipHits.isEmpty()) {
            System.out.println("   🔥 TARGET MODE ACTIVE");
            currentMode = Mode.TARGET;

            shot = pollValidTarget();

            if (shot == null) {
                System.out.println("   📋 Regenerating targets...");
                generateSmartTargets();
                shot = pollValidTarget();
            }

            if (shot == null) {
                System.out.println("   🔍 Exploring adjacent cells...");
                shot = exploreAllAdjacent();
            }

            if (shot == null) {
                System.out.println("   ♟️ Expanded pattern...");
                shot = findNearbyCell();
            }

            if (shot == null) {
                System.out.println("   ⚠️ Random fallback");
                shot = getRandomAvailableCell();
            }

        } else {
            System.out.println("   🔎 HUNT MODE");
            shot = getHuntModeShot();
        }

        if (shot == null) {
            System.err.println("   ❌ ERROR: extreme fallback");
            shot = new int[]{0, 0};
        }

        System.out.println("   ✅ SHOT: (" + shot[0] + "," + shot[1] + ")");
        System.out.println("===============================================\n");

        shotHistory.add(shot[0] + "," + shot[1]);
        return shot;
    }

    /**
     * Polls the next valid target from the priority queue.
     *
     * <p>This method continuously polls cells from the queue until
     * a valid target is found or the queue is empty.</p>
     *
     * @return coordinates {row, col} of the next valid target, or null if queue is empty
     */
    private int[] pollValidTarget() {
        System.out.println("      🎯 Queue size: " + targetQueue.size());

        while (!targetQueue.isEmpty()) {
            ScoredCell cell = targetQueue.poll();

            if (isValidTarget(cell.row, cell.col)) {
                System.out.println("      ✅ Target: (" + cell.row + "," + cell.col + ")");
                return new int[]{cell.row, cell.col};
            }
        }

        System.out.println("      ❌ No valid targets");
        return null;
    }

    /**
     * Explores all adjacent cells around active hits.
     *
     * <p>Iterates through all current hits and checks the four
     * cardinal directions for valid targets.</p>
     *
     * @return coordinates {row, col} of the first valid adjacent cell, or null if none found
     */
    private int[] exploreAllAdjacent() {
        for (int[] hit : currentShipHits) {
            for (Direction dir : Direction.values()) {
                int newRow = hit[0] + dir.dr;
                int newCol = hit[1] + dir.dc;

                if (isValidTarget(newRow, newCol)) {
                    System.out.println("      ✅ Adjacent: (" + newRow + "," + newCol + ")");
                    return new int[]{newRow, newCol};
                }
            }
        }

        System.out.println("      ❌ No adjacent cells");
        return null;
    }

    /**
     * Finds a valid cell within a 3-cell radius of active hits.
     *
     * <p>Used as a fallback when no adjacent cells are available.</p>
     *
     * @return coordinates {row, col} of a nearby valid cell, or null if none found
     */
    private int[] findNearbyCell() {
        for (int[] hit : currentShipHits) {
            for (int dr = -3; dr <= 3; dr++) {
                for (int dc = -3; dc <= 3; dc++) {
                    int newRow = hit[0] + dr;
                    int newCol = hit[1] + dc;

                    if (isValidTarget(newRow, newCol)) {
                        System.out.println("      ✅ Nearby: (" + newRow + "," + newCol + ")");
                        return new int[]{newRow, newCol};
                    }
                }
            }
        }

        System.out.println("      ❌ No nearby cells");
        return null;
    }

    /**
     * Checks if a cell is a valid target for shooting.
     *
     * <p>A cell is valid if it's within bounds, hasn't been shot,
     * and is unknown (value 0 in knownBoard).</p>
     *
     * @param row row index
     * @param col column index
     * @return true if the cell is a valid target, false otherwise
     */
    private boolean isValidTarget(int row, int col) {
        return isValidCell(row, col) &&
                !hasBeenShot(row, col) &&
                knownBoard[row][col] == 0;
    }

    /**
     * Registers the result of a shot fired by the AI.
     *
     * <p>This method updates the AI's internal state based on whether
     * the shot was a hit or miss, and whether it sunk a ship.</p>
     *
     * @param row row coordinate of the shot
     * @param col column coordinate of the shot
     * @param hit true if the shot hit a ship, false if it was a miss
     * @param sunk true if the shot sunk a ship, false otherwise
     */
    public void registerResult(int row, int col, boolean hit, boolean sunk) {
        System.out.println("\n📊 [AI] ========== RESULT ==========");
        System.out.println("   Pos: (" + row + "," + col + ") | Hit: " + hit + " | Sunk: " + sunk);

        if (!hit) {
            knownBoard[row][col] = 1;
            System.out.println("   💧 WATER - maintaining " + currentShipHits.size() + " hits");
            return;
        }

        // HIT
        knownBoard[row][col] = 2;

        boolean exists = currentShipHits.stream()
                .anyMatch(h -> h[0] == row && h[1] == col);

        if (!exists) {
            currentShipHits.add(new int[]{row, col});
            System.out.println("   💥 NEW HIT added!");
        }

        System.out.println("   📍 Total hits: " + currentShipHits.size());

        if (sunk) {
            System.out.println("   🔥 SHIP SUNK!");
            handleSunkShip(row, col);
        } else {
            System.out.println("   ⚠️ NOT sunk - continuing");
            currentMode = Mode.TARGET;
            targetQueue.clear();
            generateSmartTargets();
        }

        System.out.println("=======================================\n");
    }

    /**
     * Handles the logic when a ship is sunk.
     *
     * <p>Uses BFS to identify all cells belonging to the sunk ship,
     * removes only those hits from the active list, and marks adjacent
     * cells as impossible. If other hits remain, maintains TARGET mode.</p>
     *
     * @param lastHitRow row coordinate of the final hit that sunk the ship
     * @param lastHitCol column coordinate of the final hit that sunk the ship
     */
    private void handleSunkShip(int lastHitRow, int lastHitCol) {
        System.out.println("   🔍 Identifying sunk ship...");

        // Find all connected hits to the last shot (the sunk ship)
        Set<String> sunkShipCells = findConnectedShip(lastHitRow, lastHitCol);

        System.out.println("   🔥 Sunk ship has " + sunkShipCells.size() + " cells:");
        for (String cell : sunkShipCells) {
            System.out.println("      - " + cell);
        }

        // Mark as sunk
        for (String cell : sunkShipCells) {
            String[] parts = cell.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            knownBoard[r][c] = 3;

            // Mark adjacent cells of this ship as impossible
            markAdjacentAsImpossible(r, c);
        }

        // ✅ ONLY remove hits from the sunk ship
        currentShipHits.removeIf(hit -> {
            String key = hit[0] + "," + hit[1];
            return sunkShipCells.contains(key);
        });

        System.out.println("   🧹 Remaining hits after cleanup: " + currentShipHits.size());

        if (!currentShipHits.isEmpty()) {
            System.out.println("   ⚠️ STILL ACTIVE HITS - maintaining TARGET mode");
            System.out.print("   Remaining hits: ");
            for (int[] hit : currentShipHits) {
                System.out.print("(" + hit[0] + "," + hit[1] + ") ");
            }
            System.out.println();
            currentMode = Mode.TARGET;
            targetQueue.clear();
            generateSmartTargets();
        } else {
            System.out.println("   ✅ No more hits - returning to HUNT");
            currentMode = Mode.HUNT;
            targetQueue.clear();
        }

        // Update ship counter
        int shipSize = sunkShipCells.size();
        if (shipSize >= 1 && shipSize <= 4) {
            remainingShips[shipSize - 1]--;
            System.out.println("   📊 Remaining ships: " + Arrays.toString(remainingShips));
        }
    }

    /**
     * Finds all connected hit cells belonging to the same ship using BFS.
     *
     * <p>Starting from the given coordinates, this method explores
     * adjacent cells to identify all parts of the sunk ship.</p>
     *
     * @param startRow starting row coordinate
     * @param startCol starting column coordinate
     * @return set of strings in "row,col" format representing all cells of the sunk ship
     */
    private Set<String> findConnectedShip(int startRow, int startCol) {
        Set<String> visited = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{startRow, startCol});
        visited.add(startRow + "," + startCol);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];

            // Check the 4 adjacent directions
            for (Direction dir : Direction.values()) {
                int newRow = row + dir.dr;
                int newCol = col + dir.dc;
                String key = newRow + "," + newCol;

                // If it's a hit (2) or sunk (3) and we haven't visited it
                if (isValidCell(newRow, newCol) &&
                        !visited.contains(key) &&
                        (knownBoard[newRow][newCol] == 2 || knownBoard[newRow][newCol] == 3)) {

                    visited.add(key);
                    queue.add(new int[]{newRow, newCol});
                }
            }
        }

        return visited;
    }

    /**
     * Marks all cells adjacent to a sunk ship cell as water (impossible targets).
     *
     * <p>This prevents the AI from shooting at cells where ships cannot exist
     * according to Battleship rules (ships cannot be adjacent).</p>
     *
     * @param row row coordinate of the sunk ship cell
     * @param col column coordinate of the sunk ship cell
     */
    private void markAdjacentAsImpossible(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int newRow = row + dr;
                int newCol = col + dc;

                if (isValidCell(newRow, newCol) && knownBoard[newRow][newCol] == 0) {
                    knownBoard[newRow][newCol] = 1;
                }
            }
        }
    }

    /**
     * Generates smart target cells based on current hits.
     *
     * <p>Groups hits by proximity to identify separate ships,
     * then generates high-priority targets for each group.</p>
     */
    private void generateSmartTargets() {
        targetQueue.clear();
        System.out.println("      🎯 Generating targets for " + currentShipHits.size() + " hits");

        if (currentShipHits.isEmpty()) return;

        // Group hits into possible different ships
        List<List<int[]>> shipGroups = groupHitsByProximity();

        System.out.println("      📊 Ship groups detected: " + shipGroups.size());

        // Generate targets for each group
        for (List<int[]> group : shipGroups) {
            System.out.println("         Group with " + group.size() + " hits");
            generateTargetsForGroup(group);
        }

        System.out.println("      📋 Total targets: " + targetQueue.size());
    }

    /**
     * Groups hits by proximity to identify separate ships.
     *
     * <p>Uses BFS to find connected hits. Hits that are adjacent
     * are grouped together as they likely belong to the same ship.</p>
     *
     * @return list of groups, where each group is a list of hit coordinates
     */
    private List<List<int[]>> groupHitsByProximity() {
        List<List<int[]>> groups = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        for (int[] hit : currentShipHits) {
            String key = hit[0] + "," + hit[1];
            if (processed.contains(key)) continue;

            List<int[]> group = new ArrayList<>();
            Queue<int[]> queue = new LinkedList<>();

            queue.add(hit);
            processed.add(key);
            group.add(hit);

            // BFS to find connected hits
            while (!queue.isEmpty()) {
                int[] current = queue.poll();

                for (Direction dir : Direction.values()) {
                    int newRow = current[0] + dir.dr;
                    int newCol = current[1] + dir.dc;
                    String newKey = newRow + "," + newCol;

                    if (processed.contains(newKey)) continue;

                    // Search if there's a hit at this position
                    for (int[] otherHit : currentShipHits) {
                        if (otherHit[0] == newRow && otherHit[1] == newCol) {
                            processed.add(newKey);
                            group.add(otherHit);
                            queue.add(otherHit);
                            break;
                        }
                    }
                }
            }

            groups.add(group);
        }

        return groups;
    }

    /**
     * Generates target cells for a specific group of hits.
     *
     * <p>For single hits, adds all four adjacent cells.
     * For multiple hits, determines orientation and prioritizes extremes.</p>
     *
     * @param group list of hit coordinates belonging to the same ship
     */
    private void generateTargetsForGroup(List<int[]> group) {
        if (group.size() == 1) {
            // Single hit - 4 directions
            int[] hit = group.get(0);
            for (Direction dir : Direction.values()) {
                int newRow = hit[0] + dir.dr;
                int newCol = hit[1] + dir.dc;
                if (isValidTarget(newRow, newCol)) {
                    targetQueue.add(new ScoredCell(newRow, newCol, 100));
                }
            }
        } else {
            // Multiple hits - determine orientation
            Direction orientation = determineOrientationForGroup(group);

            if (orientation != null) {
                int[] firstHit = findExtremeInGroup(group, orientation.opposite());
                int[] lastHit = findExtremeInGroup(group, orientation);
                addExtremeCells(firstHit, orientation.opposite(), 200);
                addExtremeCells(lastHit, orientation, 200);
            } else {
                // No clear orientation - explore all
                for (int[] hit : group) {
                    for (Direction dir : Direction.values()) {
                        int newRow = hit[0] + dir.dr;
                        int newCol = hit[1] + dir.dc;
                        if (isValidTarget(newRow, newCol)) {
                            targetQueue.add(new ScoredCell(newRow, newCol, 80));
                        }
                    }
                }
            }
        }
    }

    /**
     * Determines the orientation (horizontal or vertical) of a group of hits.
     *
     * <p>Checks if all hits are in the same row (horizontal) or
     * same column (vertical).</p>
     *
     * @param group list of hit coordinates
     * @return the direction of the ship's orientation, or null if unclear
     */
    private Direction determineOrientationForGroup(List<int[]> group) {
        if (group.size() < 2) return null;

        // Horizontal
        int firstRow = group.get(0)[0];
        if (group.stream().allMatch(h -> h[0] == firstRow)) {
            List<int[]> sorted = new ArrayList<>(group);
            sorted.sort(Comparator.comparingInt(a -> a[1]));
            return sorted.get(0)[1] < sorted.get(sorted.size()-1)[1] ? Direction.EAST : Direction.WEST;
        }

        // Vertical
        int firstCol = group.get(0)[1];
        if (group.stream().allMatch(h -> h[1] == firstCol)) {
            List<int[]> sorted = new ArrayList<>(group);
            sorted.sort(Comparator.comparingInt(a -> a[0]));
            return sorted.get(0)[0] < sorted.get(sorted.size()-1)[0] ? Direction.SOUTH : Direction.NORTH;
        }

        return null;
    }

    /**
     * Finds the extreme (furthest) hit in a group in a given direction.
     *
     * @param group list of hit coordinates
     * @param dir direction to search for the extreme
     * @return coordinates of the extreme hit
     */
    private int[] findExtremeInGroup(List<int[]> group, Direction dir) {
        int[] extreme = group.get(0);
        for (int[] hit : group) {
            switch (dir) {
                case NORTH -> { if (hit[0] < extreme[0]) extreme = hit; }
                case SOUTH -> { if (hit[0] > extreme[0]) extreme = hit; }
                case WEST -> { if (hit[1] < extreme[1]) extreme = hit; }
                case EAST -> { if (hit[1] > extreme[1]) extreme = hit; }
            }
        }
        return extreme;
    }

    /**
     * Adds cells at the extremes of a ship group to the target queue.
     *
     * @param extreme coordinates of the extreme hit
     * @param dir direction to extend from the extreme
     * @param score priority score for the new target
     */
    private void addExtremeCells(int[] extreme, Direction dir, int score) {
        int newRow = extreme[0] + dir.dr;
        int newCol = extreme[1] + dir.dc;
        if (isValidTarget(newRow, newCol)) {
            targetQueue.add(new ScoredCell(newRow, newCol, score));
        }
    }

    /**
     * Selects a shot using probabilistic HUNT mode.
     *
     * <p>Updates the heat map based on possible ship placements,
     * then selects a random cell from those with maximum probability.</p>
     *
     * @return coordinates {row, col} of the selected shot
     */
    private int[] getHuntModeShot() {
        updateHeatMap();
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

        return bestCells.isEmpty() ? getRandomAvailableCell() :
                bestCells.get(new Random().nextInt(bestCells.size()));
    }

    /**
     * Updates the probability heat map based on possible ship placements.
     *
     * <p>For each remaining ship size, calculates all valid placements
     * and increments the heat value of affected cells.</p>
     */
    private void updateHeatMap() {
        for (int i = 0; i < 10; i++) Arrays.fill(heatMap[i], 0);
        for (int size = 1; size <= 4; size++) {
            if (remainingShips[size - 1] <= 0) continue;
            int count = remainingShips[size - 1];
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    if (canPlaceShip(row, col, size, true)) {
                        for (int i = 0; i < size; i++) heatMap[row][col + i] += count;
                    }
                    if (canPlaceShip(row, col, size, false)) {
                        for (int i = 0; i < size; i++) heatMap[row + i][col] += count;
                    }
                }
            }
        }
    }

    /**
     * Checks if a ship of given size can be placed at the specified position.
     *
     * @param row starting row
     * @param col starting column
     * @param size ship size
     * @param horizontal true for horizontal placement, false for vertical
     * @return true if placement is valid, false otherwise
     */
    private boolean canPlaceShip(int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (!isValidCell(r, c) || knownBoard[r][c] == 1 || knownBoard[r][c] == 3) return false;
        }
        return true;
    }

    /**
     * Selects a random available cell from all unshot positions.
     *
     * <p>Used as a fallback when no better strategy is available.</p>
     *
     * @return coordinates {row, col} of a random available cell, or {0,0} if none exist
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

    /**
     * Checks if a cell has already been shot at.
     *
     * @param row row index
     * @param col column index
     * @return true if the cell has been shot, false otherwise
     */
    private boolean hasBeenShot(int row, int col) {
        return shotHistory.contains(row + "," + col);
    }

    /**
     * Checks if a cell is within the valid board boundaries.
     *
     * @param row row index
     * @param col column index
     * @return true if the cell is within bounds (0-9), false otherwise
     */
    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < 10 && col >= 0 && col < 10;
    }

    /**
     * Resets the AI to its initial state.
     *
     * <p>Clears all tracking data, resets ship counts, and
     * reinitializes the knowledge board and heat map.</p>
     */
    public void reset() {
        currentMode = Mode.HUNT;
        targetQueue.clear();
        currentShipHits.clear();
        shotHistory.clear();
        remainingShips[0] = 4; remainingShips[1] = 3;
        remainingShips[2] = 2; remainingShips[3] = 1;
        for (int i = 0; i < 10; i++) {
            Arrays.fill(knownBoard[i], 0);
            Arrays.fill(heatMap[i], 0);
        }
    }

/**
 * Returns debug information about the current AI state.
 *
 * @return formatted string with mode, queue size, active hits,
 * and remaining ships
 * */
    public String getDebugInfo() {
        return String.format("Mode: %s | Queue: %d | Hits: %d | Ships: %s",
                currentMode, targetQueue.size(), currentShipHits.size(),
                Arrays.toString(remainingShips));
    }
}