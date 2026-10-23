# Assignment 3 — Graph Algorithms

**Student:** Begaliev Bakdaulet  
**Group:** SE-2526  
**Repository:** https://github.com/bakdauletbegaliev/graphs

Java project for the **City Network & Job Scheduler** scenario. It finds minimum-cost road connections, detects groups of mutually dependent jobs, produces a valid job order and calculates shortest paths in a DAG.

## Requirements

- JDK 17 or newer, including the Java compiler.
- Maven 3.9 or newer.

Run all commands from the project folder containing `pom.xml`.

## Build and test

```bash
mvn clean package
```

This compiles the project, runs the JUnit 5 tests and creates `target/graphs-1.0.jar`.

To run only the tests:

```bash
mvn test
```

## Run the benchmark

```bash
java -Xmx2g -jar target/graphs-1.0.jar
```

The benchmark uses five sizes: `1000`, `3162`, `10000`, `31623` and `100000`. Each size has two densities: `m = 4n` and `m = 20n`. It generates an undirected graph for MST, a directed graph for SCC and a DAG for ordering and shortest paths. Each graph starts with `new Random(42)`.

Each algorithm runs five times. The median time and operation counters are saved in `results/results.csv`, with 50 rows in total. Before accepting results, the benchmark checks the assignment reference values, MST agreement, tree sizes, topological orders and required scan counts. A failed check stops the run.

The largest graphs may take a few minutes. Timings depend on the computer and JVM; counters are the main evidence used in the report. Sorting and priority-queue comparison counts may vary between JDK implementations.

Running the benchmark updates the CSV. The committed PNG charts and report contain the analysis of the submitted measurements; this command does not regenerate them.

## Run small examples

After building the project:

```bash
java -cp target/classes benchmark.Demo data/connected.txt
java -cp target/classes benchmark.Demo data/disconnected.txt
java -cp target/classes benchmark.Demo data/cyclic.txt
java -cp target/classes benchmark.Demo data/dag.txt
```

| Example | Expected result |
|---|---|
| `connected.txt` | Kruskal and Prim return MST weight `6` |
| `disconnected.txt` | Forest weight `4`, with `3` components |
| `cyclic.txt` | `3` SCCs; a cycle prevents topological ordering |
| `dag.txt` | Distances `[0, 0, -2, 2, -3, 0]`, with restored paths |

## Implemented algorithms

| Algorithm | Main idea |
|---|---|
| DSU | Union by size and iterative path compression |
| Kruskal | Sort edges and use DSU to avoid cycles |
| Prim | Grow a tree using a priority queue; restart for each disconnected component |
| Kosaraju | Two iterative DFS passes to find strongly connected components |
| Kahn | Use in-degrees and a queue to order vertices or detect a cycle |
| DAG shortest paths | Relax edges in topological order, starting at every zero-in-degree vertex |

The graph uses adjacency lists and supports directed and undirected edges, self-loops, parallel edges, isolated vertices and disconnected graphs. MST algorithms return a minimum spanning forest for disconnected input. Negative weights are supported by DAG shortest paths. Total weights and distances use `long`.

## Project files

| Location | Contents |
|---|---|
| `src/main/java/graph/` | Graph representation, edges and text-file input |
| `src/main/java/algorithms/` | DSU, MST, SCC, topological order and shortest paths |
| `src/main/java/metrics/` | Operation counters |
| `src/main/java/benchmark/` | Seeded graph generator, benchmark and demo |
| `src/test/java/tests/` | JUnit 5 correctness tests |
| `data/` | Four small input graphs |
| `results/results.csv` | Benchmark measurements |
| `results/plots/` | Three required PNG charts |
| `results/benchmark.log` | Recorded benchmark output |
| `results/tests.log` | Recorded JUnit output |
| `results/maven-build.log` | Recorded Maven build output |
| `REPORT.md` | Complexity analysis, correctness arguments, results and algorithm choices |
| `pom.xml` | Maven dependencies and build settings |

## Input format

```text
4 3 undirected
0 1 7
1 2 -2
2 3 4
```

The header is `n m directed|undirected`, followed by exactly `m` edges written as `from to weight`. Vertices are numbered `0` through `n-1`, and weights are integers.

MST methods require an undirected graph. Kosaraju, Kahn and DAG shortest paths require a directed graph. Kahn and DAG shortest paths throw `IllegalArgumentException` if a cycle prevents topological ordering; the demo prints the error message.

## Metrics

- `dsu_ops`: find calls plus successful unions. Parent-pointer steps are not counted as separate find calls.
- `edge_scans`: sorted candidates examined by Kruskal; adjacency entries examined by Prim; DFS edges examined by Kosaraju; outgoing edges processed by Kahn; edges relaxed by DAG shortest paths.
- `comparisons`: weight comparisons during sorting and heap operations, and candidate-distance comparisons during DAG relaxation.
- `max_depth`: maximum number of vertices stored in the explicit DFS stack.

Prim scans `2m` adjacency entries, Kosaraju scans `2m` edges across its two DFS passes, and Kahn and DAG relaxation each scan `m` edges on the generated DAGs. Reverse-graph construction is excluded from DFS scan counts. In-degrees are maintained during graph construction.

DAG shortest paths runs Kahn internally with a separate metrics object. Its measured time includes topological sorting; its reported scan count covers the relaxation pass. Plotted operations are `dsu_ops + edge_scans + comparisons`, a measure of counted work rather than every machine instruction.

## Tests

The 10 JUnit test methods cover:

- DSU against a simple component-label reference.
- Kruskal and Prim on 100 random connected graphs.
- MST weights against exhaustive subsets on 30 tiny graphs.
- SCCs against mutual reachability from all-pairs BFS on 40 random graphs with at most 200 vertices.
- DAG shortest paths against Bellman–Ford on 100 random DAGs, including negative weights and restored paths.
- All four input files, disconnected graphs, isolated vertices, self-loops, parallel edges, empty graphs and cycle detection.
- Exact traversal scan counts, a 100,000-vertex path and totals exceeding the `int` range.

## Report and plots

See [REPORT.md](REPORT.md) for complexity bounds, correctness arguments, normalized counters, fitted slopes and guidance on choosing algorithms.

The three submitted charts are:

- [Operations versus n](results/plots/operations_vs_n.png)
- [Scans per edge](results/plots/scans_per_edge.png)
- [Share of edges examined by Kruskal](results/plots/kruskal_edge_share.png)

## AI-use acknowledgement

OpenAI ChatGPT/Codex assisted with implementation, test design, benchmark execution, documentation and plots. The student is responsible for reviewing the code, understanding the algorithms and explaining the results during the defense.
