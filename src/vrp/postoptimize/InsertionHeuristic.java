package vrp.postoptimize;

import vrp.entities.LocationNode;
import vrp.entities.Route;
import vrp.entities.Solution;
import vrp.entities.Truck;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements a post-optimization improvement step that tries relocating each
 * customer to a better position across all routes (a form of local-search
 * insertion improvement) to reduce the total solution cost.
 */
public class InsertionHeuristic {

    /**
     * Attempts to improve an existing solution by relocating each customer to
     * the best feasible position found across all routes. Starts with a deep
     * copy of the input solution as the current best. For each customer,
     * removes it from whichever route currently contains it (working on a
     * fresh mutable copy of the routes), then tries reinserting that customer
     * at every feasible position within every route (including the one it was
     * removed from). For each feasible reinsertion, builds a full candidate
     * solution (the modified route plus all other unchanged routes), computes
     * its total cost, and if it is strictly better than the current best
     * solution, adopts it as the new best.
     *
     * @return the best solution found after evaluating all customers
     */
    public Solution solve(Solution solutionToOptimize, List<LocationNode> locationNodeList, Truck truck) {

        Solution bestSolution = new Solution(solutionToOptimize); //deep copy

        //Get all customers
        List<LocationNode> customers = filterCustomers(locationNodeList);


        for (LocationNode customer : customers) {

            //In routeList find the list containing the customer and remove him

            //We need a List<List<LocationNode> that will not contain the client
            //Fresh copy
            List<List<LocationNode>> routeList = collapseRoutes(bestSolution.getSolutionRoutes());

            List<List<LocationNode>> filteredRouteList = routeList
                    .stream()
                    .filter(route -> route.contains(customer))
                    .collect(Collectors.toList());

            filteredRouteList.get(0).remove(customer); //the customer is also removed from routeList


            //Now our route list does not include the customer inside
            //Iterate on each routeList
            for (List<LocationNode> locationNodes : routeList) {

                List<Placement> customerPossiblePlacementsOnCurrentRoute = findPlacementsInRoute(locationNodes, customer, truck);

                //Now filter from routeList the current route we are investigating
                List<List<LocationNode>> routesFiltered = routeList
                        .stream()
                        .filter(routes -> !routes.equals(locationNodes))
                        .collect(Collectors.toList());

                //Populate routes from nodes
                List<Route> routesUnchanged = populateUnchangedRoutes(routesFiltered);

                //Now create possible routes from placements
                List<Route> routesFromPlacements = populateRoutesFromPlacements(customerPossiblePlacementsOnCurrentRoute);

                // Now for each possible route that occured from placements
                // Add to the routesUnchanged
                // Create a solution and calculate its cost,
                // If it is better from current best keep the solution

                for (Route routeFromPlacement : routesFromPlacements) {

                    List<Route> tempRouteSolution = new ArrayList<>();
                    tempRouteSolution.add(routeFromPlacement);
                    tempRouteSolution.addAll(routesUnchanged);
                    long solutionCost = calculateSolutionCost(tempRouteSolution);

                    Solution tempSolution = new Solution(tempRouteSolution, solutionCost);

                    if (tempSolution.getSolutionCost() < bestSolution.getSolutionCost()) {
                        //Create a new best solution
                        bestSolution = new Solution(tempSolution);
                    }
                }
            }
        }
        return bestSolution;
    }

    /**
     * Converts a list of raw node-list routes (that were not touched during a
     * given customer's relocation attempt) into {@code Route} objects with
     * sequential ids and computed costs.
     */
    private List<Route> populateUnchangedRoutes(List<List<LocationNode>> routesToPopulate) {

        List<Route> routes = new ArrayList<>();
        int index = 0;
        for (List<LocationNode> locationNodes : routesToPopulate) {

            long routeCost = calculateRouteCost(locationNodes);
            Route route = new Route(index, locationNodes, routeCost);
            routes.add(route);
            index++;
        }
        return routes;
    }

    /**
     * Converts each candidate {@code Placement} (a route with the customer
     * inserted at a specific position) into a {@code Route} object with cost
     * computed from its node list, using a placeholder route id of -1.
     */
    private List<Route> populateRoutesFromPlacements(List<Placement> placements) {

        List<Route> routes = new ArrayList<>();

        for (Placement placement : placements) {

            long routeCost = calculateRouteCost(placement.getNodeList());
            Route routeToAdd = new Route(-1, placement.getNodeList(), routeCost);
            routes.add(routeToAdd);
        }
        return routes;
    }

    /**
     * Converts a list of {@code Route} objects into a list of plain,
     * independent (mutable, deep-copied) node lists, so they can be freely
     * modified during the search without affecting the original solution.
     */
    private List<List<LocationNode>> collapseRoutes(List<Route> routeList) {

        List<List<LocationNode>> collapsedRouteList = new ArrayList<>();

        for (Route route : routeList) {
            List<LocationNode> collapsedRoute = new ArrayList<>(route.getLocationNodeVisitList());
            collapsedRouteList.add(collapsedRoute);
        }
        return collapsedRouteList;
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

    /**
     * Checks whether any single customer's demand exceeds the truck's capacity;
     * throws a {@code RuntimeException} if so.
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
     * true if the route is empty, or if the sum of assigned customers' demand
     * does not exceed truck capacity; otherwise false.
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
     * Tries inserting the given customer at every possible position within the
     * route (starting after the initial depot), temporarily mutating and then
     * restoring the route list for each trial. For each position where the
     * resulting route remains capacity-feasible, records a {@code Placement}
     * capturing the customer id, the resulting full node list, and the
     * insertion index.
     *
     * @return the list of all feasible placements found
     */
    private List<Placement> findPlacementsInRoute(List<LocationNode> routeToCheck, LocationNode customer, Truck truck) {

        List<Placement> feasiblePlacements = new ArrayList<>();

        // We begin from 1 because 0 is the starting depot
        for (int i = 1; i < routeToCheck.size(); i++ ){

            //place customer
            routeToCheck.add(i, customer);
            if (!isRouteFeasible(routeToCheck, truck.getTruckCapacity())) {
                routeToCheck.remove(customer);
                continue;
            }

            Placement placementToAdd = new Placement();
            placementToAdd.setCustomerId(customer.getNodeId());
            placementToAdd.setNodeList(new ArrayList<>(routeToCheck));
            placementToAdd.setIndexToPlaceCustomer(i);
            feasiblePlacements.add(placementToAdd);
            routeToCheck.remove(i); //restore


        }

        return feasiblePlacements;
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
     * Computes and returns the rounded Euclidean distance between two nodes.
     */
    private Long calculateDistanceBetweenNodes(LocationNode nodeFrom, LocationNode nodeTo) {
        long dx = nodeTo.getX() - nodeFrom.getX();
        long dy = nodeTo.getY() - nodeFrom.getY();

        return Math.round(Math.sqrt(dx * dx + dy * dy));
    }

}
