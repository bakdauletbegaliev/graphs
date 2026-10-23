package graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Graph {
    public final int n;
    public final boolean directed;
    public final List<List<Edge>> adj = new ArrayList<>();
    public final List<Edge> edges = new ArrayList<>();
    private final int[] inDegree;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("Negative vertex count");
        this.n = n;
        this.directed = directed;
        inDegree = new int[n];
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adj.get(from).add(edge);
        inDegree[to]++;
        if (!directed) {
            adj.get(to).add(new Edge(to, from, weight));
            inDegree[from]++;
        }
    }

    public void checkVertex(int v) {
        if (v < 0 || v >= n) throw new IllegalArgumentException("Invalid vertex: " + v);
    }

    public int[] inDegrees() {
        return inDegree.clone();
    }

    public Graph reversed() {
        if (!directed) throw new IllegalArgumentException("Directed graph required");
        Graph reverse = new Graph(n, true);
        for (Edge e : edges) reverse.addEdge(e.to, e.from, e.weight);
        return reverse;
    }

    public static Graph read(Path path) throws IOException {
        try (Scanner input = new Scanner(path)) {
            int n = input.nextInt();
            int m = input.nextInt();
            String kind = input.next();
            if (m < 0 || (!kind.equals("directed") && !kind.equals("undirected"))) {
                throw new IllegalArgumentException("Invalid graph header");
            }
            Graph graph = new Graph(n, kind.equals("directed"));
            for (int i = 0; i < m; i++) {
                graph.addEdge(input.nextInt(), input.nextInt(), input.nextInt());
            }
            if (input.hasNext()) throw new IllegalArgumentException("Extra input after edges");
            return graph;
        }
    }
}
