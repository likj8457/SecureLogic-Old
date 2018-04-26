package weather;

import java.util.HashMap;
import java.util.List;

abstract class ForecastProvider {
	protected HashMap<Integer, WeatherObject> todaysObservations;
	
	abstract List<WeatherObject> getFullForecast();
	abstract List<WeatherObject> getCondensedForecast();
	abstract WeatherObject getCurrentWeather();
	
	protected String GetRealIconName (String weatherCode, boolean windy) {
		String iconName = "unknown-weather.png";
		if (weatherCode == null) 
		{
			return iconName;
		}
		else if(weatherCode.equals("01d") || weatherCode.equals("01m")) 
		{
			if (!windy) {
				iconName = "clear-day.png";
			} else {
				iconName = "windy.png";				
			}
		} 
		else if(weatherCode.equals("01n")) 
		{
			if (!windy) {
				iconName = "clear-night.png";
			} else {
				iconName = "windy.png";				
			}
		} 
		else if(weatherCode.equals("02d") || weatherCode.equals("02m")) 
		{
			if (!windy) {
				iconName = "broken-clouds-day.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		} 
		else if(weatherCode.equals("02n")) 
		{
			if (!windy) {
				iconName = "broken-clouds-night.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		} 
		else if(weatherCode.equals("03d")) 
		{
			if (!windy) {
				iconName = "scattered-clouds-day.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		} 
		else if(weatherCode.equals("03n")) 
		{
			if (!windy) {
				iconName = "scattered-clouds-night.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		} 
		else if(weatherCode.equals("04d") || weatherCode.equals("04")) 
		{
			if (!windy) {
				iconName = "overcast-day.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		} 
		else if(weatherCode.equals("04n")) 
		{
			if (!windy) {
				iconName = "overcast-night.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		} 
		else if(weatherCode.equals("09d") || weatherCode.equals("40d") || weatherCode.equals("40m")) 
		{
			iconName = "showers-day.png";
		} 
		else if(weatherCode.equals("09n") || weatherCode.equals("40n")) 
		{
			iconName = "showers-night.png";
		} 
		else if(weatherCode.equals("10d") || weatherCode.equals("05d") || weatherCode.equals("05m") || weatherCode.equals("41d") || weatherCode.equals("41m") || 
				weatherCode.equals("46") || weatherCode.equals("09") || weatherCode.equals("10")) 
		{
			iconName = "rain.png";
		} 
		else if(weatherCode.equals("10n") || weatherCode.equals("05n") || weatherCode.equals("41n")) 
		{
			iconName = "rain.png";
		} 
		else if(weatherCode.equals("11d") || weatherCode.equals("24d") || weatherCode.equals("24m") || weatherCode.equals("06d") || weatherCode.equals("06m") || 
				weatherCode.equals("25d") || weatherCode.equals("25m") || weatherCode.equals("30") || weatherCode.equals("22") || weatherCode.equals("11")) 
		{
			iconName = "thunder-storm.png";
		} 
		else if(weatherCode.equals("11n") || weatherCode.equals("25n")) 
		{
			iconName = "thunder-storm.png";
		} 
		else if(weatherCode.equals("50d") || weatherCode.equals("15")) 
		{
			if (!windy) {
				iconName = "mist-day.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		} 
		else if(weatherCode.equals("50n")) 
		{
			if (!windy) {
				iconName = "mist-night.png";
			} else {
				iconName = "windy-overcast.png";				
			}
		}
		else if(weatherCode.equals("13n") || weatherCode.endsWith("n")) 
		{
			iconName = "snow.png";
		}
		else //if(weatherCode.equals("13d")) 
		{
			iconName = "snow.png";
		} 
		return iconName;
	}
}
