public class Edge implements Comparable<Edge> {
    Vertex source;
    Vertex destination;
    int cost;
    double reliability;

    public Edge(Vertex source, Vertex destination, int cost, double reliability){
        this.source = source;
        this.destination = destination;
        this.cost = cost;
        this.reliability = reliability;
    }

    @Override
    public int compareTo(Edge compareEdge) {
        double compareedge = ((Edge)compareEdge).getReliability();
        double compareCost =  ((Edge) compareEdge).getCost();

        /* For Ascending order*/
        if(this.reliability < compareedge) {
            if (this.cost <= compareCost) {
                return 1;
            }
        }else if(compareedge < this.reliability)
            return -1;
        return 0;


    }

    public double getReliability(){
        return reliability;
    }

    public int getCost(){
        return cost;
    }


}

