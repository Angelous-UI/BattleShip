package com.example.battleship.Model.Ship;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FrigateTest {

    @Test
    void testFrigateSize() {
        Frigate frigate = new Frigate(1, 1, IShip.Direction.RIGHT);
        assertEquals(1, frigate.getShipSize());
    }

    @Test
    void testRegisterHitSinksShip() {
        Frigate frigate = new Frigate(1, 1, IShip.Direction.DOWN);

        assertFalse(frigate.isSunken());
        assertEquals(0, frigate.getHitCount());

        frigate.registerHit();

        assertTrue(frigate.isSunken());
        assertEquals(1, frigate.getHitCount());
    }

    @Test
    void testDirectionAndCoordinates() {
        Frigate frigate = new Frigate(4, 7, IShip.Direction.LEFT);

        assertEquals(IShip.Direction.LEFT, frigate.getDirection());
        assertEquals(7, frigate.getRow());
        assertEquals(4, frigate.getCol());
    }

    // Test for all directions
    @Test
    void testAllDirections() {
        Frigate up = new Frigate(5, 5, IShip.Direction.UP);
        Frigate down = new Frigate(5, 5, IShip.Direction.DOWN);
        Frigate left = new Frigate(5, 5, IShip.Direction.LEFT);
        Frigate right = new Frigate(5, 5, IShip.Direction.RIGHT);

        assertEquals(IShip.Direction.UP, up.getDirection());
        assertEquals(IShip.Direction.DOWN, down.getDirection());
        assertEquals(IShip.Direction.LEFT, left.getDirection());
        assertEquals(IShip.Direction.RIGHT, right.getDirection());
    }

    // Test for multiple hits
    @Test
    void testMultipleHitsOnSunkenShip() {
        Frigate frigate = new Frigate(0, 0, IShip.Direction.RIGHT);

        frigate.registerHit();
        assertTrue(frigate.isSunken());

        // Registering more hits shouldn't cause problems
        frigate.registerHit();
        frigate.registerHit();

        assertTrue(frigate.isSunken());
        assertEquals(3, frigate.getHitCount());
    }
}
