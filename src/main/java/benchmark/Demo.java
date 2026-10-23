package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.nio.file.Path;
import java.util.Arrays;

public class Demo {
    public static void main(String[] args) throws Exception {
        Graph graph = Graph.read(Path.of(args.length == 0 ? "data/dag.txt" : args[0]));
        if (!graph.directed) {
            MstResult k = Kruskal.run(graph, new Metrics());
            MstResult p = Prim.run(graph, new Metrics());
            System.out.println("Kruskal weight=" + k.weight + "; Prim weight=" + p.weight);
            System.out.println("Components=" + k.components + "; edges=" + k.edges);
        } else {
            SccResult scc = Kosaraju.run(graph, new Metrics());
            System.out.println("SCC count=" + scc.count + "; labels=" + Arrays.toString(scc.component));
            try {
                System.out.println("Topological order=" + Arrays.toString(Kahn.run(graph, new Metrics())));
                PathResult paths = DagShortestPath.run(graph, new Metrics());
                for (int v = 0; v < graph.n; v++) {
                    System.out.println("Vertex " + v + ": distance=" + paths.distance[v] + ", path=" + paths.pathTo(v));
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
