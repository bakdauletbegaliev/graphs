package tests;

import algorithms.*;
import benchmark.GraphGenerator;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class git add src/main/java/algorithms/PathResult.java
git add src/main/java/algorithms/DagShortestPath.java

GraphAlgorithmsTest {
    @Test
    void dsuMatchesSimpleLabels() {
        Random random = new Random(17);
        Metrics metrics = new Metrics();
        DSU dsu = new DSU(80, metrics);
        int[] label = new int[80];
        for (int i = 0; i < 80; i++) label[i] = i;
        int sets = 80;
        for (int step = 0; step < 1000; step++) {
            int a = random.nextInt(80), b = random.nextInt(80);
            boolean changed = label[a] != label[b];
            assertEquals(changed, dsu.union(a, b));
            if (changed) {
                int old = label[b], replacement = label[a];
                for (int i = 0; i < 80; i++) if (label[i] == old) label[i] = replacement;
                sets--;
            }
            assertEquals(sets, dsu.components());
            for (int i = 0; i < 80; i++) {
                for (int j = 0; j < 80; j++) assertEquals(label[i] == label[j], dsu.find(i) == dsu.find(j));
            }
        }
        assertEquals(80 - sets, metrics.successfulUnions);
        assertTrue(metrics.findCalls > 0);
        dsu.makeSet(3);
        assertEquals(3, dsu.components());
        assertNotEquals(dsu.find(0), dsu.find(1));
    }

    @Test
    void oneHundredConnectedMstCrossChecks() {
        Random random = new Random(7);
        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(45);
            Graph graph = new Graph(n, false);
            for (int v = 1; v < n; v++) graph.addEdge(v, random.nextInt(v), random.nextInt(41) - 20);
            for (int i = 0; i < n*4; i++) graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(41) - 20);
            MstResult k = Kruskal.run(graph, new Metrics());
            MstResult p = Prim.run(graph, new Metrics());
            assertEquals(k.weight, p.weight);
            assertTree(graph, k);
            assertTree(graph, p);
        }
    }

    private void assertTree(Graph graph, MstResult tree) {
        assertEquals(graph.n - 1, tree.edges.size());
        DSU check = new DSU(graph.n, new Metrics());
        long weight = 0;
        for (Edge e : tree.edges) {
            assertTrue(check.union(e.from, e.to));
            weight += e.weight;
        }
        assertEquals(1, check.components());
        assertEquals(1, tree.components);
        assertEquals(weight, tree.weight);
    }

    @Test
    void mstMatchesExhaustiveSubsets() {
        Random random = new Random(21);
        for (int test = 0; test < 30; test++) {
            Graph graph = new Graph(5, false);
            for (int v = 1; v < 5; v++) graph.addEdge(v-1, v, random.nextInt(20)-10);
            for (int i = 0; i < 5; i++) graph.addEdge(random.nextInt(5), random.nextInt(5), random.nextInt(20)-10);
            long best = Long.MAX_VALUE;
            for (int mask = 0; mask < (1 << graph.edges.size()); mask++) {
                if (Integer.bitCount(mask) != 4) continue;
                DSU check = new DSU(5, new Metrics());
                long total = 0;
                boolean tree = true;
                for (int i = 0; i < graph.edges.size(); i++) if ((mask & (1 << i)) != 0) {
                    Edge e = graph.edges.get(i);
                    if (!check.union(e.from, e.to)) tree = false;
                    total += e.weight;
                }
                if (tree && check.components() == 1) best = Math.min(best, total);
            }
            assertEquals(best, Kruskal.run(graph, new Metrics()).weight);
            assertEquals(best, Prim.run(graph, new Metrics()).weight);
        }
    }

    @Test
    void sccMatchesAllPairsBfs() {
        Random random = new Random(23);
        for (int test = 0; test < 40; test++) {
            int n = 1 + random.nextInt(200);
            Graph graph = new Graph(n, true);
            for (int i = 0; i < n*3; i++) graph.addEdge(random.nextInt(n), random.nextInt(n), 1);
            boolean[][] reachable = new boolean[n][n];
            for (int start = 0; start < n; start++) {
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                reachable[start][start] = true;
                queue.add(start);
                while (!queue.isEmpty()) {
                    int v = queue.remove();
                    for (Edge e : graph.adj.get(v)) if (!reachable[start][e.to]) {
                        reachable[start][e.to] = true;
                        queue.add(e.to);
                    }
                }
            }
            SccResult result = Kosaraju.run(graph, new Metrics());
            boolean[] labels = new boolean[result.count];
            for (int a = 0; a < n; a++) {
                labels[result.component[a]] = true;
                for (int b = 0; b < n; b++) {
                    assertEquals(reachable[a][b] && reachable[b][a], result.component[a] == result.component[b]);
                }
            }
            for (boolean used : labels) assertTrue(used);
        }
    }

    @Test
    void shortestPathsMatchBellmanFordIncludingNegativeWeights() {
        Random random = new Random(31);
        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(40);
            Graph graph = new Graph(n, true);
            for (int i = 0; i < n*4; i++) {
                int a = random.nextInt(n), b = random.nextInt(n);
                if (a == b) continue;
                graph.addEdge(Math.min(a,b), Math.max(a,b), random.nextInt(41)-20);
            }
            long[] expected = new long[n];
            Arrays.fill(expected, Long.MAX_VALUE);
            int[] degree = graph.inDegrees();
            for (int v = 0; v < n; v++) if (degree[v] == 0) expected[v] = 0;
            for (int pass = 0; pass < n-1; pass++) {
                for (Edge e : graph.edges) if (expected[e.from] != Long.MAX_VALUE) {
                    expected[e.to] = Math.min(expected[e.to], expected[e.from] + e.weight);
                }
            }
            PathResult actual = DagShortestPath.run(graph, new Metrics());
            assertArrayEquals(expected, actual.distance);
            for (int v = 0; v < n; v++) {
                List<Integer> path = actual.pathTo(v);
                assertEquals(0, degree[path.get(0)]);
                assertEquals(v, path.get(path.size()-1).intValue());
                assertTrue(path.size() <= n);
                for (int i = 1; i < path.size(); i++) {
                    int from = path.get(i-1), to = path.get(i);
                    boolean validEdge = false;
                    for (Edge e : graph.adj.get(from)) {
                        if (e.to == to && actual.distance[from] + e.weight == actual.distance[to]) validEdge = true;
                    }
                    assertTrue(validEdge);
                }
            }
        }
    }

    @Test
    void inputFilesAndEdgeCases() throws Exception {
        Graph connected = Graph.read(Path.of("data/connected.txt"));
        assertEquals(6, Kruskal.run(connected, new Metrics()).weight);
        assertEquals(6, Prim.run(connected, new Metrics()).weight);
        Graph disconnected = Graph.read(Path.of("data/disconnected.txt"));
        MstResult k = Kruskal.run(disconnected, new Metrics());
        MstResult p = Prim.run(disconnected, new Metrics());
        assertEquals(4, k.weight);
        assertEquals(k.weight, p.weight);
        assertEquals(3, k.components);
        assertEquals(3, p.components);
        assertEquals(3, k.edges.size());
        Graph cyclic = Graph.read(Path.of("data/cyclic.txt"));
        assertEquals(3, Kosaraju.run(cyclic, new Metrics()).count);
        assertThrows(IllegalArgumentException.class, () -> Kahn.run(cyclic, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(cyclic, new Metrics()));
        Graph dag = Graph.read(Path.of("data/dag.txt"));
        assertTrue(Kahn.isValid(dag, Kahn.run(dag, new Metrics())));
        assertArrayEquals(new long[]{0,0,-2,2,-3,0}, DagShortestPath.run(dag, new Metrics()).distance);
    }

    @Test
    void emptyIsolatedAndSelfLoopGraphs() {
        assertEquals(0, Kruskal.run(new Graph(0,false), new Metrics()).components);
        assertEquals(0, Prim.run(new Graph(0,false), new Metrics()).weight);
        assertEquals(0, Kosaraju.run(new Graph(0,true), new Metrics()).count);
        assertEquals(0, DagShortestPath.run(new Graph(0,true), new Metrics()).sum());
        assertEquals(4, Prim.run(new Graph(4,false), new Metrics()).components);
        assertEquals(4, Kosaraju.run(new Graph(4,true), new Metrics()).count);
        Graph loop = new Graph(1,true);
        loop.addEdge(0,0,-1);
        assertEquals(1, Kosaraju.run(loop,new Metrics()).count);
        assertThrows(IllegalArgumentException.class, () -> Kahn.run(loop,new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> Prim.run(loop,new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> loop.addEdge(-1,0,1));
    }

    @Test
    void topologicalOrderValidationAndExactCounters() {
        for (int density : new int[]{4,20}) {
            Graph dag = GraphGenerator.generate(1000,1000*density,"dag");
            Metrics k = new Metrics(), d = new Metrics(), s = new Metrics();
            assertTrue(Kahn.isValid(dag,Kahn.run(dag,k)));
            DagShortestPath.run(dag,d);
            Kosaraju.run(dag,s);
            assertEquals(dag.edges.size(),k.edgeScans);
            assertEquals(dag.edges.size(),d.edgeScans);
            assertEquals(2L*dag.edges.size(),s.edgeScans);
            assertFalse(Kahn.isValid(dag,new int[dag.n]));
        }
    }

    @Test
    void iterativeDfsSurvivesOneHundredThousandVertices() {
        Graph path = new Graph(100000,true);
        for (int v = 1; v < path.n; v++) path.addEdge(v-1,v,1);
        Metrics metrics = new Metrics();
        assertEquals(path.n, Kosaraju.run(path,metrics).count);
        assertEquals(100000,metrics.maxDepth);
        assertEquals(199998,metrics.edgeScans);
    }

    @Test
    void distanceAndMstTotalsUseLong() {
        Graph dag = new Graph(3,true);
        dag.addEdge(0,1,Integer.MAX_VALUE);
        dag.addEdge(1,2,Integer.MAX_VALUE);
        assertEquals(2L*Integer.MAX_VALUE,DagShortestPath.run(dag,new Metrics()).distance[2]);
        Graph graph = new Graph(3,false);
        graph.addEdge(0,1,Integer.MAX_VALUE);
        graph.addEdge(1,2,Integer.MAX_VALUE);
        assertEquals(2L*Integer.MAX_VALUE,Kruskal.run(graph,new Metrics()).weight);
        assertEquals(2L*Integer.MAX_VALUE,Prim.run(graph,new Metrics()).weight);
    }
}
