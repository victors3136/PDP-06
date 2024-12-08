import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Graph implements IGraph {
    private final Map<Vertex, Collection<Vertex>> inbound = new HashMap<>();
    private final Map<Vertex, Collection<Vertex>> outbound = new HashMap<>();

    public static Graph fromFile(String path) {
        final var graph = new Graph();
        final List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Could not open file " + path);
        }
        final int vertexCount = getVertexCount(lines);
        for (var vertexId = 0; vertexId < vertexCount; ++vertexId) {
            graph.inbound.put(new Vertex(String.valueOf(vertexId)), new HashSet<>());
            graph.outbound.put(new Vertex(String.valueOf(vertexId)), new HashSet<>());
        }

        for (var lineCount = 1; lineCount < lines.size(); ++lineCount) {
            final var line = lines.get(lineCount);
            final var edge = line.split(" ");
            if (edge.length != 2) {
                throw new RuntimeException("Invalid edge format at line " + lineCount + ":\n" + line);
            }
            final var src = new Vertex(edge[0]);
            final var dest = new Vertex(edge[1]);

            graph.inbound.computeIfAbsent(dest, vertex -> new HashSet<>()).add(src);
            graph.outbound.computeIfAbsent(src, vertex -> new HashSet<>()).add(dest);
        }

        return graph;
    }

    private static int getVertexCount(List<String> lines) {
        if (lines.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        final var vertexCountLine = lines.getFirst().strip();
        final int vertexCount;
        try {
            vertexCount = Integer.parseInt(vertexCountLine);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Vertex count is not a valid integer in the first line:\n" + vertexCountLine);
        }
        if (vertexCount < 1) {
            throw new RuntimeException("Vertex count is less than 1");
        }
        return vertexCount;
    }

    @Override
    public Collection<Vertex> vertices() {
        return inbound.keySet();
    }

    @Override
    public int vertexCount() {
        return inbound.size();
    }

    @Override
    public int edgeCount() {
        return outbound.values().stream().mapToInt(Collection::size).sum();
    }

    @Override
    public boolean hasEdge(Vertex src, Vertex dest) {
        return inbound.get(src).contains(dest);
    }

    @Override
    public int inboundDegree(Vertex src) {
        return inbound.get(src).size();
    }

    @Override
    public int outboundDegree(Vertex src) {
        return outbound.get(src).size();
    }

    @Override
    public Collection<Vertex> inboundNeighbors(Vertex src) {
        return inbound.get(src);
    }

    @Override
    public Collection<Vertex> outboundNeighbors(Vertex src) {
        return outbound.get(src);
    }

    @Override
    public boolean hasVertex(Vertex vertex) {
        return vertices().contains(vertex);
    }

    @Override
    public String toString() {
        return outbound.entrySet().stream().map(
                        kvp ->
                                kvp.getKey().toString() + " -> ["
                                        + kvp.getValue().stream().map(Vertex::toString)
                                        .reduce((lhs, rhs) -> lhs + ", " + rhs).orElse("") + "]")
                .reduce((lhs, rhs) -> lhs + "\n" + rhs).orElse("");
    }
}
