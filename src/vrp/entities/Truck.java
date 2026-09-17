package vrp.entities;

/**
 * Immutable data class representing a truck (vehicle) type used to service routes,
 * defined by an identifier, a maximum carrying capacity, and a speed.
 */
public class Truck {

    private final long truckId;
    private final long truckSpeed;
    private final long truckCapacity;

    /**
     * Creates a truck with an identifier, maximum capacity, and speed.
     * Note: constructor parameter order is (truckId, truckCapacity, truckSpeed).
     */
    public Truck(long truckId, long truckCapacity, long truckSpeed) {
        this.truckSpeed = truckSpeed;
        this.truckId = truckId;
        this.truckCapacity = truckCapacity;
    }

    /**
     * Returns the truck's maximum carrying capacity.
     */
    public long getTruckCapacity() {
        return truckCapacity;
    }

}
