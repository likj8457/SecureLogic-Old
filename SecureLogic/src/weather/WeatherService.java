package weather;
import java.util.List;

public class WeatherService 
{
	private enum ForecastProviders {OpenWeatherMap, YR};
	private static ForecastProviders ForecastProvider = ForecastProviders.YR;
	private ForecastProvider forecastProvider;
	
	public static String[] WEEKDAYS = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
	
	public WeatherService () {
		if (ForecastProvider == ForecastProviders.OpenWeatherMap) {
			forecastProvider = new OpenWeatherMapForecastProvider();
		} else if (ForecastProvider == ForecastProviders.YR) {
			forecastProvider = new YrForecastProvider();
		}
	}
	
	public List<WeatherObject> getFullForecast() throws Exception {
		return forecastProvider.getFullForecast();
	}
	
	public List<WeatherObject> getCondensedForecast() throws Exception {
		return forecastProvider.getCondensedForecast();
	}
	
	
	public WeatherObject getCurrentWeather() throws Exception {
		return forecastProvider.getCurrentWeather();
	}
}
