import java.util.*;

public class Graph {

    HashMap<Vertex, HashSet<Vertex>> vertexMap;
    private ArrayList<Edge> connectedEdges;

    public Graph(){
        vertexMap = new HashMap<Vertex, HashSet<Vertex>>();
        connectedEdges = new ArrayList<Edge>();
    }


    public void addVertex(int id) {
        Vertex vertex = new Vertex(id);
        HashSet<Vertex> vertSet = new HashSet<Vertex>();
        vertexMap.put(vertex, vertSet);
    }

    public void clearEdges() {
        connectedEdges.clear();
        for (HashSet<Vertex> vertices : vertexMap.values()) {
            vertices.clear();  // Clear all adjacency sets
        }
    }

    public void addEdge(Edge edge){
        connectedEdges.add(edge);
    }

    public ArrayList<Edge> getNetworkEdges(){
        return connectedEdges;
    }

    public Edge getLowestReliableEdge(){
        if(connectedEdges.size() != 0){
            return connectedEdges.get(connectedEdges.size() - 1);
        }
        return null;
    }

    public Edge getSecondLowestEdge(){
        if(connectedEdges.size() != 0){
            return connectedEdges.get(connectedEdges.size() - 2);
        }
        return null;
    }

    public Vertex getVertex(int id){
        for (Vertex vertex : vertexMap.keySet()){
            if (vertex.getId() == id){
                return vertex;
            }
        }
        return null;
    }
}
