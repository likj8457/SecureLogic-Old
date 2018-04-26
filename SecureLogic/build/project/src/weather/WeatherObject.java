package weather;
import java.net.URL;

import javafx.scene.image.Image;

public class WeatherObject {
	public enum WeatherObjectTypes {Forecast, Observation};
	public Integer DayOfMonth;
	public Integer Hour;
	public String Description;
	public Image Icon;
	public String OutdoorTemperature;
	public String WindSpeed;
	public String Rain;
	public WeatherObjectTypes Type;
	
	public static Image buildImage(URL imageLocation) throws Exception {
		Image i = new Image(imageLocation.openStream());
		return i;
	}
}
