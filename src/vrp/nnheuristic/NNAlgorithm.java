package vrp.nnheuristic;

import vrp.entities.LocationNode;
import vrp.entities.Route;
import vrp.entities.Solution;
import vrp.entities.Truck;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the Nearest Neighbor (NN) heuristic for constructing VRP routes by
 * always visiting the closest feasible unserviced customer next.
 */
public class NNAlgorithm {

    /**
     * Default no-argument constructor, performs no initialization.
     */
    public NNAlgorithm() {
    }

    /**
     * Builds a VRP solution using the Nearest Neighbor heuristic. Separates
     * customers from the depot, then repeatedly builds new routes (starting and
     * ending at the depot) by always appending the nearest remaining,
     * unserviced customer that keeps the route within the truck's capacity; if
     * the nearest candidate does not fit, the next-nearest is tried, and so on.
     * Continues creating routes until every customer has been serviced.
     * Computes each route's cost and the total solution cost.
     *
     * @return a {@code Solution} containing the constructed routes and their
     *         total cost
     */
    public Solution solve(List<LocationNode> locationNodeList, Truck truckType) {

        List<LocationNode> customers = filterCustomers(locationNodeList);
        LocationNode depot = filterDepot(locationNodeList);
        List<LocationNode> customersRemaining = new ArrayList<>(customers); //Just a deep copy
        List<LocationNode> customersServiced = new ArrayList<>();


        int routeId = 0;
        List<Route> routeSolutionList = new ArrayList<>();
        while (customersServiced.size() < customers.size()) {

            List<LocationNode> locationNodesForNewRoute = new ArrayList<>();


            int remainingSize = customersRemaining.size();
            int routeIndex = 0;

            //Add Depot at first
            locationNodesForNewRoute.add(depot);

            //We will do as much iterations as the max customer number (or maybe less) but never exceed them
            for (int i = 0; i < remainingSize; i++) {
                LocationNode lastLocationNodeElement = locationNodesForNewRoute.get(locationNodesForNewRoute.size() - 1);
                sortByDistance(lastLocationNodeElement, customersRemaining);

                //Customers remaining are sorted so we get the ith element and we check feasibility
                //And we continue until route demand is covered or all customer investigation is finished
                LocationNode customerToCheck = customersRemaining.get(routeIndex);
                customersRemaining.remove(customerToCheck);
                locationNodesForNewRoute.add(customerToCheck);

                if (!isRouteFeasible(locationNodesForNewRoute, truckType.getTruckCapacity())) {
                    customersRemaining.add(customerToCheck);
                    locationNodesForNewRoute.remove(customerToCheck);
                    routeIndex++;
                } else {
                    customersServiced.add(customerToCheck);
                    routeIndex = 0; //The index 1 will become 0
                }
            }

            //Add depot to end
            locationNodesForNewRoute.add(depot);


            long routeCost = calculateRouteCost(locationNodesForNewRoute);
            Route newRoute = new Route(routeId, locationNodesForNewRoute, routeCost);
            routeId++;
            routeSolutionList.add(newRoute);
        }

        long solutionCost = calculateSolutionCost(routeSolutionList);
        Solution newSolution = new Solution(routeSolutionList, solutionCost);

        return newSolution;
    }


    /**
     * Sorts the given list of customers in place by ascending distance from the
     * specified reference node.
     */
    private void sortByDistance(LocationNode fromNode, List<LocationNode> customers) {
        customers
                .sort((customer1, customer2) -> calculateDistanceBetweenNodes(fromNode, customer1).compareTo(calculateDistanceBetweenNodes(fromNode, customer2)));
    }

    /**
     * Computes and returns the rounded Euclidean distance between two nodes.
     */
    private Long calculateDistanceBetweenNodes(LocationNode nodeFrom, LocationNode nodeTo) {
        long dx = nodeTo.getX() - nodeFrom.getX();
        long dy = nodeTo.getY() - nodeFrom.getY();

        return Math.round(Math.sqrt(dx * dx + dy * dy));
    }

    /**
     * Extracts and returns the single depot node from the full location list.
     * Throws a {@code RuntimeException} if there isn't exactly one depot.
     */
    private LocationNode filterDepot(List<LocationNode> locationNodeListWithDepot) {

        List<LocationNode> depotList = locationNodeListWithDepot
                .stream()
                .filter(node -> node.isDepot())
                .collect(Collectors.toList());

        if (depotList.size() != 1) {
            throw new RuntimeException("Depot should only be one");
        }

        return depotList.get(0);
    }

    /**
     * Returns a new list containing only the non-depot (customer) nodes from
     * the given list, without mutating the original.
     */
    private List<LocationNode> filterCustomers(List<LocationNode> locationNodeListWithDepot) {
        List<LocationNode> locationNodeListWithDepotDeepCopy = new ArrayList<>(locationNodeListWithDepot); //Deep Copy, to avoid mutation

        List<LocationNode> customers = locationNodeListWithDepotDeepCopy
                .stream()
                .filter(node -> !node.isDepot())
                .collect(Collectors.toList());

        return customers;
    }

    /**
     * Checks whether any single customer's demand exceeds the truck's capacity;
     * throws a {@code RuntimeException} if so, since such a customer could
     * never be served.
     */
    private void validateRoute(List<LocationNode> routeNodes, long truckCapacity) {
        List<LocationNode> illegalCustomers = routeNodes
                .stream()
                .filter(customer -> customer.getDemand() > truckCapacity)
                .collect(Collectors.toList());

        if (!illegalCustomers.isEmpty()) {
            throw new RuntimeException("The truck capacity defined should be greater of each demand separately");
        }
    }

    /**
     * First validates individual demands via {@code validateRoute}. Returns
     * true if the route is empty, or if the sum of all assigned customers'
     * demand does not exceed the truck's capacity; otherwise returns false.
     */
    private boolean isRouteFeasible(List<LocationNode> assignedCustomers, long truckCapacity) {

        validateRoute(assignedCustomers, truckCapacity);

        if (assignedCustomers.isEmpty()) {
            return true;
        }

        long totalRouteDemand = assignedCustomers
                .stream()
                .mapToLong(LocationNode::getDemand)
                .sum();

        return truckCapacity >= totalRouteDemand;
    }

    /**
     * Sums the distances between each consecutive pair of nodes in the route to
     * compute its total travel cost.
     */
    private long calculateRouteCost(List<LocationNode> routeNodes) {
        long routeTotalCost = 0;
        for (int i = 0; i < routeNodes.size() - 1; i++) {
            LocationNode fromNode = routeNodes.get(i);
            LocationNode toNode = routeNodes.get(i + 1);
            long currentDistanceCost = calculateDistanceBetweenNodes(fromNode, toNode);
            routeTotalCost += currentDistanceCost;
        }
        return routeTotalCost;
    }

    /**
     * Sums the routeCost of every route in the list to compute the overall
     * solution cost.
     */
    private long calculateSolutionCost(List<Route> routesList) {

        long solutionTotalCost = routesList
                .stream()
                .map(route -> route.getRouteCost())
                .reduce((a,b) -> a + b).get();

       return solutionTotalCost;
    }
}
