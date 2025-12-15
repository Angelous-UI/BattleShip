package com.example.battleship.Model.Game;

import com.example.battleship.Model.Exceptions.InvalidPositionException;
import com.example.battleship.Model.Ship.AircraftCarrier;
import com.example.battleship.Model.Ship.Frigate;
import com.example.battleship.Model.Ship.IShip;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testPlayersInitialized() {
        Game game = new Game("TestPlayer");
        assertEquals(2, game.getPlayers().size());
        assertNotNull(game.getHuman());
        assertEquals("TestPlayer", game.getHuman().getName());
    }

    @Test
    void testPlaceValidShip() {
        Game game = new Game("TestPlayer");
        IShip ship = new Frigate(3, 3, IShip.Direction.RIGHT);

        assertDoesNotThrow(() -> game.placeShip(ship));
        assertEquals(1, game.getMachineBoard().getCell(3, 3));
    }

    @Test
    void testInvalidShipPlacementOutsideBoard() {
        Game game = new Game("TestPlayer");
        //Coordinates 11,11 are outside the 0-9 range
        IShip ship = new Frigate(11, 11, IShip.Direction.UP);
        assertThrows(InvalidPositionException.class, () -> game.placeShip(ship));
    }

    @Test
    void testCannotPlaceShipOnOccupiedCell() {
        Game game = new Game("TestPlayer");
        IShip s1 = new Frigate(5, 5, IShip.Direction.UP);
        IShip s2 = new Frigate(5, 5, IShip.Direction.DOWN);

        game.placeShip(s1);
        assertThrows(InvalidPositionException.class, () -> game.placeShip(s2));
    }

    @Test
    void testGetShipCoordinates() {
        Game game = new Game("TestPlayer");
        IShip frigate = new Frigate(2, 2, IShip.Direction.RIGHT);

        var coords = game.getShipCoordinates(frigate);

        assertEquals(1, coords.size());
        assertEquals(2, coords.get(0)[0]); // row
        assertEquals(2, coords.get(0)[1]); // col
    }

    // Test for a bigger ship
    @Test
    void testGetShipCoordinatesMultiCell() {
        Game game = new Game("TestPlayer");
        IShip carrier = new AircraftCarrier(0, 0, IShip.Direction.RIGHT);

        var coords = game.getShipCoordinates(carrier);

        assertEquals(4, coords.size());
        // Verifies that the coordinates are sequential
        assertEquals(0, coords.get(0)[1]);
        assertEquals(1, coords.get(1)[1]);
        assertEquals(2, coords.get(2)[1]);
        assertEquals(3, coords.get(3)[1]);
    }

    @Test
    void testAdvanceTurnChangePlayer() {
        Game game = new Game("TestPlayer");
        game.startGame();

        Object player1 = game.getCurrentPlayer();
        assertNotNull(player1);

        game.advanceTurn();
        Object player2 = game.getCurrentPlayer();

        // Verifies the player has been changed
        assertNotEquals(player1, player2);
    }

    // Test to verify that the game detects game over
    @Test
    void testGameOverDetection() {
        Game game = new Game("TestPlayer");
        game.generateFleet();
        game.startGame();

        assertFalse(game.isGameOver());


        for (IShip ship : game.getMachineFleet()) {
            for (int i = 0; i < ship.getShipSize(); i++) {
                ship.registerHit();
            }
        }

        // The game should notice that all ships are sunken
        assertTrue(game.getMachineFleet().stream().allMatch(IShip::isSunken));
    }

    // Test to generate fleet
    @Test
    void testGenerateFleet() {
        Game game = new Game("TestPlayer");
        game.generateFleet();

        var fleet = game.getMachineFleet();

        // Verifies if there are 10 ships
        assertEquals(10, fleet.size());

        // Verifies the fleet composition
        long carriers = fleet.stream().filter(s -> s.getShipSize() == 4).count();
        long submarines = fleet.stream().filter(s -> s.getShipSize() == 3).count();
        long destroyers = fleet.stream().filter(s -> s.getShipSize() == 2).count();
        long frigates = fleet.stream().filter(s -> s.getShipSize() == 1).count();

        assertEquals(1, carriers);
        assertEquals(2, submarines);
        assertEquals(3, destroyers);
        assertEquals(4, frigates);
    }
}
