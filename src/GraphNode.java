import java.util.ArrayList;


public class GraphNode {
	ArrayList<GraphNode> neighbors = new ArrayList<GraphNode>(); 
	MapNode originalNode; 
	
	public MapNode getMapNode(){
		return originalNode; 
	}
	
	public GraphNode(MapNode originalNode){
		this.originalNode = originalNode; 
	}

	public void addNeighbor(GraphNode neighborNode){
		neighbors.add(neighborNode);
	}
	
	public ArrayList<GraphNode> getNeighbors(){
		return neighbors; 
	}
}
