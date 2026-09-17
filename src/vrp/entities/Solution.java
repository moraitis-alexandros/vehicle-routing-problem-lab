package vrp.entities;

import java.util.List;

/**
 * Immutable data class representing a full VRP solution: a set of routes that
 * together cover all customers, along with the total cost of the solution.
 */
public class Solution {

    private final List<Route> solutionRoutes;
    private final long solutionCost;

    /**
     * Creates a solution with an immutable copy of the given list of routes and
     * the total solution cost.
     */
    public Solution(List<Route> solutionRoutes, long solutionCost) {
        this.solutionRoutes = List.copyOf(solutionRoutes);
        this.solutionCost = solutionCost;
    }

    /**
     * Copy constructor. Creates a new Solution from an existing one, copying the
     * cost and an immutable copy of the routes list.
     */
    public Solution(Solution other) {
        this.solutionCost = other.getSolutionCost();
        this.solutionRoutes = List.copyOf(other.solutionRoutes);

    }

    public long getSolutionCost() {
        return solutionCost;
    }

    public List<Route> getSolutionRoutes() {
        return solutionRoutes;
    }

}
