import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Vertex implements Comparable<Vertex>{
    public final String name;
    public ArrayList<Edge> adjacency;
    private int minDistance = Integer.MAX_VALUE;
    public Vertex previous;

    public Vertex(String argName){
        name = argName;
        adjacency = new ArrayList<>();
    }

    public String toString(){
        return name;
    }

    public int compareTo(Vertex other){
        return Integer.compare(minDistance, other.minDistance);
    }

    // getters and setters

    public ArrayList<Edge> getAdjacency() {
        return adjacency;
    }

    public void addAdjacency(Edge adj) {
        adjacency.add(adj);
    }

    public int getMinDistance() { 
		return minDistance; 
	}

    public void setMinDistance(int minDis) {
        minDistance = minDis;
    }

    public void resetMinDistance() {
        minDistance = Integer.MAX_VALUE;
    }
}

class Edge{
    public final Vertex target;
    public final int cost;

    public Edge(Vertex argTarget, int argCost) {
        target = argTarget; cost = argCost;
    }
}

class VertexComparator implements Comparator<Vertex> {
    @Override
    public int compare(Vertex v1, Vertex v2){
        return v1.name.compareTo(v2.name);
    }
}

class LSRCompute{
    static final List<String> nodeName = Arrays.asList("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""));

    public static void main(String[] args){
        String lsaFile;
        String firstNode;
        String executionMode;
        // read arguments
        try {
            lsaFile = args[0]; // *.lsa
        } catch(Exception e){
            System.out.println("Missing argument 1: input lsa file");
            return;
        }
        try {
            firstNode = args[1].toUpperCase(); // [A...Z]
        }
        catch(Exception e){
            firstNode = "A";
        }
        try {
            executionMode = args[2].toUpperCase(); // SS|CA
        }
        catch (Exception e) {
            executionMode = "CA";
        }

        long nNodes = 0;

        try{
            nNodes = Files.lines(Paths.get(lsaFile)).count();
        }
        catch(Exception e){
            System.err.println("Error while opening file: " + e.getMessage());
        }

        ArrayList<Vertex> vertices = new ArrayList<>();

        for(int i = 0; i < nNodes; i++){
            // create new node and assign it into vertex
            vertices.add(new Vertex(nodeName.get(i)));
        }

        try{
            // read LSA file
            FileInputStream fstream = new FileInputStream(lsaFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            int indexRecord = 0;

            while ((strLine = br.readLine()) != null){
                // split by spaces
                String[] strRecord = strLine.split("\\s");
                final int nRecords = strRecord.length;

                for(int j = 1; j < nRecords; j++){ 
                    String[] recordSplit = strRecord[j].split(":");
                    int neighbor = nodeName.indexOf(recordSplit[0]); // extract alphabet
                    int cost = Integer.parseInt(recordSplit[1]);  // extract cost
                    vertices.get(indexRecord).addAdjacency(new Edge(vertices.get(neighbor), cost));  // add into the neighbor array
                }
                ++indexRecord;
            }

            br.close();
        }
        catch(Exception e){
            System.err.println("Error while reading lsa file: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }

        int nodeIndex = nodeName.indexOf(firstNode);

        switch(executionMode){
            case "SS": { // print a specific node
                while (true) {
                    final Vertex node = vertices.get(nodeIndex);
                    computePaths(node);
                    Scanner sc = new Scanner(System.in);
                    String targetName;
                    int targetIndex;
                    do {
						System.out.println();
						System.out.println( "Source " + vertices.get(nodeIndex) + ":" );
						System.out.print("To which node? ");
                        targetName = sc.next().toUpperCase();
                        targetIndex = nodeName.indexOf(targetName);
                        if (targetIndex == -1) { // target node not found
                            System.out.println("Node " + targetName + " does not exists");
                        }
                    } while(targetIndex == -1); // loop user input while target does not exist

                    Vertex targetNode = vertices.get(targetIndex);
					System.out.println();
                    System.out.print("Found " + targetNode + ": ");
                    List<Vertex> path = getShortestPathTo(targetNode);
                    System.out.print("Path: ");
                    for (int i = 0; i < path.size(); i++) {
                        System.out.print(path.get(i));
                        if (i < path.size() - 1)
                            System.out.print(">");
                    }
                    System.out.println(" Cost: " + targetNode.getMinDistance());
					printAll(vertices);
                    performUserAction(vertices);

                    // reset all distances
                    vertices.forEach(Vertex::resetMinDistance);

                    pressAnyKeyToContinue();
                }
            }
			case "CA": {  // compute all nodes
                while (true) {
                    computePaths(vertices.get(nodeIndex));
					System.out.println();
                    System.out.println("Source " + vertices.get(nodeIndex) + ":");
					System.out.println();
                    for (Vertex v : vertices) {  // print out all shortest paths
                        if (v != vertices.get(nodeIndex)) {
                            System.out.print(v + ": ");
                            List<Vertex> path = getShortestPathTo(v);
                            System.out.print("Path: ");
                            for (int i = 0; i < path.size(); i++) {
                                System.out.print(path.get(i));
                                if (i + 1 < path.size())
                                    System.out.print(">");
                            }
                            System.out.println(" Cost: " + v.getMinDistance());
                        }
                    }
					printAll(vertices);
                    performUserAction(vertices);

                    // reset all distances
                    vertices.forEach(Vertex::resetMinDistance);

                    pressAnyKeyToContinue();
                }
            }
        }
    }

    public static void computePaths(Vertex source) {
        source.setMinDistance(0);

        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Vertex u = vertexQueue.poll();
            for (Edge e : u.getAdjacency()){
                Vertex v = e.target;
                int cost = e.cost;
                int distanceThroughU = u.getMinDistance() + cost;
                if (distanceThroughU < v.getMinDistance()) {
                    vertexQueue.remove(v);

                    v.setMinDistance(distanceThroughU);
                    v.previous = u;
                    vertexQueue.add(v);
                }
            }
        }
    }

    public static List<Vertex> getShortestPathTo(Vertex target) {
        List<Vertex> path = new ArrayList<>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);

        Collections.reverse(path);
        return path;
    }

    private static void pressAnyKeyToContinue(){
        System.out.println("[Press any key to continue]");
        try {
            System.in.read();
        }
        catch(Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void performUserAction(List<Vertex> vertex){
		System.out.println();
        System.out.print("Do you want to add or delete node (add/del/exit)? ");
        Scanner sc = new Scanner(System.in);
        String action = sc.next().toUpperCase();

        if(action.equals("ADD")){
            System.out.println("Please enter the new node relation (newNode: existingNode1:cost existingNode2:cost...):");
            sc = new Scanner(System.in);
            String newRecords = sc.nextLine().toUpperCase();
            String[] record = newRecords.split("\\s"); // split by spaces

            String newNodeName = record[0].substring(0,1);

            for(Vertex node: vertex){ // check if new node name already exists
                if(node.name.equals(newNodeName)){
                    System.out.println("Node " + newNodeName + " already exists");
                    return;
                }
            }

            Vertex newNode = new Vertex(newNodeName);

            for(int i = 1; i < record.length; i++){
                int neighorIndex = -1;
                for(Vertex node : vertex){ // finding index of existing node
                    if(node.name.equals(record[i].substring(0, 1))){
                        neighorIndex = vertex.indexOf(node); // add into the neighbor array
                        break;
                    }
                }

                int cost = Integer.parseInt(record[i].substring(2,3));

                newNode.addAdjacency(new Edge(vertex.get(neighorIndex), cost));
                vertex.get(neighorIndex).addAdjacency(new Edge(newNode, cost));
            }
            vertex.add(newNode);
            System.out.println("Node " + newNode + " added");
        }
        else if(action.equals("DEL")){
            System.out.println("Which of the following node you want to delete? ");
            System.out.println(vertex);
            System.out.print("Node to delete (1 node only): ");
            sc = new Scanner(System.in);
            action = sc.next().toUpperCase();

            Iterator<Vertex> nodeIterator = vertex.iterator();
            while(nodeIterator.hasNext()) { // iterate through vertices to search
                Vertex v = nodeIterator.next();
                Iterator<Edge> adjIterator = v.getAdjacency().iterator();
                while (adjIterator.hasNext()) {
                    Edge adj = adjIterator.next();
                    if(adj.target.name.equals(action)){  // remove by being the adjacent of other nodes
                        adjIterator.remove();
                    }
                }
                if(v.name.equals(action)) {
                    nodeIterator.remove(); // change position in the array
                }
            }
        }
        else { // exit
            System.exit(0);
        }
        vertex.sort(new VertexComparator());  // sort nodes
    }
	public static void printAll(ArrayList<Vertex> allVertex){
        System.out.println();
        System.out.println("======= The relations are the following =======");
		System.out.println();
        for(int i=0; i<allVertex.size(); i++){
            System.out.print(allVertex.get(i).name + ": ");
            for(int j=0; j<allVertex.get(i).adjacency.size(); j++){
                 System.out.print(allVertex.get(i).adjacency.get(j).target + ":" + allVertex.get(i).adjacency.get(j).cost + " ");
            }
            System.out.println();
        }
    }
}