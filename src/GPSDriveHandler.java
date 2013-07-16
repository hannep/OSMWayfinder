import com.starkeffect.highway.GPSDevice;
import com.starkeffect.highway.GPSEvent;
import com.starkeffect.highway.GPSListener;

public class GPSDriveHandler {
	Graph graph;
	double currentLatitude;
	double currentLongitude;
	double currentHeading;

	public GPSDriveHandler(Graph graph, GPSDevice device) {
		this.graph = graph;
	}
	
	
	

	public GraphNode getClosestGraphNode() {
		GraphNode closestNode = null;
		double closestDistance = 1000000;
		for (GraphNode graphNode : graph.getGraphNodes()) {
			double dx = graphNode.getMapNode().getLongitude()
					- currentLongitude;
			double dy = graphNode.getMapNode().getLatitude() - currentLatitude;
			double distance = Math.sqrt((dx * dx) + (dy * dy));
			if (distance < closestDistance) {
				closestDistance = distance;
				closestNode = graphNode;
			}
		}
		return closestNode; 

	}
}
