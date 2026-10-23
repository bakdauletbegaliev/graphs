package benchmark;

import graph.Graph;
import java.util.Random;

public class GraphGenerator {
    public static Graph generate(int n, int m, String kind) {
        if (n < 2 || m < 0) throw new IllegalArgumentException("Use n >= 2 and m >= 0");
        if (!kind.equals("undirected") && !kind.equals("directed") && !kind.equals("dag")) {
            throw new IllegalArgumentException("Unknown graph kind");
        }
        boolean undirected = kind.equals("undirected");
        if (undirected && m < n - 1) throw new IllegalArgumentException("Tree needs n - 1 edges");
        Random random = new Random(42);
        Graph graph = new Graph(n, !undirected);
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = i;
        if (undirected || kind.equals("dag")) {
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = rank[i]; rank[i] = rank[j]; rank[j] = temp;
            }
        }
        if (undirected) {
            for (int i = 1; i < n; i++) {
                int a = rank[random.nextInt(i)];
                int b = rank[i];
                graph.addEdge(a, b, 1 + random.nextInt(1000));
            }
        }
        while (graph.edges.size() < m) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            while (a == b) b = random.nextInt(n);
            if (kind.equals("dag") && rank[a] > rank[b]) {
                int temp = a; a = b; b = temp;
            }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return graph;
    }
}
