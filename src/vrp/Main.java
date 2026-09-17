package vrp;

import vrp.cwheuristic.ClarkNWrightAlgorithm;
import vrp.entities.LocationNode;
import vrp.entities.Solution;
import vrp.entities.Truck;
import vrp.nnheuristic.NNAlgorithm;
import vrp.postoptimize.InsertionHeuristic;
import vrp.tools.Randomizer;
import vrp.tools.SolutionDesigner;

import java.util.List;

/**
 * Entry point that wires together the VRP algorithms to generate random test data,
 * build solutions with different heuristics, and visualize the results.
 */
public class Main {

    /**
     * Generates random location nodes and a truck definition, then runs the Nearest
     * Neighbor algorithm and the Clark &amp; Wright savings algorithm to build initial
     * solutions. Visualizes both solutions in separate windows, then applies the
     * Insertion Heuristic as a post-optimization step on the Nearest Neighbor solution
     * and visualizes the improved result. Prints the cost and route count for each
     * solution to standard output.
     */
    public static void main(String[] args) {


        List<LocationNode> locationNodeList = Randomizer.produceRandomLocationNodes(20);
        Truck truckType = new Truck(20, 10_000, 1);

        NNAlgorithm nnAlgorithm = new NNAlgorithm();
        Solution nnSolution = nnAlgorithm.solve(locationNodeList, truckType);
        System.out.println("NN Solution Cost: " + nnSolution.getSolutionCost() + " Routes Number: " + nnSolution.getSolutionRoutes().size());

        ClarkNWrightAlgorithm clarkNWrightAlgorithm = new ClarkNWrightAlgorithm();
        Solution cwSolution = clarkNWrightAlgorithm.solve(locationNodeList, truckType);
        System.out.println("C&W Solution Cost: " + cwSolution.getSolutionCost() + " Routes Number: " + cwSolution.getSolutionRoutes().size());

        SolutionDesigner designer = new SolutionDesigner();
        designer.drawSolution(nnSolution, locationNodeList, "NN Algorithm");

        SolutionDesigner designer2 = new SolutionDesigner();
        designer2.drawSolution(cwSolution, locationNodeList, "C&W Algorithm");

        InsertionHeuristic insertionHeuristic = new InsertionHeuristic();
        Solution insertionSolution = insertionHeuristic.solve(nnSolution, locationNodeList, truckType);
        System.out.println("Insertion Solution Cost: " + insertionSolution.getSolutionCost() + " Routes Number: " + insertionSolution.getSolutionRoutes().size());

        SolutionDesigner designer3 = new SolutionDesigner();
        designer3.drawSolution(insertionSolution, locationNodeList, "Insertion Post Algorithm");
    }
}