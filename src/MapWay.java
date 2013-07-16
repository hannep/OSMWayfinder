import java.util.ArrayList;

public class MapWay implements Comparable {
	private String id;
	private String name;
	private ArrayList<MapNode> nodes;
	private Type type; 
	
	public static enum Type {
		NONE, HIGHWAY_PRIMARY, HIGHWAY_RESIDENTIAL, HIGHWAY_SECONDARY, BUILDING, NOT_A_ROAD, NATURAL_WATER, NATURAL_ROCK, NATURAL_GRASS, NATURAL_LEAF, NATURAL_SAND, NATURAL_ICE, NATURAL 
	}

	public MapWay(String id, String name, Type type, ArrayList<MapNode> nodes) {
		this.id = id;
		this.name = name;
		this.type = type; 
		this.nodes = nodes;
		for(MapNode node : nodes) {
			node.addWay(this); 
		}
		
	}

	public String getId() {
		return id;
	}

	public ArrayList<MapNode> getNodeList() {
		return nodes;
	}

	@Override
	public String toString() {
		// Some of the ways don't have names.
		if (name == null) {
			return "Way #" + id;
		}
		return name;
	}

	public String getName() {
		return name;
	}
	
	public Type getType(){
		return type; 
	}
	
	@Override
	public int compareTo(Object obj) {
		MapWay other = (MapWay) obj;
		if (other.getName() == null && name == null) {
			return 0;
		} else if (other.getName() == null) {
			return -1;
		} else if (name == null) {
			return 1;
		}
		return this.toString().compareTo(obj.toString());
	}
	
}
