package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayDeque;

public class Kahn {
    public static int[] run(Graph graph, Metrics metrics) {
        if (!graph.directed) throw new IllegalArgumentException("Directed graph required");
        // In-degrees are stored when edges are inserted, so no extra edge pass is needed.
        int[] degree = graph.inDegrees();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < graph.n; v++) if (degree[v] == 0) queue.add(v);
        int[] order = new int[graph.n];
        int used = 0;
        while (!queue.isEmpty()) {
            int v = queue.remove();
            order[used++] = v;
            for (Edge edge : graph.adj.get(v)) {
                metrics.edgeScans++;
                degree[edge.to]--;
                if (degree[edge.to] == 0) queue.add(edge.to);
            }
        }
        if (used != graph.n) throw new IllegalArgumentException("Cycle detected: no topological order");
        return order;
    }

    public static boolean isValid(Graph graph, int[] order) {
        if (order.length != graph.n) return false;
        int[] position = new int[graph.n];
        boolean[] seen = new boolean[graph.n];
        for (int i = 0; i < order.length; i++) {
            int v = order[i];
            if (v < 0 || v >= graph.n || seen[v]) return false;
            seen[v] = true;
            position[v] = i;
        }
        for (Edge edge : graph.edges) if (position[edge.from] >= position[edge.to]) return false;
        return true;
    }
}
