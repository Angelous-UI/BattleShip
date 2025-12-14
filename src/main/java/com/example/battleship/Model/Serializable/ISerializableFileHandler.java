package com.example.battleship.Model.Serializable;

/**
 * Defines a contract for file-based object serialization and deserialization.
 * <p>
 * Implementations of this interface are responsible for persisting objects
 * to storage and restoring them using a specific serialization strategy.
 * </p>
 */
public interface ISerializableFileHandler {

    /**
     * Serializes an object and stores it in a file.
     *
     * @param fileName name of the file where the object will be saved
     * @param object   object to serialize
     */
    void serialize(String fileName, Object object);

    /**
     * Deserializes an object from a file.
     *
     * @param fileName name of the file to read from
     * @return the deserialized object, or {@code null} if the operation fails
     */
    Object deserialize(String fileName);

    boolean delete(String filename);
}
