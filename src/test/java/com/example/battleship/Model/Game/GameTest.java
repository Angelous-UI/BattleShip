package com.example.battleship.Model.Game;

import com.example.battleship.Model.Board.Board;
import com.example.battleship.Model.Exceptions.InvalidPositionException;
import com.example.battleship.Model.Player.Human;
import com.example.battleship.Model.Ship.Frigate;
import com.example.battleship.Model.Ship.IShip;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testPlayersInitialized() {
        Game game = new Game();
        assertEquals(2, game.getPlayers().size());
    }

    @Test
    void testPlaceValidShip() {
        Game game = new Game();
        IShip ship = new Frigate(3, 3, IShip.Direction.RIGHT);

        assertDoesNotThrow(() -> game.placeShip(ship));

        // Board should have a ship at 3,3
        assertEquals(1, game.getBoard().getCell(3, 3));
    }

    @Test
    void testInvalidShipPlacementOutsideBoard() {
        Game game = new Game();

        // A ship outside limits
        IShip ship = new Frigate(11, 11, IShip.Direction.UP);

        assertThrows(InvalidPositionException.class, () -> game.placeShip(ship));
    }

    @Test
    void testCannotPlaceShipOnOccupiedCell() {
        Game game = new Game();
        IShip s1 = new Frigate(5, 5, IShip.Direction.UP);
        IShip s2 = new Frigate(5, 5, IShip.Direction.DOWN);

        game.placeShip(s1);
        assertThrows(InvalidPositionException.class, () -> game.placeShip(s2));
    }

    @Test
    void testGetShipCoordinates() {
        Game game = new Game();
        IShip f = new Frigate(2, 2, IShip.Direction.RIGHT);

        var coords = game.getShipCoordinates(f);

        assertEquals(1, coords.size());
        assertArrayEquals(new int[]{2,2}, coords.get(0));
    }

    @Test
    void testAdvanceTurnLoopsThroughPlayers() {
        Game game = new Game();

        Object p1 = game.getPlayers().get(0);
        Object p2 = game.getPlayers().get(1);

        assertEquals(p1, game.getCurrentPlayer());

        game.advanceTurn();
        assertEquals(p2, game.getCurrentPlayer());

        game.advanceTurn();
        assertEquals(p1, game.getCurrentPlayer()); // vuelve al inicio
    }
}
