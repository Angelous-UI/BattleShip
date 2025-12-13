package com.example.battleship.Model.Serializable;

import java.io.*;

/**
 * Handles object serialization and deserialization using Java's
 * built-in object streams.
 * <p>
 * This class provides a simple file-based persistence mechanism
 * by implementing the {@link ISerializableFileHandler} interface.
 * </p>
 */
public class SerializableFileHandler implements ISerializableFileHandler {

    /**
     * Serializes an object and writes it to a file.
     *
     * @param fileName name of the file where the object will be stored
     * @param object   object to be serialized
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
     * Deserializes an object from a file.
     *
     * @param fileName name of the file to read from
     * @return the deserialized object, or {@code null} if an error occurs
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
