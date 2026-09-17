package vrp.postoptimize;

import vrp.entities.LocationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable helper data class representing a candidate insertion of a customer
 * into a route at a specific index, along with the resulting route's node list.
 */
public class Placement {

    private long routeId;
    private long customerId;
    private int indexToPlaceCustomer;
    private long routeCost;
    private List<LocationNode> nodeList;

    /**
     * Default no-argument constructor for building a Placement incrementally
     * via setters.
     */
    public Placement() {}

    /**
     * Creates a placement with the given routeId, customerId, insertion index,
     * and route cost. The node list is not set by this constructor and must be
     * assigned separately via {@code setNodeList}.
     */
    public Placement(long routeId, long customerId, int indexToPlaceCustomer, long routeCost) {
        this.routeId = routeId;
        this.customerId = customerId;
        this.indexToPlaceCustomer = indexToPlaceCustomer;
        this.routeCost = routeCost;
    }

    /**
     * Returns the list of nodes representing the route after insertion.
     * Lazily initializes the list to an empty {@code ArrayList} if it hasn't
     * been set yet.
     */
    public List<LocationNode> getNodeList() {

        if (nodeList == null) {
            nodeList = new ArrayList<>();
        }
        return nodeList;
    }

    /**
     * Sets the full node list for this placement.
     */
    public void setNodeList(List<LocationNode> nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * Returns the id of the route this placement is associated with.
     */
    public long getRouteId() {
        return routeId;
    }

    /**
     * Sets the route id.
     */
    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    /**
     * Returns the id of the customer being placed.
     */
    public long getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer id.
     */
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the index at which the customer is inserted into the route.
     */
    public int getIndexToPlaceCustomer() {
        return indexToPlaceCustomer;
    }

    /**
     * Sets the insertion index.
     */
    public void setIndexToPlaceCustomer(int indexToPlaceCustomer) {
        this.indexToPlaceCustomer = indexToPlaceCustomer;
    }

    /**
     * Returns the precomputed cost associated with this placement's route.
     */
    public long getRouteCost() {
        return routeCost;
    }

    /**
     * Sets the route cost.
     */
    public void setRouteCost(long routeCost) {
        this.routeCost = routeCost;
    }
}
