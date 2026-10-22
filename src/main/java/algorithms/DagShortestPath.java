package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.Arrays;

public class DagShortestPath {
    public static PathResult run(Graph graph, Metrics metrics) {
        int[] order = Kahn.run(graph, new Metrics());
        int[] degree = graph.inDegrees();
        long[] distance = new long[graph.n];
        int[] parent = new int[graph.n];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);
        for (int v = 0; v < graph.n; v++) if (degree[v] == 0) distance[v] = 0;
        for (int v : order) {
            for (Edge edge : graph.adj.get(v)) {
                metrics.edgeScans++;
                if (distance[v] != Long.MAX_VALUE) {
                    long candidate = distance[v] + edge.weight;
                    metrics.comparisons++;
                    if (candidate < distance[edge.to]) {
                        distance[edge.to] = candidate;
                        parent[edge.to] = v;
                    }
                }
            }
        }
        return new PathResult(distance, parent);
    }
}
