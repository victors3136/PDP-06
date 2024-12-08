public class Main {
    private static void check(String path, String originVertexID) {
        final var graph = Graph.fromFile(path);
        System.out.println(graph);
        System.out.printf("\nDoes the graph have a Hamiltonian cycle? %s\n%n", new HamiltonianCycleDetector(
                Graph.fromFile(path),
                new Vertex(originVertexID)).detect()
                ? "yes" : "no");
    }

    public static void main(String[] args) {
        check("inputWithCycle", "0");
        check("inputWithoutCycle", "5");
    }
}