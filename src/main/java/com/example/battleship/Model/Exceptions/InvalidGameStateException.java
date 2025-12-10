package com.example.battleship.Model.Exceptions;

public class InvalidGameStateException extends RuntimeException {
    public InvalidGameStateException(String message) {
        super(message);
    }
}