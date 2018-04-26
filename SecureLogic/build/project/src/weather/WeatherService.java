package weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import application.Util;
import weather.WeatherObject.WeatherObjectTypes;
import zwave.fibaro.HC2Interface;

public class WeatherService 
{
	private static String GENERAL_OBSERVATION_URL = "http://api.openweathermap.org/data/2.5/weather?lat=55.37624270&lon=13.15742310&appid=7cc62a97aa786a31530400c9df9bf948&units=metric";
	private static String GENRAL_FORCAST_URL = "http://api.openweathermap.org/data/2.5/forecast?q=Trelleborg,SE&appid=7cc62a97aa786a31530400c9df9bf948&units=metric";
	private static List<Integer> staticTimesToRecord = Arrays.asList(10, 13, 16, 19, 22);
	private static HashMap<Integer, WeatherObject> todaysObservations = new HashMap<>();
	
	public static String[] WEEKDAYS = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
	
	private static JSONObject readWeather(String url) throws IOException, JSONException {
		String text = readStringFromUrl(url);
		return new JSONObject(text);
	}
	
	private static String readStringFromUrl(String url) throws IOException {

		InputStream inputStream = new URL(url).openStream();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			StringBuilder stringBuilder = new StringBuilder();
			int cp;
			while ((cp = reader.read()) != -1) {
				stringBuilder.append((char) cp);
			}
			return stringBuilder.toString();
		} finally {
			inputStream.close();
		}
	}
	
	public static List<WeatherObject> GetFullForecast() throws Exception {
		JSONObject gFObject = readWeather(GENRAL_FORCAST_URL);
		JSONArray gFList = gFObject.getJSONArray("list");
		List<WeatherObject> retArr = new ArrayList<WeatherObject>();
		
		for(WeatherObject wO : todaysObservations.values()) {
			retArr.add(wO);
		}
		
		for(int i=0; i< gFList.length(); i++) {
			long dT = 0; 
			
			try {
				String tempS = gFList.getJSONObject(i).getString("dt");
				dT = Long.parseLong(tempS);
				Date date = new Date(dT*1000L); 
				SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd"); 
				SimpleDateFormat timeOfDayFormat = new SimpleDateFormat("HH"); 
				// give a timezone reference for formatting (see comment at the bottom)
				dayOfMonthFormat.setTimeZone(TimeZone.getTimeZone("GMT+1")); 
				timeOfDayFormat.setTimeZone(TimeZone.getTimeZone("GMT+1")); 
				String dayOfMonthS = dayOfMonthFormat.format(date);
				String hourOfDayS = timeOfDayFormat.format(date);
				Integer dayOfMonthI = Integer.parseInt(dayOfMonthS);
				Integer hourOfDayI = Integer.parseInt(hourOfDayS);

				//Only look at wheather between 0800-2200, the rest is probably not important - except for first day if today, and last day if no hours between 8-21 are given
				if (hourOfDayI < 8 || hourOfDayI > 22) 
				{
					continue;
				}
		    	
				DecimalFormat df = new DecimalFormat("#");
		    	
		    	WeatherObject wO = new WeatherObject();
		    	wO.DayOfMonth = dayOfMonthI;
		    	wO.Hour = hourOfDayI;

				//Wind
				JSONObject wObj2 = gFList.getJSONObject(i).getJSONObject("wind");
				double wind = wObj2.getDouble("speed");
		    	wO.WindSpeed = df.format(wind);
		    	
		    	JSONArray wArr = gFList.getJSONObject(i).getJSONArray("weather");
				if (wArr.length() > 0) {
					String wDesc = wArr.getJSONObject(0).getString("description");
					wO.Description = wDesc;
					String wIcon = wArr.getJSONObject(0).getString("icon");
					
			    	if (wIcon.endsWith("n")) {
			    		wIcon = wIcon.substring(0,  wIcon.length()-1) + "d";
			    	}
			    	
			    	boolean windy = wind >= 10;
			    	
			    	String iconName = GetRealIconName(wIcon, windy);
			    	wO.Icon = WeatherObject.buildImage(WeatherService.class.getResource("/img/50x50/" + iconName));
				}
				//Temp
				JSONObject wObj = gFList.getJSONObject(i).getJSONObject("main");
				double temp = wObj.getDouble("temp");
				wO.OutdoorTemperature = df.format(temp);
				
				//Rain
				if (gFList.getJSONObject(i).has("rain")) {
					JSONObject wObj3 = gFList.getJSONObject(i).getJSONObject("rain");
					if (wObj3.has("3h")) {
						double rain = wObj3.getDouble("3h");
						DecimalFormat dfRain = new DecimalFormat("#.#");
						wO.Rain = dfRain.format(rain);
					}
				}
				
				wO.Type = WeatherObjectTypes.Forecast;
		    	retArr.add(wO);
			}catch (Exception e) 
			{
				Util.logException(e);
			}
		}
		return retArr;
	}
	
	public static List<WeatherObject> GetCondensedForecast() throws Exception {
		JSONObject gFObject = readWeather(GENRAL_FORCAST_URL);
		JSONArray gFList = gFObject.getJSONArray("list");
		
		//First gather all info
		
		//<day of month, <weather desc, number occurences>>
		Map<Integer, Map<String, Integer>> mostOccuringWeatherDesc = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, Double> highestTemp = new HashMap<Integer, Double>();
		Map<Integer, Double> highestWind = new HashMap<Integer, Double>();
		Map<String, String> descToIcon = new HashMap<String, String>();
		
		boolean ignoreTodayTimeLimit = true;
		boolean ignoreLastdayTimeLimit = true;
		int lastDayInForecast = -1;
		

		Date todaysDate = new Date(System.currentTimeMillis());
		SimpleDateFormat toDaysDayOfMonthFormat = new SimpleDateFormat("dd"); 
		toDaysDayOfMonthFormat.setTimeZone(TimeZone.getTimeZone("GMT+1")); 
		String toDaysDayOfMonthS = toDaysDayOfMonthFormat.format(todaysDate);
		Integer toDaysDayOfMonthI = Integer.parseInt(toDaysDayOfMonthS);
		
		//find out if first forecast today is after 21, if so, use the one we get.
		//also, find out if the last day in the forecast has forecasts after 0800, if not, include all day
		for(int i=0; i< gFList.length(); i++) {
			String tempS = gFList.getJSONObject(i).getString("dt");
			long dT = 0;dT = Long.parseLong(tempS);
			Date date = new Date(dT*1000L); 
			SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd"); 
			SimpleDateFormat timeOfDayFormat = new SimpleDateFormat("HH"); 
			// give a timezone reference for formatting (see comment at the bottom)
			dayOfMonthFormat.setTimeZone(TimeZone.getTimeZone("GMT+1")); 
			timeOfDayFormat.setTimeZone(TimeZone.getTimeZone("GMT+1")); 
			String dayOfMonthS = dayOfMonthFormat.format(date);
			String hourOfDayS = timeOfDayFormat.format(date);
			Integer dayOfMonthI = Integer.parseInt(dayOfMonthS);
			Integer hourOfDayI = Integer.parseInt(hourOfDayS);
			
			if (toDaysDayOfMonthI == dayOfMonthI && hourOfDayI > 8 && hourOfDayI < 22) {
				ignoreTodayTimeLimit = false;
			} 
			else if (toDaysDayOfMonthI != dayOfMonthI) 
			{
				if (lastDayInForecast != toDaysDayOfMonthI) {
					//New last day
					lastDayInForecast = toDaysDayOfMonthI;
					ignoreLastdayTimeLimit = true;
				}
				if (hourOfDayI > 8 && hourOfDayI < 22) {
					ignoreLastdayTimeLimit = false;
				}
			}
			
		}
		
		
		for(int i=0; i< gFList.length(); i++) {
			long dT = 0; 
			
			try {
				String tempS = gFList.getJSONObject(i).getString("dt");
				dT = Long.parseLong(tempS);
				Date date = new Date(dT*1000L); 
				SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd"); 
				SimpleDateFormat timeOfDayFormat = new SimpleDateFormat("HH"); 
				// give a timezone reference for formatting (see comment at the bottom)
				dayOfMonthFormat.setTimeZone(TimeZone.getTimeZone("GMT+1")); 
				timeOfDayFormat.setTimeZone(TimeZone.getTimeZone("GMT+1")); 
				String dayOfMonthS = dayOfMonthFormat.format(date);
				String hourOfDayS = timeOfDayFormat.format(date);
				Integer dayOfMonthI = Integer.parseInt(dayOfMonthS);
				Integer hourOfDayI = Integer.parseInt(hourOfDayS);

				//Only look at wheather between 0800-2200, the rest is probably not important - except for first day if today, and last day if no hours between 8-21 are given
				if (hourOfDayI < 8 || hourOfDayI > 21) 
				{
					if(dayOfMonthI == lastDayInForecast && ignoreLastdayTimeLimit) {
						//do nothing
					} 
					else if(dayOfMonthI == toDaysDayOfMonthI && ignoreTodayTimeLimit) 
					{
						//do nothing
					} 
					else 
					{
						continue;
					}
				}
				
				//Most occuring weather
				JSONArray wArr = gFList.getJSONObject(i).getJSONArray("weather");
				for(int j=0; j< wArr.length(); j++) {
					String wDesc = wArr.getJSONObject(j).getString("description");
					if (!descToIcon.containsKey(wDesc)) {
						String wIcon = wArr.getJSONObject(j).getString("icon");
						descToIcon.put(wDesc, wIcon);
					}
					
					if (mostOccuringWeatherDesc.containsKey(dayOfMonthI)) {
						Map<String, Integer> temp = mostOccuringWeatherDesc.get(dayOfMonthI);
						if(temp.containsKey(wDesc)) {
							temp.put(wDesc, temp.get(wDesc) + 1);
						} else {
							temp.put(wDesc, 1);
						}
						mostOccuringWeatherDesc.put(dayOfMonthI, temp);
					} else {
						Map<String, Integer> temp = new HashMap<String, Integer>();
						temp.put(wDesc, 1);
						mostOccuringWeatherDesc.put(dayOfMonthI, temp);
					}
				}
				
				//Temp
				JSONObject wObj = gFList.getJSONObject(i).getJSONObject("main");
				double tempMax = wObj.getDouble("temp_max");
				if (highestTemp.containsKey(dayOfMonthI) && highestTemp.get(dayOfMonthI) < tempMax) {
					highestTemp.put(dayOfMonthI, tempMax);
				} else if (!highestTemp.containsKey(dayOfMonthI)) {
					highestTemp.put(dayOfMonthI, tempMax);
				}
				
				//Wind
				JSONObject wObj2 = gFList.getJSONObject(i).getJSONObject("wind");
				double wind = wObj2.getDouble("speed");
				if (highestWind.containsKey(dayOfMonthI) && highestWind.get(dayOfMonthI) < wind) {
					highestWind.put(dayOfMonthI, wind);
				} else if (!highestWind.containsKey(dayOfMonthI)) {
					highestWind.put(dayOfMonthI, wind);
				}
			}catch (Exception e) 
			{
				Util.logException(e);
			}
		}
		
		//Now, summarize the data 
		Map<Integer, String> summarizedWeather = new HashMap<Integer, String>();
		
		for(Map.Entry<Integer, Map<String, Integer>> entry : mostOccuringWeatherDesc.entrySet()) {
		    Integer key = entry.getKey();
		    Map<String, Integer> value = entry.getValue();
		    int mostOccuringNumber = 0;
		    String mostOccuringDesc = "N/A";
		    
		    for(Map.Entry<String, Integer> entry2 : value.entrySet()) {
		    	Integer occurences = entry2.getValue();
		    	if (occurences > mostOccuringNumber) {
		    		mostOccuringNumber = occurences;
		    		mostOccuringDesc = entry2.getKey();
		    	}
		    }
		    summarizedWeather.put(key, mostOccuringDesc);
		}
		
		List<WeatherObject> retArr = new ArrayList<WeatherObject>();
		
		for(Map.Entry<Integer, String> entry : summarizedWeather.entrySet()) {
			try {
	    	Integer day = entry.getKey();
	    	String desc = entry.getValue();
	    	
	    	WeatherObject wO = new WeatherObject();
	    	wO.DayOfMonth = Integer.parseInt(day.toString());
	    	wO.Description = desc;
	    	
	    	//Only show day icons in forecasts
	    	String icon = descToIcon.get(desc);
	    	if (icon.endsWith("n")) {
	    		icon = icon.substring(0,  icon.length()-1) + "d";
	    	}
	    	
	    	boolean windy = false;
	    	if (highestWind.get(day) > 10) 
			{
				windy = true;
			}
	    	
	    	String iconName = GetRealIconName(icon, windy);
	    	wO.Icon = WeatherObject.buildImage(WeatherService.class.getResource("/img/50x50/" + iconName));
	    	wO.WindSpeed = highestWind.get(day).toString();
	    	
			DecimalFormat df = new DecimalFormat("#");
			wO.OutdoorTemperature = df.format(highestTemp.get(day));
	    	
	    	retArr.add(wO);
			} catch (Exception e) {
				Util.logException(e);
			}
		}

    	return retArr;
	}
	
	
	public static WeatherObject GetCurrentWeather() throws Exception {
		JSONObject gWObject = readWeather(GENERAL_OBSERVATION_URL);
		JSONArray gWWeather = gWObject.getJSONArray("weather");
		JSONObject gWWind = gWObject.getJSONObject("wind");

		WeatherObject weatherO = new WeatherObject();
		weatherO.Description = gWWeather.getJSONObject(0).getString("description");
		
		boolean windy = false;
		try {
			String tempS = gWWind.getString("speed");
			double tempD = Double.parseDouble(tempS);
			DecimalFormat df = new DecimalFormat("#.#");
			weatherO.WindSpeed = df.format(tempD);
			
			if (tempD > 10) 
			{
				windy = true;
			}
		}catch (Exception e) 
		{
			weatherO.OutdoorTemperature = "ERR";	
		}
		
		//Icon
		String weatherCode = gWWeather.getJSONObject(0).getString("icon");
		String iconName = GetRealIconName(weatherCode, windy);
		
		weatherO.Icon = WeatherObject.buildImage(WeatherService.class.getResource("/img/70x70/" + iconName));
		
		//Rain
		if (gWObject.has("rain")) {
			JSONObject wObj = gWObject.getJSONObject("rain");
			if (wObj != null) {
				double rain = wObj.getDouble("3h");
				DecimalFormat dfRain = new DecimalFormat("#.#");
				weatherO.Rain = dfRain.format(rain);
			}
		}
		
		//Save this one?
		Date date = new Date(System.currentTimeMillis()); 
		SimpleDateFormat timeOfDayFormat = new SimpleDateFormat("HH");
		SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd");
		String hourOfDayS = timeOfDayFormat.format(date);
		String dayOfMonthS = dayOfMonthFormat.format(date);
		Integer hourOfDayI = Integer.parseInt(hourOfDayS);
		Integer dayOfMonthI = Integer.parseInt(dayOfMonthS);
		if (hourOfDayI == 1) { 
			//New day, clear
			todaysObservations = new HashMap<>();
		} else if (staticTimesToRecord.contains(hourOfDayI) && !todaysObservations.containsKey(hourOfDayI)){
			weatherO.Hour = hourOfDayI;
			weatherO.DayOfMonth = dayOfMonthI;
			weatherO.OutdoorTemperature = HC2Interface.getTempDeviceStatus(341);
			weatherO.Type = WeatherObjectTypes.Observation;
			todaysObservations.put(hourOfDayI, weatherO);
		}

		return weatherO;
	}
	
	private static String GetRealIconName (String weatherCode, boolean windy) {
		String iconName = "unknown-weather.png";
		if (weatherCode == null) 
		{
			return iconName;
		}
		else if(weatherCode.equals("01d")) 
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
		else if(weatherCode.equals("02d")) 
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
		else if(weatherCode.equals("04d")) 
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
		else if(weatherCode.equals("09d")) 
		{
			iconName = "showers-day.png";
		} 
		else if(weatherCode.equals("09n")) 
		{
			iconName = "showers-night.png";
		} 
		else if(weatherCode.equals("10d")) 
		{
			iconName = "rain.png";
		} 
		else if(weatherCode.equals("10n")) 
		{
			iconName = "rain.png";
		} 
		else if(weatherCode.equals("11d")) 
		{
			iconName = "thunder-storm.png";
		} 
		else if(weatherCode.equals("11n")) 
		{
			iconName = "thunder-storm.png";
		} 
		else if(weatherCode.equals("13d")) 
		{
			iconName = "snow.png";
		} 
		else if(weatherCode.equals("13n")) 
		{
			iconName = "snow.png";
		} 
		else if(weatherCode.equals("50d")) 
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
		return iconName;
	}
}
