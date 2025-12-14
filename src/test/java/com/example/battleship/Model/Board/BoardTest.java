package com.example.battleship.Model.Board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void testBoardStartsEmpty() {
        Board board = new Board();
        assertEquals(0, board.getCell(1, 1));
        assertEquals(0, board.getCell(10, 10));
    }

    @Test
    void testSetCellAndGetCell() {
        Board board = new Board();
        board.setCell(5, 5, 3);
        assertEquals(3, board.getCell(5, 5));
    }

    @Test
    void testInvalidCellReturnsDefaultZero() {
        Board board = new Board();
        assertEquals(0, board.getCell(99, 99));  // no existe, regresa 0
    }
}
