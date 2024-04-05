import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Vertex {
    private int id;
    ArrayList<Edge> edges;

    public Vertex (int id){
        this.id = id;
        edges = new ArrayList<Edge>();
    }

    public void addEdge(Edge edge){
        edges.add(edge);
    }

    public void removeEdge(Edge edge){
        edges.remove(edge);
    }

    public Edge getEdge(Vertex destination){

        for (Edge edge : edges){
            if (edge.destination == destination || edge.source == destination){
                return edge;
            }
        }

        return null ;
    }

    public int getId(){
        return id;
    }
}
