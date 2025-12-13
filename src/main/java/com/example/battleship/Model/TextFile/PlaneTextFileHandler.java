package com.example.battleship.Model.TextFile;

import java.io.*;

/**
 * Handles plain text file reading and writing operations.
 * <p>
 * This class provides basic functionality to persist and retrieve
 * text-based data using standard Java I/O streams.
 * </p>
 *
 * <p>
 * It implements the {@link IPlaneTextFileHandler} interface to ensure
 * consistent file handling behavior.
 * </p>
 */
public class PlaneTextFileHandler implements IPlaneTextFileHandler {

    /**
     * Writes text content to a file.
     *
     * @param fileName name of the file to write to
     * @param content  text content to be written
     */
    @Override
    public void writeToFile(String fileName, String content){
        try (BufferedWriter Writer = new BufferedWriter(new FileWriter(fileName))){
            Writer.write(content);
            Writer.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Reads text content from a file and splits it into tokens.
     *
     * <p>
     * Each line is trimmed and concatenated using a comma delimiter,
     * then split into a string array.
     * </p>
     *
     * @param fileName name of the file to read from
     * @return an array of strings extracted from the file
     */
    @Override
    public String[] readFromFile(String fileName){
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String line;
            while((line = reader.readLine()) != null){
                content.append(line.trim()).append(",");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return content.toString().split(",");
    }

}
