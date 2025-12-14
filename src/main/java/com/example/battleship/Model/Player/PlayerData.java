package com.example.battleship.Model.Player;

/**
 * Represents player statistics and data persistence for the Battleship game.
 * <p>
 * This class stores individual player performance metrics including games played,
 * games won, total shots fired, and accuracy. It provides CSV serialization for
 * file-based persistence and static methods for managing multiple player profiles.
 * </p>
 *
 * <p>Player data is automatically stored in the {@code data/players.csv} file,
 * with one player record per line. The class uses case-insensitive player names
 * as unique identifiers.</p>
 *
 * @author Battleship Development Team
 * @version 1.0
 */
public class PlayerData {
    /** Player's username */
    private String name;

    /** Total number of games played by this player */
    private int gamesPlayed;

    /** Total number of games won by this player */
    private int gamesWon;

    /** Total number of shots fired across all games */
    private int totalShots;

    /** Total number of successful hits across all games */
    private int totalHits;

    /**
     * Creates a new player with default statistics (all zeros).
     *
     * @param name the player's username
     */
    public PlayerData(String name) {
        this.name = name;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.totalShots = 0;
        this.totalHits = 0;
    }

    /**
     * Creates a player with specified statistics.
     *
     * @param name the player's username
     * @param gamesPlayed total games played
     * @param gamesWon total games won
     * @param totalShots total shots fired
     * @param totalHits total successful hits
     */
    public PlayerData(String name, int gamesPlayed, int gamesWon, int totalShots, int totalHits) {
        this.name = name;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.totalShots = totalShots;
        this.totalHits = totalHits;
    }

    /**
     * Converts player data to CSV format.
     *
     * @return comma-separated string: "name,gamesPlayed,gamesWon,totalShots,totalHits"
     */
    public String toCSV() {
        return String.format("%s,%d,%d,%d,%d",
                name, gamesPlayed, gamesWon, totalShots, totalHits);
    }

    /**
     * Creates a PlayerData object from CSV array.
     *
     * @param data string array with 5 elements: [name, gamesPlayed, gamesWon, totalShots, totalHits]
     * @return PlayerData object, or null if data is invalid
     */
    public static PlayerData fromCSV(String[] data) {
        if (data.length < 5) return null;
        return new PlayerData(
                data[0],
                Integer.parseInt(data[1]),
                Integer.parseInt(data[2]),
                Integer.parseInt(data[3]),
                Integer.parseInt(data[4])
        );
    }

    /**
     * Gets the player's username.
     *
     * @return player name
     */
    public String getName() { return name; }

    /**
     * Gets total games played.
     *
     * @return number of games played
     */
    public int getGamesPlayed() { return gamesPlayed; }

    /**
     * Gets total games won.
     *
     * @return number of games won
     */
    public int getGamesWon() { return gamesWon; }

    /**
     * Gets total shots fired.
     *
     * @return total number of shots
     */
    public int getTotalShots() { return totalShots; }

    /**
     * Gets total successful hits.
     *
     * @return total number of hits
     */
    public int getTotalHits() { return totalHits; }

    /**
     * Increments the games played counter by one.
     */
    public void incrementGamesPlayed() { gamesPlayed++; }

    /**
     * Increments the games won counter by one.
     */
    public void incrementGamesWon() { gamesWon++; }

    /**
     * Adds shots to the total shot count.
     *
     * @param shots number of shots to add
     */
    public void addShots(int shots) { totalShots += shots; }

    /**
     * Adds hits to the total hit count.
     *
     * @param hits number of hits to add
     */
    public void addHits(int hits) { totalHits += hits; }

    /**
     * Calculates the player's accuracy as a percentage.
     *
     * @return accuracy percentage (0-100), or 0 if no shots have been fired
     */
    public double getAccuracy() {
        return totalShots > 0 ? (totalHits * 100.0 / totalShots) : 0;
    }

    /**
     * Returns a formatted string representation of player statistics.
     *
     * @return string containing player name, games played, games won, and accuracy
     */
    @Override
    public String toString() {
        return String.format("Player: %s | Games: %d | Won: %d | Accuracy: %.1f%%",
                name, gamesPlayed, gamesWon, getAccuracy());
    }

    // ==================== PERSISTENCE METHODS ====================

    /** Directory where player data is stored */
    private static final String DATA_DIR = "data";

    /** Filename for player data CSV */
    private static final String PLAYERS_FILE = "players.csv";

    /** Full path to player data file */
    private static final String FILE_PATH = DATA_DIR + java.io.File.separator + PLAYERS_FILE;

    static {
        createDataDirectory();
    }

    /**
     * Creates the data directory if it doesn't exist.
     */
    private static void createDataDirectory() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(DATA_DIR);
            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files.createDirectories(path);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }

    /**
     * Saves or updates a player's data to the CSV file.
     * <p>
     * If a player with the same name (case-insensitive) already exists,
     * their data is updated. Otherwise, a new entry is created.
     * </p>
     *
     * @param player the PlayerData object to save
     */
    public static void savePlayerData(PlayerData player) {
        java.util.Map<String, PlayerData> players = loadAllPlayers();
        players.put(player.getName().toLowerCase(), player);

        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(FILE_PATH))) {
            for (PlayerData p : players.values()) {
                writer.write(p.toCSV());
                writer.newLine();
            }
        } catch (java.io.IOException e) {
            System.err.println("Error saving player data: " + e.getMessage());
        }
    }

    /**
     * Loads a specific player's data from the CSV file.
     *
     * @param playerName the player's username (case-insensitive)
     * @return PlayerData object if found, null otherwise
     */
    public static PlayerData loadPlayerData(String playerName) {
        java.util.Map<String, PlayerData> players = loadAllPlayers();
        return players.get(playerName.toLowerCase());
    }

    /**
     * Loads all player data from the CSV file.
     *
     * @return map of player names (lowercase) to PlayerData objects
     */
    public static java.util.Map<String, PlayerData> loadAllPlayers() {
        java.util.Map<String, PlayerData> players = new java.util.HashMap<>();
        java.io.File file = new java.io.File(FILE_PATH);

        if (!file.exists()) {
            return players;
        }

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");
                PlayerData player = PlayerData.fromCSV(data);

                if (player != null) {
                    players.put(player.getName().toLowerCase(), player);
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("Error loading player data: " + e.getMessage());
        }

        return players;
    }

    /**
     * Returns the top players sorted by wins and accuracy.
     * <p>
     * Players are first sorted by number of wins (descending), then by
     * accuracy percentage (descending) as a tiebreaker.
     * </p>
     *
     * @param limit maximum number of players to return
     * @return list of top players, may be smaller than limit if fewer players exist
     */
    public static java.util.List<PlayerData> getTopPlayers(int limit) {
        java.util.List<PlayerData> allPlayers = new java.util.ArrayList<>(loadAllPlayers().values());

        allPlayers.sort((p1, p2) -> {
            int winComparison = Integer.compare(p2.getGamesWon(), p1.getGamesWon());
            if (winComparison != 0) return winComparison;

            return Double.compare(p2.getAccuracy(), p1.getAccuracy());
        });

        return allPlayers.subList(0, Math.min(limit, allPlayers.size()));
    }

    /**
     * Checks if a player exists in the database.
     *
     * @param playerName the player's username (case-insensitive)
     * @return true if player exists, false otherwise
     */
    public static boolean playerExists(String playerName) {
        return loadPlayerData(playerName) != null;
    }

    /**
     * Deletes a player's data from the CSV file.
     *
     * @param playerName the player's username (case-insensitive)
     */
    public static void deletePlayerData(String playerName) {
        java.util.Map<String, PlayerData> players = loadAllPlayers();
        players.remove(playerName.toLowerCase());

        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(FILE_PATH))) {
            for (PlayerData p : players.values()) {
                writer.write(p.toCSV());
                writer.newLine();
            }
        } catch (java.io.IOException e) {
            System.err.println("Error deleting player data: " + e.getMessage());
        }
    }
}