package vrp.cwheuristic;

import vrp.entities.LocationNode;
import vrp.entities.Route;
import vrp.entities.Solution;
import vrp.entities.Truck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the Clarke &amp; Wright (Clark and Wright) savings algorithm for
 * constructing VRP routes by iteratively merging single-customer routes based
 * on cost savings, subject to truck capacity constraints.
 */
public class ClarkNWrightAlgorithm {

    /**
     * Default no-argument constructor, performs no initialization.
     */
    public ClarkNWrightAlgorithm() {}

    /**
     * Builds a VRP solution using the Clarke &amp; Wright savings algorithm,
     * assuming an unlimited homogeneous truck fleet. Separates customers from
     * the depot, computes all pairwise savings values, and initializes one
     * candidate route per customer (depot-customer-depot). Repeatedly takes the
     * pair with the highest remaining savings and attempts to merge the two
     * routes containing those customers, provided the customers sit at valid
     * endpoints of their respective routes and the merged route stays within
     * truck capacity. Handles four merge cases: appending one route after
     * another in either direction, or merging two routes that share a
     * starting-point pair or an ending-point pair by reversing one route.
     * Discards infeasible or already-checked pairs. Continues until no pairs
     * remain, then builds {@code Route} objects from the final candidate routes.
     *
     * @return a {@code Solution} containing the merged routes and their total
     *         cost
     */
    public Solution solve(List<LocationNode> locationNodeList, Truck truckType) {

        //We assume unlimited homogenous truck fleet
        List<LocationNode> customers = filterCustomers(locationNodeList);
        LocationNode depot = filterDepot(locationNodeList);
        List<Pair> pairSavingsPool = calculatePairSavings(customers, depot);
        // Create one candidate route for each customer
        // i.e 0-1-0, 0-2-0, 0-3-0 etc
        List<List<LocationNode>> assignedRoutePool = generateCandidateRoutes(customers, depot);

        // At this point we have all the candidate routes and all the pair savings
        // For each pair


        while (!pairSavingsPool.isEmpty()) {

            //For each Pair
            Pair pairSaving = pairSavingsPool.get(0); //We will always get the highest (non assigned pair) on list, i.e the largest cost savings

            LocationNode fromCustomer = pairSaving.getFromNode();
            LocationNode toCustomer = pairSaving.getToNode();
            //Get route that contains fromNode
            List<LocationNode> fromRoute = getRouteForCustomer(assignedRoutePool, fromCustomer);

            //Get route that contains toNode
            List<LocationNode> toRoute = getRouteForCustomer(assignedRoutePool, toCustomer);

            //If the routes are same then the pair is already checked, proceed to the next pair
            if (fromRoute.equals(toRoute)) {
                pairSavingsPool.remove(0);
                continue;
            }

            // If different routes then check that are valid endpoints
            // If at least one is not starting or ending point then we cannot merge
            boolean fromCustomerInStartingPoint = customerIsInStartingPoint(fromRoute, fromCustomer);
            boolean fromCustomerInEndingPoint = customerIsInEndingPoint(fromRoute, fromCustomer);
            boolean toCustomerInStartingPoint = customerIsInStartingPoint(toRoute, toCustomer);
            boolean toCustomerInEndingPoint = customerIsInEndingPoint(toRoute, toCustomer);

            boolean fromCustomerIsOnEdge = customerIsOnEdge(fromCustomerInStartingPoint, fromCustomerInEndingPoint);
            boolean toCustomerIsOnEdge = customerIsOnEdge(toCustomerInStartingPoint, toCustomerInEndingPoint);

            if (!fromCustomerIsOnEdge || !toCustomerIsOnEdge) {
                //The customers cannot be connected
                pairSavingsPool.remove(0);
                continue;
            }

            //In other case we merge
            //If we have (a) 0-A-B-0 and (b) 0-C-D-0
            // i)   With pair (B,C) OR (C,B) => 0-A-B-C-D-0 append (b) on a -- B endPoint in (a) && C startingPoint in (b)
            // ii)  With pair (A,D) OR (D,A) => 0-C-D-A-B-0 append (a) on b -- A startingPoint in (a) && D endingPoint in (b)
            // iii) With pair (A,C) OR (C,A) => UNFOLD 0-B-A-C-D-0 OR 0-D-C-A-B-0 -- it is the opposite trip with different order -- Both A, C startingPoints
            // iv)  With pair (B,D) OR (D, B) => UNFOLD 0-C-D-B-A-0 OR 0-A-B-D-C-0 it is the opposite trip with different order -- Both B, D endingPoints
            // In cases (iii) & (iv) it is the same route just in reverse order. So we will keep one.

            // Case (i)
            if(fromCustomerInEndingPoint && toCustomerInStartingPoint ) {
                List<LocationNode> routeEnhanced = applyRouteOnRoute(fromRoute, toRoute);

                if (!isRouteFeasible(routeEnhanced, truckType.getTruckCapacity())) {
                    // If the capacity is not good then restore on previous route customer
                    pairSavingsPool.remove(0);
                    continue;
                }

                assignedRoutePool.remove(fromRoute);
                assignedRoutePool.remove(toRoute);
                assignedRoutePool.add(routeEnhanced);
                //If it ok update the variables
                pairSavingsPool.remove(0);
            }
            // Case (ii)
            else if (fromCustomerInStartingPoint && toCustomerInEndingPoint) {
                List<LocationNode> routeEnhanced = applyRouteOnRoute(toRoute, fromRoute);
                if (!isRouteFeasible(routeEnhanced, truckType.getTruckCapacity())) {
                    // If the capacity is not good then restore on previous route customer
                    pairSavingsPool.remove(0);
                    continue;
                }
                assignedRoutePool.remove(toRoute);
                assignedRoutePool.remove(fromRoute);
                assignedRoutePool.add(routeEnhanced);
                //If it ok update the variables
                pairSavingsPool.remove(0);
            }

            // Case (iv)
            else if (fromCustomerInStartingPoint && toCustomerInStartingPoint) {
                List<LocationNode> routeEnhanced = applySpecialRouteOnRouteOnStartingPoint(fromRoute, toRoute);
                //The route returned does not exist on assignedRoutePool as it is a deep copy

                if (!isRouteFeasible(routeEnhanced, truckType.getTruckCapacity())) {
                    // If the capacity is not good then restore on previous route customer
                    // No mutation in applySpecialRouteOnRouteOnEdges because inside we used deep copies
                    pairSavingsPool.remove(0);
                    continue;
                }

                assignedRoutePool.remove(toRoute);
                assignedRoutePool.remove(fromRoute);
                assignedRoutePool.add(routeEnhanced); //It is a deep copy
                //If it ok update the variables
                pairSavingsPool.remove(0);
            }
            else if(fromCustomerInEndingPoint && toCustomerInEndingPoint) {
                List<LocationNode> routeEnhanced = applySpecialRouteOnRouteOnEndingPoint(fromRoute, toRoute);
                if (!isRouteFeasible(routeEnhanced, truckType.getTruckCapacity())) {
                    // If the capacity is not good then restore on previous route customer
                    // No mutation in applySpecialRouteOnRouteOnEdges because inside we used deep copies
                    pairSavingsPool.remove(0);
                    continue;
                }
                assignedRoutePool.remove(toRoute);
                assignedRoutePool.remove(fromRoute);
                assignedRoutePool.add(routeEnhanced); //It is a deep copy
                //If it ok update the variables
                pairSavingsPool.remove(0);
            }
            else {
                throw new RuntimeException("Unsupported case");
            }
        }

        List<Route> routesPopulated = populateRoutes(assignedRoutePool);
        long solutionCost = calculateSolutionCost(routesPopulated);

        Solution solution = new Solution(routesPopulated, solutionCost);
        return solution;
    }

    /**
     * Converts each raw node list into a {@code Route} object with a
     * sequential id and a computed cost.
     */
    private List<Route> populateRoutes(List<List<LocationNode>> routesToPopulate) {

        int routeId = 0;
        List<Route> routeList = new ArrayList<>();

        for (List<LocationNode> locationNodes : routesToPopulate) {
            long routeCost = calculateRouteCost(locationNodes);
            Route routeToAdd = new Route(routeId, locationNodes, routeCost);
            routeList.add(routeToAdd);
            routeId ++;
        }
        return routeList;
    }

    /**
     * Returns true if a customer occupies either the starting or the ending
     * position of a route.
     */
    private boolean customerIsOnEdge(boolean isOnStartingPoint, boolean isOnEndingPoint) {
        return (isOnStartingPoint || isOnEndingPoint);
    }

    /**
     * Returns true if the given customer is the first customer (index 1,
     * right after the depot) in the route. Throws {@code IllegalStateException}
     * if the route has fewer than 3 nodes (i.e., does not at least contain
     * depot-customer-depot).
     */
    private boolean customerIsInStartingPoint(List<LocationNode> nodes, LocationNode customer) {

        if (nodes.size() < 3) {
            throw new IllegalStateException("Route must contain depot, customer(s), depot");
        }

        return nodes.get(1).equals(customer);
    }

    /**
     * Returns true if the given customer is the last customer (second-to-last
     * node, right before the closing depot) in the route.
     */
    private boolean customerIsInEndingPoint(List<LocationNode> route, LocationNode customerToCheck) {
        LocationNode endingRouteCustomer = route.get(route.size() - 2);
        return endingRouteCustomer.equals(customerToCheck);
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
     * Merges two routes for the case where the first route's ending customer
     * connects to the second route's starting customer. Creates deep copies,
     * removes the closing depot from the first route, then appends the second
     * route's nodes (excluding its opening depot) to it. Returns the merged
     * route without mutating the originals.
     */
    private List<LocationNode> applyRouteOnRoute(List<LocationNode> route, List<LocationNode> routeToAdd) {

        List<LocationNode> routeDeepCopy = new ArrayList<>(route);

        List<LocationNode> routeToAddDeepCopy = new ArrayList<>(routeToAdd);

        routeDeepCopy.remove(routeDeepCopy.size() - 1);
        routeDeepCopy.addAll(routeToAddDeepCopy.subList(1, routeToAddDeepCopy.size()));

        return routeDeepCopy;
    }

    /**
     * Merges two routes for the case where both connecting customers are at the
     * ending point of their respective routes. Deep copies both routes, strips
     * the closing depot from the first, strips the closing depot from the
     * second and reverses it, then appends the reversed second route onto the
     * first, effectively "unfolding" one route.
     */
    private List<LocationNode> applySpecialRouteOnRouteOnEndingPoint(List<LocationNode> route, List<LocationNode> routeToInverse) {
        //If we have (a) 0-A-B-0 and (b) 0-C-D-0
        // iii) With pair (A,C) OR (C,A) => UNFOLD 0-B-A-C-D-0 OR 0-D-C-A-B-0 -- it is the opposite trip with different order -- Both A, C startingPoints
        // iv)  With pair (B,D) OR (D, B) => UNFOLD 0-C-D-B-A-0 OR 0-A-B-D-C-0 it is the opposite trip with different order -- Both B, D endingPoints

        //Make deepCopies for both
        List<LocationNode> routeDeepCopy = new ArrayList<>(route);
        List<LocationNode> routeToInverseDeepCopy = new ArrayList<>(routeToInverse);

        // Get the route 0-A-B-0 and remove depot from end
        routeDeepCopy.remove(routeDeepCopy.size() - 1);

        // Now we have 0-A-B-
        // Get the route 0-C-D-0
        // Remove depot from start

        routeToInverseDeepCopy.remove(routeToInverseDeepCopy.size() - 1);
        //Now we have 0-C-D-
        //Reverse
        Collections.reverse(routeToInverseDeepCopy);
        //Now we have D-C-0

        //Merge the nodes by using a deep copy => 0-A-B-D-C-0
        routeDeepCopy.addAll(routeToInverseDeepCopy);
        return routeDeepCopy;
    }

    /**
     * Merges two routes for the case where both connecting customers are at the
     * starting point of their respective routes. Deep copies both routes,
     * strips the opening depot from the first, strips the opening depot from
     * the second and reverses it, then appends the first route onto the
     * reversed second route.
     */
    private List<LocationNode> applySpecialRouteOnRouteOnStartingPoint(List<LocationNode> route, List<LocationNode> routeToInverse) {
        //If we have (a) 0-A-B-0 and (b) 0-C-D-0
        // iii) With pair (A,C) OR (C,A) => UNFOLD 0-B-A-C-D-0 OR 0-D-C-A-B-0 -- it is the opposite trip with different order -- Both A, C startingPoints
        //Make deepCopies for both

        List<LocationNode> routeDeepCopy = new ArrayList<>(route);

        List<LocationNode> routeToInverseDeepCopy = new ArrayList<>(routeToInverse);

        // Get the route 0-A-B-0 and remove depot from start
        routeDeepCopy.remove(0);

        // Now we have -A-B-0
        // Get the route 0-C-D-0
        // Remove depot from end
        routeToInverseDeepCopy.remove(0);
        //Now we have -C-D-0
        //Reverse
        Collections.reverse(routeToInverseDeepCopy);
        //Now we have 0-D-C

        //Merge the nodes by using a deep copy => 0-D-C-A-B-0
        routeToInverseDeepCopy.addAll(routeDeepCopy);
        return routeToInverseDeepCopy;
    }

    /**
     * Finds and returns the single route (from the candidate list) that
     * contains the given customer. Throws a {@code RuntimeException} if more
     * than one route contains the customer, or if no route contains the
     * customer.
     */
    private List<LocationNode> getRouteForCustomer(List<List<LocationNode>> candidateRoutes, LocationNode customerToFilter) {
        List<List<LocationNode>> routeList = candidateRoutes
                .stream()
                .filter(route -> customerExistInRoute(route, customerToFilter))
                .collect(Collectors.toList());

        if (routeList.size() > 1) {
            throw new RuntimeException("Only one route per customer is allowed");
        }

        if (routeList.size() == 0) {
            throw new RuntimeException("Each customer must have at least one route");
        }

        return routeList.get(0);
    }

    /**
     * Returns true if the given customer is present in the given route's node
     * list.
     */
    private boolean customerExistInRoute(List<LocationNode> routeList, LocationNode customerToFilter) {
        return routeList.contains(customerToFilter);
    }

    /**
     * Builds and returns the initial set of candidate routes, one per
     * customer, each in the form depot-customer-depot.
     */
    private List<List<LocationNode>> generateCandidateRoutes(List<LocationNode> customers, LocationNode depot) {

        // We want one route for each customer.
        // Each Route also starts at depot and finish at depot
        List<List<LocationNode>> candidateRoutes = new ArrayList<>();
        for (LocationNode customer : customers) {

            List<LocationNode> routeNodes = new ArrayList<>();
            routeNodes.add(depot);
            routeNodes.add(customer);
            routeNodes.add(depot);
            candidateRoutes.add(routeNodes);
        }

        return candidateRoutes;
    }

    /**
     * Orchestrates savings computation: generates all customer pairs, computes
     * each pair's savings cost relative to the depot, sorts the pairs by
     * descending savings, and returns the sorted list.
     */
    private List<Pair> calculatePairSavings(List<LocationNode> locationNodeList, LocationNode depot) {

        List<Pair> pairs = calculatePairs(locationNodeList);

        calculatePairSavingCost(pairs, depot);

        sortPairs(pairs);

        return pairs;
    }

    /**
     * Generates every unique unordered pair of customers from the given list
     * (no depot involved) and wraps each in a {@code Pair} object.
     */
    private List<Pair> calculatePairs(List<LocationNode> locationNodeList) {

        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < locationNodeList.size() - 1; i++) {
            for (int j = i + 1; j < locationNodeList.size(); j++ ) {
                Pair pair = new Pair(locationNodeList.get(i), locationNodeList.get(j));
                pairs.add(pair);
            }
        }
        return pairs;
    }

    /**
     * Sorts the given list of pairs in place by descending savings cost
     * (highest savings first).
     */
    private void sortPairs(List<Pair> pairs) {
        pairs.sort(Comparator.
                comparing(Pair::getSavingsCost)
                .reversed());
    }

    /**
     * For each pair, computes the Clarke &amp; Wright savings value:
     * (distance from depot to fromNode) + (distance from depot to toNode) -
     * (distance between fromNode and toNode), and stores it on the {@code Pair}.
     */
    private void calculatePairSavingCost(List<Pair> pairs, LocationNode depot) {
        for (Pair pair : pairs) {
            long costDepotFromNode = calculateDistanceBetweenNodes(depot, pair.getFromNode());
            long costDepotToNode = calculateDistanceBetweenNodes(depot, pair.getToNode());
            long costFromNodeToNode = calculateDistanceBetweenNodes(pair.getFromNode(), pair.getToNode());
            long savings = costDepotFromNode + costDepotToNode - costFromNodeToNode;
            pair.setSavingsCost(savings);
        }
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



}
