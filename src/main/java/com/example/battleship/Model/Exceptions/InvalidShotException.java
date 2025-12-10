package com.example.battleship.Model.Exceptions;

public class InvalidShotException extends RuntimeException {
    public InvalidShotException(String message) {
        super(message);
    }
}
