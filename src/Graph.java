import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Graph implements IGraph {
    private final Map<Vertex, Collection<Vertex>> inbound;
    private final Map<Vertex, Collection<Vertex>> outbound;

    private Graph(Map<Vertex, Collection<Vertex>> inbound,
                  Map<Vertex, Collection<Vertex>> outbound) {
        this.inbound = inbound.entrySet().stream().map(entry -> Map.entry(
                entry.getKey(),
                new HashSet<>(entry.getValue())
        )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.outbound = outbound.entrySet().stream().map(entry -> Map.entry(
                entry.getKey(),
                new HashSet<>(entry.getValue())
        )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Graph fromFile(String path) {
        final List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Could not open file " + path);
        }
        final int vertexCount = parseAndRemoveVertexCountLine(lines);
        final var inbound = new ConcurrentHashMap<Vertex, Collection<Vertex>>();
        final var outbound = new ConcurrentHashMap<Vertex, Collection<Vertex>>();
        IntStream.range(0, vertexCount).parallel().forEach(id -> {
            inbound.put(new Vertex(String.valueOf(id)), new ConcurrentSkipListSet<>());
            outbound.put(new Vertex(String.valueOf(id)), new ConcurrentSkipListSet<>());
        });

        lines.stream().parallel().forEach(line -> {
            final var edge = line.split(" ");
            if (edge.length != 2) {
                throw new RuntimeException("Invalid edge format at line:\n" + line);
            }
            final var src = new Vertex(edge[0]);
            final var dest = new Vertex(edge[1]);
            inbound.get(dest).add(src);
            outbound.get(src).add(dest);
        });
        return new Graph(new HashMap<>(inbound), new HashMap<>(outbound));
    }

    private static int parseAndRemoveVertexCountLine(List<String> lines) {
        if (lines.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        final var vertexCountLine = lines.removeFirst().strip();
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
