package metrics;

// Each algorithm gets a new counter object for each run.
public class Metrics {
    public long findCalls;
    public long successfulUnions;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    public void recordDepth(int depth) {
        if (depth > maxDepth) maxDepth = depth;
    }
}
