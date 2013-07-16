
public class GeoPoint{
	private double latitude; 
	private double longitude; 
	
	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude; 
	}
	
	public double distance(MapNode other) {
		double dx = other.getLongitude()
				- getLongitude();
		double dy = other.getLatitude() - getLatitude();
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	@Override 
	public String toString(){
		return String.format("(%s, %s)", latitude, longitude); 
	}
}
