package vrp.tools;


import vrp.entities.LocationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Static utility class for generating random test data for the VRP: a set of
 * randomly placed customer nodes plus a single depot node.
 */
public class Randomizer {

    private static long MAX_CUSTOMER_THRESHOLD = 1000;
    private static int MAX_HEIGHT = 1000;
    private static int MAX_WIDTH = 1000;
    private static int RANDOM_SEED = 12345;

    /**
     * Private constructor to prevent instantiation, since this is a static
     * utility class.
     */
    private Randomizer() {}

    /**
     * Generates a list of random customer {@code LocationNode} objects (up to a
     * maximum of {@code MAX_CUSTOMER_THRESHOLD}) plus one depot node appended at
     * the end. Customer coordinates are randomly offset from the center of a
     * fixed-size grid within a fixed radius, using a fixed random seed for
     * reproducible coordinates. Customer demand values are randomly generated
     * between 1000 and 5000 using a separate fixed-seed {@code Random} instance.
     *
     * @param totalCustomers number of customer nodes to generate; throws a
     *                        {@code RuntimeException} if this exceeds
     *                        {@code MAX_CUSTOMER_THRESHOLD}
     * @return the combined list of generated customers and the depot node
     */
    public static List<LocationNode> produceRandomLocationNodes(long totalCustomers) {

        if (totalCustomers > MAX_CUSTOMER_THRESHOLD) {
            throw new RuntimeException("Only up to 1000 customers are supported for generation");
        }

        List<LocationNode> locationNodeList = new ArrayList<>();
        long counter = 1;
        Random randomCoordinates = new Random(RANDOM_SEED);
        Random randomDemand = new Random(789);

        int centerX = MAX_WIDTH / 2;
        int centerY = MAX_HEIGHT / 2;
        int radius = 450; // how far customers can spread from the depot

        while (counter <= totalCustomers) {
            int offsetX = randomCoordinates.nextInt(2 * radius + 1) - radius; // range: -radius .. +radius
            int offsetY = randomCoordinates.nextInt(2 * radius + 1) - radius;

            int x = centerX + offsetX;
            int y = centerY + offsetY;

            long demand = randomDemand.nextInt(1000, 5000);
            LocationNode locationNode = new LocationNode(counter, demand, x, y, false);
            locationNodeList.add(locationNode);
            counter++;
        }

        LocationNode depot = produceRandomDepot();
        locationNodeList.add(depot);
        return locationNodeList;
    }


    /**
     * Creates and returns a single depot {@code LocationNode} positioned at the
     * center of the grid, with id 0, demand 0, and {@code isDepot} set to true.
     */
    private static LocationNode produceRandomDepot() {
        int centerX = MAX_WIDTH / 2;
        int centerY = MAX_HEIGHT / 2;
        LocationNode locationNode = new LocationNode(0, 0, centerX, centerY, true);
        return locationNode;
    }


}
