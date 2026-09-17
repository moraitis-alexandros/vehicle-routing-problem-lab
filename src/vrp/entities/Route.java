package vrp.entities;

import java.util.List;
import java.util.Objects;

/**
 * Immutable data class representing a single vehicle route: an ordered list of
 * nodes to visit (typically depot, one or more customers, depot) along with the
 * route's identifier and its precomputed travel cost.
 */
public class Route {

    private final long id;
    private final List<LocationNode> locationNodeVisitList;
    private final long routeCost;

    /**
     * Creates a route with an identifier, an immutable copy of the ordered visit
     * list, and a precomputed total cost.
     */
    public Route(long id, List<LocationNode> locationNodeVisitList, long routeCost) {
        this.id = id;
        this.locationNodeVisitList = List.copyOf(locationNodeVisitList);
        this.routeCost = routeCost;
    }

    public List<LocationNode> getLocationNodeVisitList() {
        return locationNodeVisitList;
    }

    public long getRouteCost() {
        return routeCost;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return id == route.id && routeCost == route.routeCost && Objects.equals(locationNodeVisitList, route.locationNodeVisitList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, locationNodeVisitList, routeCost);
    }
}
