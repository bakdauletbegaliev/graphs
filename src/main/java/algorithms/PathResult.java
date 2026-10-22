package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathResult {
    public final long[] distance;
    public final int[] parent;

    public PathResult(long[] distance, int[] parent) {
        this.distance = distance;
        this.parent = parent;
    }

    public List<Integer> pathTo(int vertex) {
        if (vertex < 0 || vertex >= parent.length) throw new IllegalArgumentException("Invalid vertex");
        List<Integer> path = new ArrayList<>();
        while (vertex != -1) {
            path.add(vertex);
            vertex = parent[vertex];
        }
        Collections.reverse(path);
        return path;
    }

    public long sum() {
        long total = 0;
        for (long value : distance) total += value;
        return total;
    }
}
