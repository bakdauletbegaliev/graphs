package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.List;

public class Kruskal {
    public static MstResult run(Graph graph, Metrics metrics) {
        if (graph.directed) throw new IllegalArgumentException("MST requires an undirected graph");
        List<Edge> sorted = new ArrayList<>(graph.edges);
        sorted.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });
        DSU dsu = new DSU(graph.n, metrics);
        MstResult result = new MstResult();
        for (Edge edge : sorted) {
            if (dsu.components() == 1) break;
            metrics.edgeScans++;
            if (dsu.union(edge.from, edge.to)) {
                result.edges.add(edge);
                result.weight += edge.weight;
            }
        }
        result.components = dsu.components();
        return result;
    }
}
