package vrp.entities;

import java.util.Objects;

/**
 * Immutable data class representing a single location in the VRP: either a customer
 * or the depot. Holds an identifier, a demand quantity, X/Y coordinates, and a flag
 * indicating whether this node is the depot.
 */
public class LocationNode {

    private final long nodeId;
    private final long demand;
    private final int x;
    private final  int y;
    private final boolean isDepot;

    /**
     * Creates a location node with its identifier, demand quantity, coordinates,
     * and whether it represents the depot.
     */
    public LocationNode(long nodeId, long demand, int x, int y, boolean isDepot) {
        this.nodeId = nodeId;
        this.demand = demand;
        this.x = x;
        this.y = y;
        this.isDepot = isDepot;
    }

    /**
     * Returns true if this node represents the depot, false if it is a customer.
     */
    public boolean isDepot() {
        return isDepot;
    }

    public long getNodeId() {
        return nodeId;
    }


    /**
     * Returns the demand quantity required by this node (0 for the depot).
     */
    public long getDemand() {
        return demand;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LocationNode that = (LocationNode) o;
        return nodeId == that.nodeId && demand == that.demand && x == that.x && y == that.y && isDepot == that.isDepot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, demand, x, y, isDepot);
    }
}
