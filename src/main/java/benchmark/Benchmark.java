package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

public class Benchmark {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final long[] MST = {147970,30363,468911,97589,1486139,309550,4715515,968517,15012094,3056681};
    private static final long[] SCC = {27,1,137,1,373,1,1188,1,3923,1};
    private static final long[] DIST = {396274,252411,1271911,723157,3941828,2381342,12551891,7376612,40096539,23512087};

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        Files.createDirectories(Path.of("results"));
        Path temp = Path.of("results/results.tmp");
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(temp))) {
            csv.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            int index = 0;
            for (int n : SIZES) {
                for (int density : new int[]{4, 20}) {
                    int m = n * density;
                    String label = density == 4 ? "sparse" : "dense";
                    Graph graph = GraphGenerator.generate(n, m, "undirected");
                    long kruskal = measure("Kruskal", graph, label, MST[index], csv);
                    long prim = measure("Prim", graph, label, MST[index], csv);
                    require(kruskal == prim, "MST algorithms disagree");
                    graph = GraphGenerator.generate(n, m, "directed");
                    long scc = measure("Kosaraju", graph, label, SCC[index], csv);
                    graph = GraphGenerator.generate(n, m, "dag");
                    measure("Kahn", graph, label, n, csv);
                    long distance = measure("DagShortestPath", graph, label, DIST[index], csv);
                    System.out.printf("%d / %d : MST=%d SCC=%d DAG sum=%d [PASS]%n", n,m,kruskal,scc,distance);
                    csv.flush();
                    index++;
                }
            }
        }
        Files.move(temp, Path.of("results/results.csv"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("50 rows written; every reference value and invariant passed.");
    }

    private static long measure(String algorithm, Graph graph, String density, long expected, PrintWriter csv) {
        double[] times = new double[5];
        Metrics first = null;
        long result = 0;
        for (int run = 0; run < 5; run++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            MstResult tree = null;
            int[] order = null;
            switch (algorithm) {
                case "Kruskal": tree = Kruskal.run(graph, metrics); result = tree.weight; break;
                case "Prim": tree = Prim.run(graph, metrics); result = tree.weight; break;
                case "Kosaraju": result = Kosaraju.run(graph, metrics).count; break;
                case "Kahn": order = Kahn.run(graph, metrics); result = order.length; break;
                case "DagShortestPath": result = DagShortestPath.run(graph, metrics).sum(); break;
                default: throw new IllegalArgumentException("Unknown algorithm");
            }
            times[run] = (System.nanoTime() - start) / 1_000_000.0;
            require(result == expected, algorithm + " n=" + graph.n + " expected=" + expected + " actual=" + result);
            if (tree != null) require(tree.components == 1 && tree.edges.size() == graph.n - 1, "Invalid tree");
            if (order != null) require(Kahn.isValid(graph, order), "Invalid topological order");
            long m = graph.edges.size();
            if (algorithm.equals("Kosaraju")) require(metrics.edgeScans == 2*m, "SCC scan count");
            if (algorithm.equals("Kahn") || algorithm.equals("DagShortestPath")) require(metrics.edgeScans == m, "DAG scan count");
            if (first == null) first = metrics;
            else require(first.dsuOps() == metrics.dsuOps() && first.edgeScans == metrics.edgeScans
                    && first.comparisons == metrics.comparisons && first.maxDepth == metrics.maxDepth,
                    "Counters changed between runs");
        }
        Arrays.sort(times);
        String kind = algorithm.equals("Kruskal") || algorithm.equals("Prim") ? "undirected" : algorithm.equals("Kosaraju") ? "directed" : "dag";
        csv.printf("%s,%s_%s,%d,%d,%.6f,%d,%d,%d,%d,%d%n", algorithm,kind,density,
                graph.n,graph.edges.size(),times[2],first.dsuOps(),first.edgeScans,first.comparisons,first.maxDepth,result);
        return result;
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }
}
