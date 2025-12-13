package com.example.battleship.Model.Ship;

/**
 * Abstract adapter class for {@link IShip} that provides default implementations
 * of all interface methods by delegating to a wrapped IShip instance.
 *
 * <p>This adapter allows subclasses to override only the methods they need to customize,
 * without being forced to implement all interface methods. This is particularly useful
 * when you want to add behavior (logging, validation, statistics) to specific ship operations.</p>
 * @author Battleship Development Team
 * @version 1.0
 * @see IShip
 * @since 1.0
 */
public abstract class ShipAdapter implements IShip {

    /**
     * The wrapped IShip instance that handles the actual ship logic.
     */
    private final IShip ship;

    /**
     * Creates a new ShipAdapter wrapping the given ship instance.
     * The adapter will delegate all method calls to this ship unless overridden.
     *
     * @param ship the ship to wrap and adapt
     * @throws NullPointerException if ship is null
     */
    public ShipAdapter(IShip ship) {
        if (ship == null) {
            throw new NullPointerException("Ship cannot be null");
        }
        this.ship = ship;
    }

    /**
     * Returns whether the ship has been sunk.
     * <p>Default implementation delegates to the wrapped ship.</p>
     *
     * @return true if sunken, false otherwise
     */
    @Override
    public boolean isSunken() {
        return ship.isSunken();
    }

    /**
     * Registers a hit against the ship.
     * <p>Default implementation delegates to the wrapped ship.</p>
     */
    @Override
    public void registerHit() {
        ship.registerHit();
    }

    /**
     * Returns the base row (y) where the ship was placed.
     * <p>Default implementation delegates to the wrapped ship.</p>
     *
     * @return base row coordinate
     */
    @Override
    public int getRow() {
        return ship.getRow();
    }

    /**
     * Returns the base column (x) where the ship was placed.
     * <p>Default implementation delegates to the wrapped ship.</p>
     *
     * @return base column coordinate
     */
    @Override
    public int getCol() {
        return ship.getCol();
    }

    /**
     * Returns the size (number of cells) occupied by the ship.
     * <p>Default implementation delegates to the wrapped ship.</p>
     *
     * @return ship size in cells
     */
    @Override
    public int getShipSize() {
        return ship.getShipSize();
    }

    /**
     * Returns the placement direction for the ship.
     * <p>Default implementation delegates to the wrapped ship.</p>
     *
     * @return Direction enum value
     */
    @Override
    public Direction getDirection() {
        return ship.getDirection();
    }

    /**
     * Returns the number of registered hits this ship currently has.
     * <p>Default implementation delegates to the wrapped ship.</p>
     *
     * @return hit count
     */
    @Override
    public int getHitCount() {
        return ship.getHitCount();
    }

    /**
     * Returns the wrapped IShip instance.
     * Useful if you need direct access to the underlying Ship object.
     *
     * @return the wrapped IShip instance
     */
    protected IShip getShip() {
        return ship;
    }
}