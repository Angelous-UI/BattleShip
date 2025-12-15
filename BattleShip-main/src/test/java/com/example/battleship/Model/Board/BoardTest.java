package com.example.battleship.Model.Board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void testBoardStartsEmpty() {
        Board board = new Board();
        //Uses indexes from 0-9, not 1-10
        assertEquals(0, board.getCell(0, 0));
        assertEquals(0, board.getCell(9, 9));
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
        assertEquals(0, board.getCell(99, 99));
    }

    //Verifies multiple states in a cell
    @Test
    void testCellStates() {
        Board board = new Board();
        board.setCell(0, 0, 0); // Agua vacía
        board.setCell(1, 1, 1); // Barco
        board.setCell(2, 2, 2); // Fallo
        board.setCell(3, 3, 3); // Impacto

        assertEquals(0, board.getCell(0, 0));
        assertEquals(1, board.getCell(1, 1));
        assertEquals(2, board.getCell(2, 2));
        assertEquals(3, board.getCell(3, 3));
    }
}
