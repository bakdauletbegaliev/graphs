# Assignment 3 - Graphs

**Student:** Begaliev Bakdaulet  
**Group:** SE-2526  
**Repository:** https://github.com/bakdauletbegaliev/graphs  
**Release:** main / v1.0

A simple Java project for the City Network & Job Scheduler scenario. Algorithms use ordinary classes, arrays, loops and Java collections. No external graph library is used.

## Quick start

Requirements: **JDK 17 or newer** (not only a JRE), Maven 3.9+, and optionally Python 3 with `numpy`, `matplotlib`, `reportlab` for regenerating the charts/report. Run commands from this directory, which contains `pom.xml`.

```bash
mvn clean package
java -Xmx2g -jar target/graphs-1.0.jar
```

The first command compiles the project and runs all JUnit 5 tests. The second runs the complete benchmark (5 measurements per algorithm, 50 CSV rows), checks all reference values, and writes `results/results.csv`. Allow a few minutes for the largest graphs. `-Xmx2g` allows a 2 GB heap.

Tests only:

```bash
mvn test
```

Small readable examples:

```bash
java -cp target/classes benchmark.Demo data/connected.txt
java -cp target/classes benchmark.Demo data/disconnected.txt
java -cp target/classes benchmark.Demo data/cyclic.txt
java -cp target/classes benchmark.Demo data/dag.txt
```

Expected: connected MST weight 6; disconnected forest weight 4 with 3 components; cyclic graph has 3 SCCs and no topological order; DAG distances `[0, 0, -2, 2, -3, 0]`. The demo also prints restored paths.

## Reproduce the full submission

Install the Python dependencies once:

```bash
python -m pip install -r scripts/requirements.txt
```

Then on Windows PowerShell:

```powershell
.\reproduce.ps1
```

Or on Linux / Git Bash with Maven and Python available:

```bash
bash reproduce.sh
```

These commands run tests, benchmark, and regenerate all three PNG charts plus `REPORT.md` and the five-page `REPORT.pdf`. Time measurements will change. Result values and traversal counters must match. Sort/heap comparison counts can depend on JDK collection implementations; use the same JDK for exact comparisons. The checked-in measurements were taken on OpenJDK 17.0.20, Linux amd64. JUnit was executed directly and through Maven; this environment used the Eclipse Java compiler (ECJ) targeting Java 17 because it only supplied a JRE. The standard project uses Maven's default javac compiler on your JDK.

## Files and reading order

| File / folder | Purpose |
|---|---|
| `src/main/java/graph/Graph.java`, `Edge.java` | One adjacency-list graph for both graph types; text input |
| `src/main/java/algorithms/DSU.java` | Union by size + iterative path compression |
| `Kruskal.java`, `Prim.java` in algorithms | Minimum spanning trees / forests |
| `Kosaraju.java`, `Kahn.java` in algorithms | Iterative SCC search and topological ordering |
| `DagShortestPath.java`, `PathResult.java` in algorithms | Multi-source shortest paths and path restoration |
| `src/main/java/metrics/Metrics.java` | Counters updated inside the algorithms |
| `src/main/java/benchmark/` | Exact seeded generator, self-checking benchmark, small demo |
| `src/test/java/tests/GraphAlgorithmsTest.java` | 10 JUnit methods containing many randomized cases |
| `data/` | Four example graph files, all used in tests |
| `results/results.csv` | 50 actual benchmark rows, median of five timings |
| `results/benchmark.log`, `tests.log`, `maven-build.log` | Execution evidence |
| `results/plots/` | Three PNG charts |
| `REPORT.md`, `REPORT.pdf` | Analysis and five-page report |
| `DEFENSE_RU.md` | Short Russian explanations for defense |
| `scripts/make_report.py` | Regenerates charts and report from the measured CSV |

## Input format

```text
4 3 undirected
0 1 7
1 2 -2
2 3 4
```

Header: `n m directed|undirected`. Then exactly `m` lines: `from to weight`. Vertices are `0..n-1`. Loops, parallel edges, isolated vertices and disconnected graphs are supported. MST methods require an undirected graph; SCC/Kahn/paths require a directed graph. Kahn and DAG paths report a cycle via `IllegalArgumentException`. Distances and total weights are `long`, although individual weights are `int`.

## Counter definitions

- `dsu_ops = findCalls + successfulUnions`. A find call counts once; internal parent-pointer steps are not separate find calls. A failed union does not increment successfulUnions, but its two finds do count.
- `edge_scans`: Kruskal's sorted candidates up to early termination; Prim's adjacency entries (2m); Kosaraju's two DFS passes (2m); Kahn's outgoing adjacency scan (m); DAG relaxation edges (m).
- Graph construction/reversal and order validation are excluded from traversal scan counters. In-degrees are stored on insertion. DAG paths compute Kahn internally with a separate metrics object; the reported time includes that work, while its `edge_scans` measures only relaxation as required.
- `comparisons`: weight comparisons in sort/heap; distance comparisons in DAG relaxation. Other boolean checks are not included.
- `max_depth`: maximum occupied size of the explicit DFS stack, across both SCC passes; zero for algorithms without a DFS stack.
- Plotted operations = `dsu_ops + edge_scans + comparisons`; this is a proxy for counted work, not a count of every machine instruction.

The generator restarts `new Random(42)` for each graph. When endpoints coincide, it redraws **b only**, keeping a fixed. This interpretation reproduces every reference value. Both `4n` and `20n` are linear-density families asymptotically; “dense” is the assignment's relative label.

## Tests

DSU vs component labels; 100 random connected MST cases; 30 tiny MST cases vs exhaustive edge subsets; 40 SCC cases vs all-pairs BFS up to 200 vertices; 100 DAG cases vs Bellman-Ford with negative weights and restored-path checks; all four input files; isolated/empty/self-loop graphs; cycle detection; exact scan counters; a 100,000-vertex DFS path; totals beyond int range.

## Git history and publication

The archive includes `graphs-history.bundle` with the real commits, main, all five required feature branches and tag v1.0. It is a local repository history; this package does not claim that it has already been pushed to GitHub. Commit author is OpenAI Codex, with the student identified above and in the report.

To restore the history, run from the extracted folder:

```bash
git clone graphs-history.bundle ../graphs-git
cd ../graphs-git
git remote rename origin bundle
git remote add origin https://github.com/bakdauletbegaliev/graphs.git
git branch feature/dsu bundle/feature/dsu
git branch feature/mst bundle/feature/mst
git branch feature/scc bundle/feature/scc
git branch feature/paths bundle/feature/paths
git branch feature/metrics bundle/feature/metrics
git push -u origin main
git push origin feature/dsu feature/mst feature/scc feature/paths feature/metrics
git push origin v1.0
```

Sign in to GitHub if prompted. Do not force-push over existing work. Inspect `git log --oneline --all` and `git branch -a` before submitting. Upload the ZIP to Moodle after confirming the GitHub main branch and v1.0 tag are visible.

## Scope and AI acknowledgement

All mandatory algorithms are included. Optional Tarjan/condensation bonus tasks are not implemented, to keep the project focused and understandable. The PDF's detailed benchmark requirement (five sizes x two densities x five algorithm rows = 50) takes precedence over its later inconsistent “four generated graphs” wording.

OpenAI ChatGPT/Codex assisted with implementation, tests, measurements, documentation and plots. This acknowledgement does not imply that the student has independently written or already mastered every line. Before defense, review `DEFENSE_RU.md`, run the examples, and explain the algorithms in your own words.
