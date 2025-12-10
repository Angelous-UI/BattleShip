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
}
