package com.example.battleship.Model.Player;

public class PlayerData {
    private String name;
    private int gamesPlayed;
    private int gamesWon;
    private int totalShots;
    private int totalHits;

    public PlayerData(String name) {
        this.name = name;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.totalShots = 0;
        this.totalHits = 0;
    }

    public PlayerData(String name, int gamesPlayed, int gamesWon, int totalShots, int totalHits) {
        this.name = name;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.totalShots = totalShots;
        this.totalHits = totalHits;
    }

    // Convertir a formato CSV
    public String toCSV() {
        return String.format("%s,%d,%d,%d,%d",
                name, gamesPlayed, gamesWon, totalShots, totalHits);
    }

    // Crear desde formato CSV
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

    // Getters y Setters
    public String getName() { return name; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public int getTotalShots() { return totalShots; }
    public int getTotalHits() { return totalHits; }
    public void incrementGamesPlayed() { gamesPlayed++; }
    public void incrementGamesWon() { gamesWon++; }
    public void addShots(int shots) { totalShots += shots; }
    public void addHits(int hits) { totalHits += hits; }

    public double getAccuracy() {
        return totalShots > 0 ? (totalHits * 100.0 / totalShots) : 0;
    }

    @Override
    public String toString() {
        return String.format("Jugador: %s | Partidas: %d | Ganadas: %d | Precisión: %.1f%%",
                name, gamesPlayed, gamesWon, getAccuracy());
    }
}
