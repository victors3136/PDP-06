import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HamiltonianCycleDetector {
    private final IGraph graph;
    private final ForkJoinPool executor;
    private final AtomicBoolean found;
    private final Vertex origin;

    public HamiltonianCycleDetector(IGraph graph, Vertex origin) {
        if (!graph.hasVertex(origin)) {
            throw new IllegalArgumentException("Vertex " + origin + " does not exist");
        }
        this.graph = graph;
        this.executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        this.found = new AtomicBoolean(false);
        this.origin = origin;
    }

    public boolean detect() {
        try {
            return dfs(origin, new HashSet<>());
        } finally {
            executor.shutdown();
        }
    }

    private boolean dfs(Vertex source, Set<Vertex> visited) {
        if (found.get()) {
            return true;
        }
        if (graph.outboundNeighbors(source).contains(origin) && visited.size() == graph.vertexCount()) {
            found.set(true);
            return true;
        }
        final var futures = new ArrayList<Future<Boolean>>();
        futures.ensureCapacity(graph.outboundDegree(source));

        for (final var neighbor : graph.outboundNeighbors(source)) {
            if (visited.contains(neighbor)) {
                continue;
            }
            final var visitedSetCopy = new HashSet<>(visited);
            visitedSetCopy.add(neighbor);
            futures.add(executor.submit(() -> dfs(neighbor, visitedSetCopy)));
        }
        for (final var neighborSearch : futures) {
            try {
                if (neighborSearch.get()) {
                    return true;
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Error during parallel execution", e);
            }
        }
        return false;
    }

}
