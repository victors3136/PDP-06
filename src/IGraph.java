import java.util.Collection;

public interface IGraph {
    Collection<Vertex> vertices();

    int vertexCount();

    int edgeCount();

    boolean hasEdge(Vertex src, Vertex dest);

    int inboundDegree(Vertex src);

    int outboundDegree(Vertex src);

    Collection<Vertex> inboundNeighbors(Vertex src);

    Collection<Vertex> outboundNeighbors(Vertex src);

    boolean hasVertex(Vertex vertex);
}
