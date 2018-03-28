package weather;
import java.net.URL;

import javafx.scene.image.Image;

public class WeatherObject {
	public Integer DayOfMonth;
	public String Description;
	public Image Icon;
	public String OutdoorTemperature;
	public String WindSpeed;
	
	public static Image buildImage(URL imageLocation) throws Exception {
		Image i = new Image(imageLocation.openStream());
		return i;
	}
}
