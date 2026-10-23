package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.Arrays;

public class Kosaraju {
    public static SccResult run(Graph graph, Metrics metrics) {
        if (!graph.directed) throw new IllegalArgumentException("Directed graph required");
        int n = graph.n;
        boolean[] visited = new boolean[n];
        int[] stack = new int[n];
        int[] nextEdge = new int[n];
        int[] finish = new int[n];
        int finished = 0;
        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            int depth = 0;
            stack[depth++] = start;
            visited[start] = true;
            metrics.recordDepth(depth);
            while (depth > 0) {
                int v = stack[depth - 1];
                if (nextEdge[v] < graph.adj.get(v).size()) {
                    Edge edge = graph.adj.get(v).get(nextEdge[v]++);
                    metrics.edgeScans++;
                    if (!visited[edge.to]) {
                        visited[edge.to] = true;
                        stack[depth++] = edge.to;
                        metrics.recordDepth(depth);
                    }
                } else {
                    // Finish only after all children have finished.
                    finish[finished++] = v;
                    depth--;
                }
            }
        }
        // Construction is not counted as a DFS edge scan (assignment convention).
        Graph reverse = graph.reversed();
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int start = finish[i];
            if (component[start] != -1) continue;
            int depth = 0;
            stack[depth++] = start;
            component[start] = count;
            metrics.recordDepth(depth);
            while (depth > 0) {
                int v = stack[--depth];
                for (Edge edge : reverse.adj.get(v)) {
                    metrics.edgeScans++;
                    if (component[edge.to] == -1) {
                        component[edge.to] = count;
                        stack[depth++] = edge.to;
                        metrics.recordDepth(depth);
                    }
                }
            }
            count++;
        }
        return new SccResult(component, count);
    }
}
