package weather;
import java.util.List;

import application.Util;

public class WeatherService 
{
	private enum ForecastProviders {OpenWeatherMap, YR};
	private static ForecastProviders ForecastProvider = ForecastProviders.YR;
	private ForecastProvider forecastProvider;
	
	public static String[] WEEKDAYS = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
	
	public WeatherService () {
		UpdateForecastProvider();
	}
	
	public void UpdateForecastProvider() {
		String provider = Util.getSetting("ForecastProvider");
		if (provider != null && provider.equals("YR")) {
			ForecastProvider = ForecastProviders.YR;
			forecastProvider = new YrForecastProvider();
		} else if (provider != null && provider.equals("OpenWeatherMap")) {
			ForecastProvider = ForecastProviders.OpenWeatherMap;
			forecastProvider = new OpenWeatherMapForecastProvider();
		} else {
			//default
			ForecastProvider = ForecastProviders.YR;
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
	
	public int getRowIndexForWeatherObject(WeatherObject wO) {
		return forecastProvider.getRowIndexForWeatherObject(wO);
	}
	
	public String getRowHeaderForWeatherObject(WeatherObject wO) {
		return forecastProvider.getRowHeaderForWeatherObject(wO);
	}
}
