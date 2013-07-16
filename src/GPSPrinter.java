import com.starkeffect.highway.GPSEvent;
import com.starkeffect.highway.GPSListener;

public class GPSPrinter implements GPSListener {

	@Override
	public void processEvent(GPSEvent event) {
		System.out.println(event.getLatitude() + " " + event.getLongitude()
				+ " " + event.getHeading());

	}

}
