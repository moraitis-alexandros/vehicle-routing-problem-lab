package vrp.cwheuristic;

import vrp.entities.LocationNode;

import java.util.Objects;

/**
 * This is a helper entity class, to store our Pair Savings
 */

public class Pair {

    private LocationNode fromNode;
    private LocationNode toNode;
    private Long savingsCost;

    /**
     * Creates a pair with its two customer nodes. The savings cost is not set
     * here and must be assigned later via {@code setSavingsCost}.
     */
    public Pair(LocationNode fromNode, LocationNode toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public LocationNode getFromNode() {
        return fromNode;
    }

    public LocationNode getToNode() {
        return toNode;
    }

    public Long getSavingsCost() {
        return savingsCost;
    }

    public void setSavingsCost(Long savingsCost) {
        this.savingsCost = savingsCost;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return savingsCost == pair.savingsCost && Objects.equals(fromNode, pair.fromNode) && Objects.equals(toNode, pair.toNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromNode, toNode, savingsCost);
    }
}
