package com.example.battleship.Model.Serializable;

import java.io.*;

/**
 * Handles object serialization and deserialization for game state persistence.
 * <p>
 * This class provides file-based persistence using Java's built-in object
 * streams. It implements the {@link ISerializableFileHandler} interface to
 * save, load, and delete serialized game states.
 * </p>
 *
 * <p>Game states are stored as binary .dat files in the application directory.
 * Each player's game is saved to a separate file named "game_save_[username].dat".</p>
 *
 * @author Battleship Development Team
 * @version 1.0
 * @see ISerializableFileHandler
 * @see com.example.battleship.Model.Game.GameState
 */
public class SerializableFileHandler implements ISerializableFileHandler {

    /**
     * Serializes an object and writes it to a file.
     * <p>
     * Uses Java's ObjectOutputStream to write the object in binary format.
     * If the file already exists, it will be overwritten. The method prints
     * a success message to console upon completion.
     * </p>
     *
     * @param fileName name of the file where the object will be stored
     * @param object   object to be serialized (must implement Serializable)
     */
    @Override
    public void serialize(String fileName, Object object) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(object);
            oos.flush();
            System.out.println("✅ Object serialized: " + fileName);
        } catch (IOException e) {
            System.err.println("❌ Serialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes a serialized file from the filesystem.
     * <p>
     * Attempts to delete the specified file. Prints a confirmation message
     * if successful, or an error message if the operation fails.
     * </p>
     *
     * @param fileName name of the file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    @Override
    public boolean delete(String fileName) {
        try {
            File file = new File(fileName);

            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("🗑️ File deleted: " + fileName);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error deleting file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deserializes an object from a file.
     * <p>
     * Uses Java's ObjectInputStream to read the binary file and reconstruct
     * the object. Returns null if the file doesn't exist, is corrupted, or
     * if the class definition has changed since serialization.
     * </p>
     *
     * @param fileName name of the file to read from
     * @return the deserialized object, or null if an error occurs
     */
    @Override
    public Object deserialize(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            Object object = ois.readObject();
            System.out.println("✅ Object deserialized: " + fileName);
            return object;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Deserialization error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}