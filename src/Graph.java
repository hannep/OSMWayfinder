import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Graph {

	HashMap<String, GraphNode> graphNodeMap = new HashMap<String, GraphNode>();;
	//ArrayList<GraphNode> graphNodes = new ArrayList<GraphNode>();

	public Collection<GraphNode> getGraphNodes() {
		return graphNodeMap.values();
	}

	public Graph(Collection<MapWay> ways) {
		for (MapWay way : ways) {
			ArrayList<GraphNode> graphNodes = new ArrayList<GraphNode>();
			if (!way.getType().equals(MapWay.Type.NOT_A_ROAD)
					&& !way.getType().equals(MapWay.Type.NATURAL)
					&& !way.getType().equals(MapWay.Type.BUILDING)) {
				ArrayList<MapNode> nodes = way.getNodeList();
				for (MapNode node : nodes) {
					if (!graphNodeMap.containsKey(node.id)) {
						graphNodeMap.put(node.id, new GraphNode(node));
					}
					graphNodes.add(graphNodeMap.get(node.id));
				}

				for (int i = 0; i < graphNodes.size() - 1; i++) {
					graphNodes.get(i).addNeighbor(graphNodes.get(i + 1));
					graphNodes.get(i + 1).addNeighbor(graphNodes.get(i));
				}
			}
		}
	}

	private double getDistance(GraphNode node1, GraphNode node2) {
		MapNode mapNode1 = node1.originalNode;
		MapNode mapNode2 = node2.originalNode;
		double dx = mapNode2.getLongitude() - mapNode1.getLongitude();
		double dy = mapNode2.getLatitude() - mapNode1.getLatitude();
		return Math.sqrt(dx * dx + dy * dy);

	}

	public ArrayList<MapNode> getShortestPath(MapNode startNode, MapNode endNode) {
		HashMap<GraphNode, Double> distance = new HashMap<GraphNode, Double>();
		HashMap<GraphNode, GraphNode> previous = new HashMap<GraphNode, GraphNode>();
		HashSet<GraphNode> visited = new HashSet<GraphNode>();
		PriorityQueue<QueueEntry> queue = new PriorityQueue<QueueEntry>();

		distance.put(graphNodeMap.get(startNode.id), 0.0);
		QueueEntry entry = new QueueEntry(graphNodeMap.get(startNode.id), 0.0);
		queue.add(entry);
		GraphNode endGraphNode = null;

		while (queue.size() != 0) {
			QueueEntry next = queue.remove();
			GraphNode nextGraphNode = next.graphNode;
			if (visited.contains(nextGraphNode)) {
				continue;
			}

			visited.add(nextGraphNode);
			if (nextGraphNode.originalNode == endNode) {
				endGraphNode = nextGraphNode;
				break;
			}

			for (GraphNode neighbor : nextGraphNode.getNeighbors()) {
				double newDistance = distance.get(nextGraphNode)
						+ getDistance(nextGraphNode, neighbor);
				if (!distance.containsKey(neighbor)
						|| newDistance < distance.get(neighbor)) {
					distance.put(neighbor, newDistance);
					previous.put(neighbor, nextGraphNode);
					queue.add(new QueueEntry(neighbor, newDistance));
				}
			}

		}

		if (endGraphNode == null) {
			return null;
		}
		ArrayList<MapNode> returnList = new ArrayList<MapNode>();
		while (previous.containsKey(endGraphNode)) {
			returnList.add(endGraphNode.originalNode);
			endGraphNode = previous.get(endGraphNode);
		}
		returnList.add(endGraphNode.originalNode);
		Collections.reverse(returnList);

		return returnList;

	}

	public GraphNode getClosestGraphNode(GeoPoint point) {
		GraphNode closestNode = null;
		double closestDistance = 1000000;
		for (GraphNode graphNode : getGraphNodes()) {
			double distance = point.distance(graphNode.getMapNode());
			if (distance < closestDistance) {
				closestDistance = distance;
				closestNode = graphNode;
			}
		}
		return closestNode;

	}

}
