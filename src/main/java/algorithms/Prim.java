package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.PriorityQueue;

public class Prim {
    public static MstResult run(Graph graph, Metrics metrics) {
        if (graph.directed) throw new IllegalArgumentException("MST requires an undirected graph");
        boolean[] visited = new boolean[graph.n];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });
        MstResult result = new MstResult();
        for (int start = 0; start < graph.n; start++) {
            if (visited[start]) continue;
            result.components++;
            queue.add(new Edge(-1, start, 0));
            while (!queue.isEmpty()) {
                Edge best = queue.remove();
                int v = best.to;
                if (visited[v]) continue;
                visited[v] = true;
                if (best.from != -1) {
                    result.edges.add(best);
                    result.weight += best.weight;
                }
                for (Edge edge : graph.adj.get(v)) {
                    metrics.edgeScans++;
                    if (!visited[edge.to]) queue.add(edge);
                }
            }
        }
        return result;
    }
}
