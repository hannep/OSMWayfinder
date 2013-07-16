import java.util.ArrayList;
import java.util.HashSet;

public class MapNode {
	String id;
	double latitude, longitude;
	HashSet<MapWay> ways = new HashSet<MapWay>();
	
	public MapWay getCommonWay(MapNode other){ 
		HashSet<MapWay> intersection = new HashSet<MapWay>(ways); 
		intersection.retainAll(other.ways); 
		return intersection.iterator().next(); 
	}
	
	public void addWay(MapWay way){
		ways.add(way); 
	}

	public MapNode(String id, double latitude, double longitude) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getID() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public GeoPoint getPoint(){
		return new GeoPoint(latitude, longitude);
	}
}
