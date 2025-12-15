package com.example.battleship.Model.TextFile;

/**
 * Defines a contract for plain text file input and output operations.
 * <p>
 * Implementations of this interface are responsible for writing textual
 * data to files and reading it back using a defined format.
 * </p>
 */
public interface IPlaneTextFileHandler {

    /**
     * Writes text content to a file.
     *
     * @param fileName name of the file to write to
     * @param content  text content to be stored
     */
    void writeToFile(String fileName, String content);

    /**
     * Reads text content from a file and returns it as an array of strings.
     *
     * @param fileName name of the file to read from
     * @return an array of strings read from the file
     */
    String[] readFromFile(String fileName);
}
