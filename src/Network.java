import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Network {

    private static int numberOfCities;
    private static ArrayList<Double> reliabilityMajorMatrix;
    private static ArrayList<Integer> costMajorMatrix;
    private static String fileName;
    private static int targetCost;
    private static double spanningReliability;
    private static int spanningCost;
    private static int totalCostEE;
    private static double totalReliability;
    private static double totalReliabilityEE;
    private static int totalCost;
    private static long durationKruskal;
    private static long durationEE;
    private static int amountOfSpanningEdges;
    private static boolean spanningTreeFound;
    private static Graph networkGraph;
    private static Graph networkGraphEE;

    public static void main (String[] args) throws InterruptedException {

        parseArguments(args);
        networkGraph = new Graph();
        networkGraphEE = new Graph();
        reliabilityMajorMatrix = new ArrayList<>();
        costMajorMatrix = new ArrayList<>();
        ArrayList<Edge> edgesKruskal = new ArrayList<>();
        ArrayList<Edge> edgesEE = new ArrayList<>();

        readInputFile(fileName);

        initializeVertices(networkGraph);
        initializeVertices(networkGraphEE);
        createEdges(edgesKruskal, networkGraph);
        createEdges(edgesEE, networkGraphEE);

        // sort by cost
        Comparator<Edge> costOrder = Comparator.comparing(Edge::getCost).thenComparing(Edge::getReliability, Comparator.reverseOrder());
        Collections.sort(edgesKruskal, costOrder);

        kruskalMST(networkGraph, edgesKruskal);
        long startTimeEE = System.nanoTime();
        exhaustiveEnumeration(networkGraphEE, edgesEE);
        long endTimeEE = System.nanoTime();
        durationEE = endTimeEE - startTimeEE;

        for (int i = 0; i < 1000; i++) {

        }

        amountOfSpanningEdges = networkGraph.getNetworkEdges().size();
        spanningReliability = calculateSpanningTreeReliability(networkGraph);
        spanningCost = calculateSpanningTreeCost(networkGraph);

        double currentReliability = spanningReliability;
        int currentCost = spanningCost;


        double newReliability;
        int newCost;

        ArrayList<Edge> additionalEdges = new ArrayList<>();
        while (currentCost < targetCost){

            if(additionalEdges.isEmpty()){
                // Grab next set of available edges
                additionalEdges = getLowestAvailableCostEdges(networkGraph, edgesKruskal);
                if(additionalEdges.isEmpty()){
                    // make sure if no more edges
                    break;
                }
            }

            // only update with the first available low cost edge.
            updateGraph(networkGraph, additionalEdges.get(0));

            ArrayList<Integer[]> combinationList = new ArrayList<>();
            findCombination(networkGraph, combinationList);

            newReliability = calculateTotalReliability(networkGraph, combinationList);
            newCost = calculateTotalCost(networkGraph);

            if (newCost > targetCost){
                removeNetworkEdge(networkGraph, additionalEdges.get(0));
                break;
            } else {
                currentReliability = newReliability;
                currentCost = newCost;
            }

            additionalEdges.remove(0);

        }

        totalReliability = currentReliability;
        totalCost = currentCost;
        if (targetCost < totalCost) {
            System.out.println("INFEASIBLE SOLUTION");
            System.exit(1);
        }

        displayGraphInformationEE();
        displayGraphInformationKruskal();


    }

    public static void initializeVertices (Graph networkGraph){
        for (int i = 0; i < numberOfCities; i++){
            networkGraph.addVertex(i);
        }
    }

    public static void createEdges(ArrayList<Edge> edges, Graph networkGraph){
        int matrixIndex = 0;
        for (int i = 0 ; i < numberOfCities; i++){
            int sourceId = i;
            for (int j = sourceId + 1 ; j < numberOfCities; j++){
                int destinationId = j;
                edges.add(new Edge(networkGraph.getVertex(sourceId), networkGraph.getVertex(destinationId), costMajorMatrix.get(matrixIndex), reliabilityMajorMatrix.get(matrixIndex)));
                matrixIndex++;
            }
        }
    }

    public static void readInputFile(String fileName){

        int counter = 0;
        int reliabilityMatrixRowCounter = 0;
        int costMatrixRowCounter = 0;
        try {
            File f = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(f));
            String readLine = "";
            String reliabilityLine = "";
            String costLine ="";

            while ((readLine = br.readLine()) != null){
                if (!readLine.contains("#")){
                    if (counter == 0){
                        numberOfCities = Integer.valueOf(readLine);
                        counter++;
                    } else if (counter == 1){
                        if (reliabilityLine.isEmpty()) {
                            reliabilityLine = reliabilityLine + readLine;
                        } else {
                            reliabilityLine = reliabilityLine + " " + readLine;
                        }
                        reliabilityMatrixRowCounter++;
                        if (reliabilityMatrixRowCounter == numberOfCities - 1) {
                            String[] reliabilityValues = reliabilityLine.split(" ");
                            for (String value : reliabilityValues){
                                Double dnum = Double.valueOf(value);
                                reliabilityMajorMatrix.add(dnum);
                            }
                            counter++;
                        }
                    } else if (counter == 2){
                        if (costLine.isEmpty()) {
                            costLine = costLine + readLine;
                        } else {
                            costLine = costLine + " " + readLine;
                        }
                        costMatrixRowCounter++;
                        if (costMatrixRowCounter == numberOfCities - 1) {
                            String[] costValues = costLine.split(" ");
                            for (String value : costValues){
                                costMajorMatrix.add(Integer.valueOf(value));
                            }
                            counter++;
                        }
                    } else {
                        System.out.println("ERROR TOO MANY LINES");
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void combination(int arr[], int data[], int start, int end, int index, int r, ArrayList<Integer[]> combinationValue){
        if(index == r){
            Integer[] combinationList = Arrays.stream(data).boxed().toArray(Integer[]::new);
            combinationValue.add(combinationList);
            return;
        }

        for (int i = start; i <= end && end-i+1 >= r-index; i++){
            data[index] = arr[i];
            combination(arr, data, i+1, end, index+1, r, combinationValue);
        }

    }

    public static void getCombinations(int arr[], int n, int r, ArrayList<Integer[]> combinationValue){
        int data[] = new int[r];
        combination(arr, data, 0 , n-1, 0, r, combinationValue);
    }


    // using BFS to check for any cycles
    public static boolean hasCycle(Graph networkGraph){
        HashMap<Vertex, Vertex> parents = new HashMap<>();
        Queue<Vertex> queue = new LinkedList<>();
        ArrayList<Vertex> visitedVertices = new ArrayList<>();

        Vertex source = networkGraph.getVertex(networkGraph.getNetworkEdges().get(0).source.getId());
        Vertex parent = source;
        queue.add(source);
        visitedVertices.add(source);
        parents.put(source,parent);

        boolean firstTime = true;

        while(!queue.isEmpty()){
            Vertex v = queue.remove();
            parent = v;

            for(Vertex neighbor : networkGraph.vertexMap.get(v)){
                if (!(parents.get(v).equals(neighbor))){

                    if(visitedVertices.contains(neighbor) && !firstTime){
                        return true;
                    }

                    visitedVertices.add(neighbor);
                    queue.add(neighbor);
                    parents.put(neighbor, parent);
                }
            }
            firstTime = false;
        }

        return false;
    }
    public static void exhaustiveEnumeration(Graph networkGraph, ArrayList<Edge> edges) {
        int counter = 0;
        double bestReliability = 0;
        int bestCost = 0;
        ArrayList<Edge> bestNetwork = null;
        int allEdges = edges.size();
        int combinations = 1 << allEdges;  // 2^allEdges possible subsets

        for (int i = 1; i < combinations; i++) {  // Start from 1 to skip the empty set
            ;
            ArrayList<Edge> subset = new ArrayList<>();
            int currentCost = 0;
            double currentReliability = 1;  // Assuming reliability multiplies

            for (int j = 0; j < allEdges; j++) {
                if ((i & (1 << j)) != 0) {  // Check if the j-th edge is included in the subset
                    Edge edge = edges.get(j);
                    subset.add(edge);
                    currentCost += edge.getCost();
                    currentReliability *= edge.getReliability();
                    counter++;
                }
            }

            if (currentCost <= targetCost && isSpanningTree(networkGraph, subset) && currentReliability > bestReliability ) {
                bestReliability = currentReliability;
                bestCost = currentCost;
                bestNetwork = new ArrayList<>(subset);
            }
            if (spanningTreeFound && currentCost <= targetCost) {
                bestReliability = currentReliability;
                bestCost = currentCost;
                bestNetwork = new ArrayList<>(subset);
            }
        }
        totalReliabilityEE = bestReliability;
        totalCostEE = bestCost;

        // Now use bestNetwork as the result
        if (bestNetwork != null) {
            networkGraph.clearEdges();  // Clear existing edges
            for (Edge edge : bestNetwork) {
                networkGraph.addEdge(edge);  // Add each edge of the best network to the graph
            }
        }
    }

    // Helper method to check if a given subset of edges forms a spanning tree
    private static boolean isSpanningTree(Graph networkGraph, ArrayList<Edge> edges) {
        // Implement using a Union-Find data structure to check connectivity and cycle formation
        UnionFind uf = new UnionFind(numberOfCities);
        for (Edge edge : edges) {
            if (uf.find(edge.source.getId()) == uf.find(edge.destination.getId())) {
                return false;  // A cycle is detected
            }
            uf.union(edge.source.getId(), edge.destination.getId());
        }
        // Check if all nodes are connected
        int firstParent = uf.find(0);
        for (int i = 1; i < numberOfCities; i++) {
            if (uf.find(i) != firstParent) {
                return false;  // Not all nodes are connected
            }
        }
        spanningTreeFound = true;
        return true;
    }

    public static void kruskalMST(Graph networkGraph, ArrayList<Edge> edges){
        long startTimeKruskal = System.nanoTime();
        for(Edge edge : edges){
            networkGraph.addEdge(edge);
            addNetworkEdge(networkGraph, edge);
            if(networkGraph.getNetworkEdges().size() != 1){
                if(hasCycle(networkGraph)){
                    removeNetworkEdge(networkGraph,edge);
                } else {
                    if (networkGraph.getNetworkEdges().size() == (networkGraph.vertexMap.keySet().size() - 1)) {
                        break;
                    }
                }
            }
        }
        long endTimeKruskal = System.nanoTime();
        durationKruskal = endTimeKruskal - startTimeKruskal;
    }

    public static void addNetworkEdge(Graph networkGraph, Edge edge){
        // Source Vertex
        edge.source.addEdge(edge);
        // add , destination to source
        HashSet<Vertex> hs = networkGraph.vertexMap.get(edge.source);
        hs.add(edge.destination);
        networkGraph.vertexMap.put(edge.source, hs);
        edge.source.addEdge(edge);

        // add , source to destination

        HashSet<Vertex> hs2 = networkGraph.vertexMap.get(edge.destination);
        hs2.add(edge.source);
        networkGraph.vertexMap.put(edge.destination, hs2);
        edge.destination.addEdge(edge);
    }

    public static void removeNetworkEdge(Graph networkGraph, Edge edge){

        // add , destination to source
        HashSet<Vertex> hs = networkGraph.vertexMap.get(edge.source);
        hs.remove(edge.destination);
        networkGraph.vertexMap.put(edge.source, hs);
        edge.source.removeEdge(edge);

        // add , source to destination

        HashSet<Vertex> hs2 = networkGraph.vertexMap.get(edge.destination);
        hs2.remove(edge.source);
        networkGraph.vertexMap.put(edge.destination, hs2);
        edge.destination.removeEdge(edge);

        networkGraph.getNetworkEdges().remove(edge);

    }

    public static double calculateSpanningTreeReliability(Graph networkGraph){

        double spanningTreeReliability = 1;
        for(Edge edge : networkGraph.getNetworkEdges()){
            spanningTreeReliability *= edge.getReliability();
        }

        return spanningTreeReliability;
    }

    public static int calculateSpanningTreeCost(Graph networkGraph){
        int cost = 0;
        for(Edge edge : networkGraph.getNetworkEdges()){
            cost += edge.getCost();
        }
        return cost;

    }

    public static ArrayList<Edge> getLowestAvailableCostEdges(Graph networkGraph, ArrayList<Edge> edges){
        ArrayList<Edge>  lowestAvailableCostEdges = new ArrayList<>();
        boolean firstAvailableEdge = true;
        int minimumAvailableCost = 0;
        for(Edge edge : edges){
            if(!networkGraph.getNetworkEdges().contains(edge)){

                if(firstAvailableEdge){
                    lowestAvailableCostEdges.add(edge);
                    minimumAvailableCost = edge.getCost();
                    firstAvailableEdge = false;
                }

                else if (edge.getCost() == minimumAvailableCost){
                    lowestAvailableCostEdges.add(edge);
                }
            }
        }
        return lowestAvailableCostEdges;
    }

    public static void findCombination(Graph networkGraph, ArrayList<Integer[]> combinationList){

        int arr[] = new int[networkGraph.getNetworkEdges().size()];
        for (int i = 0; i < arr.length; i++){
            arr[i] = i;
        }

        int n = arr.length;

        for(int i = amountOfSpanningEdges; i <= n; i++){
            getCombinations(arr, n, i, combinationList);
        }
    }

    public static double calculateTotalReliability(Graph networkGraph, ArrayList<Integer[]> combinationList){
        double totalReliability = 0;

        for (Integer[] combination : combinationList){
            ArrayList<Edge> edges = convertCombinationToEdges(networkGraph, combination);
            if (checkConnectivity(networkGraph,edges)){
                double reliability = 1;

                for(Edge edge : edges){
                    reliability *= edge.getReliability();
                }

                for (int i = 0; i < networkGraph.getNetworkEdges().size(); i++){
                    if (Arrays.binarySearch(combination, i) < 0){
                        reliability *= (1 - networkGraph.getNetworkEdges().get(i).getReliability());
                    }
                }
                totalReliability += reliability;
            }
        }
        return totalReliability;
    }

    public static int calculateTotalCost(Graph networkGraph){
        int totalCost = 0;

        for(Edge edge : networkGraph.getNetworkEdges()){
            totalCost += edge.getCost();
        }

        return totalCost;
    }

    public static void updateGraph(Graph networkGraph, Edge edge){
        Vertex source = edge.source;
        Vertex destination = edge.destination;

        source.addEdge(edge);
        HashSet<Vertex> hs = networkGraph.vertexMap.get(source);
        hs.add(destination);
        networkGraph.vertexMap.put(source, hs);


        destination.addEdge(edge);
        HashSet<Vertex> hs2 = networkGraph.vertexMap.get(edge.destination);
        hs2.add(destination);
        networkGraph.vertexMap.put(destination, hs2);

        networkGraph.addEdge(edge);
    }

    public static boolean checkConnectivity(Graph networkGraph, ArrayList<Edge> edges) {
        boolean[] visitedVertices = new boolean[networkGraph.vertexMap.keySet().size()];
        Arrays.fill(visitedVertices, Boolean.FALSE);

        // verify if every node is connected
        for (Edge edge : edges) {
            if (!visitedVertices[edge.source.getId()]) {
                visitedVertices[edge.source.getId()] = true;
            }

            if (!visitedVertices[edge.destination.getId()]) {
                visitedVertices[edge.destination.getId()] = true;
            }

        }

        for (boolean visited : visitedVertices){
            if (!visited){
                return false;
            }
        }
        return true;
    }

    public static ArrayList<Edge> convertCombinationToEdges(Graph networkGraph, Integer[] combinationList){
        ArrayList<Edge> edges = new ArrayList<>();
        for (Integer index : combinationList){
            edges.add(networkGraph.getNetworkEdges().get(index));
        }
        return edges;
    }

    public static void parseArguments(String[] args){
        if (args.length == 2){
            fileName = args[0];
            targetCost = Integer.parseInt(args[1]);
        }
        else {
            System.out.println("INVALID INPUTS");
            System.exit(1);
        }
    }



    public static void displayGraphInformationEE(){
        ArrayList<Edge> finalEdges = networkGraph.getNetworkEdges();
        System.out.println("----- NETWORK DESIGN - EXHAUSTIVE ENUMERATION -----");
        System.out.println("Connected edges : ");
        for(Edge edge : finalEdges){
            System.out.println(" [" + (edge.source.getId() + 1) + ", " + (edge.destination.getId() + 1) + "]" + "  cost: " + edge.cost + "  reliability: " + edge.reliability);
        }
        System.out.println("NETWORK HAS TOTAL EDGE : " + networkGraph.getNetworkEdges().size());
        System.out.println("DESIGN MAX RELIABILITY : " + totalReliability);
        System.out.println("DESIGN TARGET COST : " + targetCost);
        System.out.println("DESIGN OPTIMIZED COST : " + totalCost);
        System.out.println("EXECUTION TIME IN NANOSECONDS: " + durationEE);
        System.out.println("----------------------------------------------------------------------------------------------------");
    }

    public static void displayGraphInformationKruskal(){
        ArrayList<Edge> finalEdges = networkGraph.getNetworkEdges();
        System.out.println("----- NETWORK DESIGN - KRUSKAL'S -----");
        System.out.println("Connected edges : ");
        for(Edge edge : finalEdges){
            System.out.println(" [" + (edge.source.getId() + 1) + ", " + (edge.destination.getId() + 1) + "]" + "  cost: " + edge.cost + "  reliability: " + edge.reliability);
        }
        System.out.println("NETWORK HAS TOTAL EDGE : " + networkGraph.getNetworkEdges().size());
        System.out.println("DESIGN MAX RELIABILITY : " + totalReliability);
        System.out.println("DESIGN TARGET COST : " + targetCost);
        System.out.println("DESIGN OPTIMIZED COST : " + totalCost);
        System.out.println("EXECUTION TIME IN NANOSECONDS: " + durationKruskal);
        System.out.println("----------------------------------------------------------------------------------------------------");
        System.out.println("EXECUTION TIME OF KRUSKAL'S ALGORITHM IS " + (float) durationEE/durationKruskal + " (" + durationEE + "/" + durationKruskal + ")" + " TIMES FASTER THAN EXHAUSTIVE ENUMERATION");
        System.out.println("----- END -----");
    }
}

