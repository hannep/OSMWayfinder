import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

public class GPSMapPanel extends JPanel {
	
	GPSDisplay display; 
	
	ArrayList<MapWay> mapWays = new ArrayList<MapWay>();

	static final double ZOOM_FACTOR = 1.0 / 1000;
	static final double MAX_ZOOM = 1270000;
	static final double MIN_ZOOM = 100;

	double baseX = -73.2;
	double baseY = 40.9;

	int prevX, prevY;

	private double zoom = 6500;

	MapWay selectedWay;
	MapNode selectedStartNode;
	MapNode selectedEndNode;
	ArrayList<MapNode> currentPath;

	GeoPoint currentLocation;

	HashMap<MapWay.Type, Color> typeColorLookup = new HashMap<MapWay.Type, Color>();

	// TODO These should be in the controller.
	
	public void setStartNode(MapNode node){
		selectedStartNode = node; 
		display.updatePathFromMapPanel(); 
	}
	
	public void setEndNode(MapNode node){
		selectedEndNode = node; 
		display.updatePathFromMapPanel(); 
	}
	

	public void setCurrentPath(ArrayList<MapNode> nodes) {
		currentPath = nodes;
		repaint();
	}

	public void setCurrentLocation(GeoPoint point) {
		currentLocation = point;
		repaint();
	}

	GPSMapPanel(GPSDisplay display) {
		this.display = display; 
		this.enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK
				| MouseEvent.MOUSE_EVENT_MASK
				| MouseEvent.MOUSE_WHEEL_EVENT_MASK);

		typeColorLookup.put(MapWay.Type.HIGHWAY_PRIMARY, new Color(200, 0, 0));
		typeColorLookup.put(MapWay.Type.HIGHWAY_SECONDARY, new Color(250, 100,
				0));
		typeColorLookup.put(MapWay.Type.HIGHWAY_RESIDENTIAL, new Color(250,
				150, 0));
		typeColorLookup.put(MapWay.Type.BUILDING, new Color(250, 225, 0));
		typeColorLookup.put(MapWay.Type.NATURAL_GRASS, new Color(124, 200, 64));
		typeColorLookup.put(MapWay.Type.NATURAL_LEAF, new Color(80, 120, 38));
		typeColorLookup.put(MapWay.Type.NATURAL_WATER, new Color(0, 100, 200));
		typeColorLookup.put(MapWay.Type.NATURAL_ROCK, new Color(104, 86, 66));
		typeColorLookup.put(MapWay.Type.NATURAL_SAND, new Color(175, 103, 90));
		typeColorLookup.put(MapWay.Type.NATURAL_ICE, new Color(145, 235, 247));
		typeColorLookup.put(MapWay.Type.NATURAL, new Color(59, 103, 28));
	}

	MapNode getStartNode() {
		return selectedStartNode;
	}

	MapNode getEndNode() {
		return selectedEndNode;
	}

	void setWays(ArrayList<MapWay> mapWaysList) {
		mapWays = mapWaysList;
		// baseX = 180.0;
		// baseY = -90.0;

		double latSum = 0.0;
		double longSum = 0.0;
		int totalCount = 0;

		for (MapWay mapWay : mapWaysList) {
			for (MapNode mapNode : mapWay.getNodeList()) {
				latSum += mapNode.getLatitude();
				longSum += mapNode.getLongitude();
				totalCount++;

				// if (mapNode.getLatitude() > baseY) {
				// baseY = mapNode.getLatitude();
				// }
				// if (mapNode.getLongitude() < baseX) {
				// baseX = mapNode.getLongitude();
				// }

			}
		}

		baseX = longSum / totalCount;
		baseY = latSum / totalCount;

	}

	private int getScreenFromMapX(double mapX) {

		return (int) ((mapX - baseX) * (zoom)) + this.getWidth() / 2;
	}

	private int getScreenFromMapY(double mapY) {
		return (int) (0 - (mapY - baseY) * (zoom)) + this.getHeight() / 2;
	}

	private int getScreenFromMapX(MapNode node) {
		return getScreenFromMapX(node.getLongitude());

	}

	private int getScreenFromMapY(GeoPoint node) {
		return getScreenFromMapY(node.getLatitude());
	}

	private int getScreenFromMapX(GeoPoint node) {
		return getScreenFromMapX(node.getLongitude());
	}

	private int getScreenFromMapY(MapNode node) {
		return getScreenFromMapY(node.getLatitude());
	}

	private double getMapFromScreenX(int screenX) {
		return ((screenX - this.getWidth() / 2) / zoom) + baseX;
	}

	private double getMapFromScreenY(int screenY) {
		return (0 - ((screenY - this.getHeight() / 2) / zoom)) + baseY;
	}

	private void paintNodes(Graphics g, ArrayList<MapNode> nodes) {
		for (int i = 0; i < nodes.size() - 1; i++) {
			int x1 = getScreenFromMapX(nodes.get(i));
			int x2 = getScreenFromMapX(nodes.get(i + 1));
			int y1 = getScreenFromMapY(nodes.get(i));
			int y2 = getScreenFromMapY(nodes.get(i + 1));
			g.drawLine(x1, y1, x2, y2);

		}
	}

	@Override
	public void paint(Graphics g) {

		g.clearRect(0, 0, getWidth(), getHeight());
		for (MapWay mapWay : mapWays) {
			ArrayList<MapNode> nodes = mapWay.getNodeList();
			if (mapWay == selectedWay) {
				g.setColor(Color.cyan);
			}

			else if (typeColorLookup.containsKey(mapWay.getType())) {
				g.setColor(typeColorLookup.get(mapWay.getType()));

			} else {
				g.setColor(Color.LIGHT_GRAY);
			}

			paintNodes(g, nodes);

		}

		if (selectedStartNode != null) {
			g.setColor(Color.green);
			g.fillOval((getScreenFromMapX(selectedStartNode) - 4),
					(getScreenFromMapY(selectedStartNode) - 4), 7, 7);
		}
		if (selectedEndNode != null) {
			g.setColor(Color.red);
			g.fillOval((getScreenFromMapX(selectedEndNode) - 4),
					(getScreenFromMapY(selectedEndNode) - 4), 7, 7);
		}

		if (currentLocation != null) {
			g.setColor(Color.orange);
			g.fillOval((getScreenFromMapX(currentLocation) - 5),
					(getScreenFromMapY(currentLocation) - 5), 9, 9);
		}

		if (currentPath != null) {
			System.out.println("Directions Painted!");
			g.setColor(Color.white);
			paintNodes(g, currentPath);
		}

	}

	public void changeZoom(double zoom) {
		this.zoom *= Math.pow(1.1, zoom);
		System.out.println(this.zoom);
		this.zoom = Math.max(MIN_ZOOM, this.zoom);
		this.zoom = Math.min(MAX_ZOOM, this.zoom);

		repaint();
	}

	@Override
	protected void processMouseWheelEvent(MouseWheelEvent event) {
		changeZoom(-(event.getUnitsToScroll()));
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent event) {
		if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
			// System.out.printf("%s %s \n", event.getX(), event.getY());
			int difX = event.getX() - prevX;
			int difY = event.getY() - prevY;
			baseX -= (difX / zoom);
			baseY += (difY / zoom);
			prevX = event.getX();
			prevY = event.getY();
			repaint();
		}
		// System.out.printf("%s %s \n", event.getX(), event.getY());
	}

	protected void processMouseEvent(MouseEvent event) {
		if (event.getID() == MouseEvent.MOUSE_PRESSED) {
			prevX = event.getX();
			prevY = event.getY();
		}

		if (event.getID() == MouseEvent.MOUSE_CLICKED) {

			double clickLongitude = getMapFromScreenX(event.getX());
			double clickLatitude = getMapFromScreenY(event.getY());
			MapNode closestNode = null;
			double closestDistance = 1000000;
			for (MapWay mapWay : mapWays) {
				if (!mapWay.getType().equals(MapWay.Type.NOT_A_ROAD)
						&& !mapWay.getType().equals(MapWay.Type.NATURAL)
						&& !mapWay.getType().equals(MapWay.Type.BUILDING)) {
					for (MapNode mapNode : mapWay.getNodeList()) {
						double dx = mapNode.getLongitude() - clickLongitude;
						double dy = mapNode.getLatitude() - clickLatitude;
						double distance = Math.sqrt((dx * dx) + (dy * dy));
						if (distance < closestDistance) {
							closestDistance = distance;
							closestNode = mapNode;
						} 
					}
				}
			}

			System.out.println("Picked closest node");

			if (event.getButton() == MouseEvent.BUTTON1) {
				setStartNode(closestNode); 
				
			}

			if (event.getButton() == MouseEvent.BUTTON3) {
				setEndNode(closestNode); 
			}

			repaint();
		}

	}

	void setSelectedWay(MapWay way) {
		selectedWay = way;
	}

}
