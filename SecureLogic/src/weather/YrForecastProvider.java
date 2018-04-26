package weather;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import application.Util;
import weather.WeatherObject.TimeOfDayTypes;
import weather.WeatherObject.WeatherObjectTypes;
import zwave.fibaro.HC2Interface;

public class YrForecastProvider extends ForecastProvider {
	private static List<Integer> staticTimesToRecord = Arrays.asList(0, 6, 12, 18);

	public YrForecastProvider() {
		todaysObservations = new HashMap<>();
	}

	public List<WeatherObject> getFullForecast() {
		YrForecastParser yrForecastParser = new YrForecastParser();
		parse(yrForecastParser);

		List<WeatherObject> retArr = yrForecastParser.getForecasts();
		
		for(WeatherObject wObserved : todaysObservations.values()) {
			boolean includeObserved = true;
			for(WeatherObject wForcasted : retArr) {
				if (wForcasted.TimeOfDay == wObserved.TimeOfDay && wForcasted.DayOfMonth == wObserved.DayOfMonth) {
					includeObserved = false;
					break;
				}
			}
			if (includeObserved) {
				retArr.add(wObserved);
			}
		}
		
		return retArr;
	}

	public List<WeatherObject> getCondensedForecast() {
		YrForecastParser yrForecastParser = new YrForecastParser();
		parse(yrForecastParser);
		Date toDay = new Date(System.currentTimeMillis());
		Calendar dayCal = Calendar.getInstance();
		dayCal.setTime(toDay);
		int dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH);
		boolean todayInList = false;

		List<WeatherObject> retList = new ArrayList<WeatherObject>();
		for (WeatherObject wO : yrForecastParser.getForecasts()) {
			if (wO.TimeOfDay == TimeOfDayTypes.Day) {
				if (wO.DayOfMonth == dayOfMonth) {
					todayInList = true;
				}
				retList.add(wO);
			}
		}

		// if its late and timeofday > day (evening), then we missed a weatherobject for
		// today... lets grab the first in the list...
		if (!todayInList) {
			retList.add(0, yrForecastParser.getForecasts().get(0));
		}

		return retList;
	}

	public WeatherObject getCurrentWeather() {
		YrForecastParser yrForecastParser = new YrForecastParser();
		parse(yrForecastParser);
		if (yrForecastParser.getForecasts().size() > 0) {
			WeatherObject wO = yrForecastParser.getForecasts().get(0);
			try {
				wO.OutdoorTemperature = HC2Interface.getTempDeviceStatus(341);
			} catch (Exception e) {
				Util.logException(e);
			}
			wO.Type = WeatherObjectTypes.Observation;

			// Save this one?
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat timeOfDayFormat = new SimpleDateFormat("HH");
			String hourOfDayS = timeOfDayFormat.format(date);
			;
			Integer hourOfDayI = Integer.parseInt(hourOfDayS);
			if (hourOfDayI == 1) {
				// New day, clear
				todaysObservations = new HashMap<>();
			} else if (staticTimesToRecord.contains(hourOfDayI) && !todaysObservations.containsKey(hourOfDayI)) {
				todaysObservations.put(hourOfDayI, wO);
			}

			return wO;
		}
		return null;
	}

	private void parse(YrForecastParser yrForecastParser) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse("https://www.yr.no/place/Sweden/Scania/Trelleborg/forecast.xml", yrForecastParser);
		} catch (Exception e) {
			Util.logException(e);
		}
	}

	class YrForecastParser extends DefaultHandler {
		private boolean inForecast = false;
		private boolean inTabular = false;
		private WeatherObject currentWeatherObject = null;

		private Instant cutOff;

		private List<WeatherObject> forecasts;

		public List<WeatherObject> getForecasts() {
			return forecasts;
		}

		private YrForecastParser() {
			Date toDay = new Date(System.currentTimeMillis());
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(toDay);
			dayCal.add(Calendar.DAY_OF_YEAR, 4);
			dayCal.set(Calendar.HOUR_OF_DAY, 23);

			cutOff = Instant.ofEpochSecond(dayCal.getTimeInMillis() / 1000);

			forecasts = new ArrayList<>();

			inForecast = false;
			inTabular = false;
			currentWeatherObject = null;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {

			if (qName.equalsIgnoreCase("forecast")) {
				inForecast = true;
			} else if (qName.equalsIgnoreCase("tabular") && inForecast) {
				inTabular = true;
			} else if (qName.equalsIgnoreCase("time") && inForecast && inTabular) {
				String from = attributes.getValue("from");
				Instant fromInstant = ZonedDateTime.parse(from + "+01:00").toInstant();
				if (fromInstant.compareTo(cutOff) <= 0) {
					currentWeatherObject = new WeatherObject();
					ZonedDateTime zDT = ZonedDateTime.parse(from + "+01:00");

					int dayOfMonth = zDT.toLocalDate().getDayOfMonth();
					currentWeatherObject.DayOfMonth = dayOfMonth;

					int hour = zDT.toLocalTime().getHour();
					currentWeatherObject.Hour = hour;

					String period = attributes.getValue("period");
					if (period.equals("0")) {
						currentWeatherObject.TimeOfDay = WeatherObject.TimeOfDayTypes.Night;
					} else if (period.equals("1")) {
						currentWeatherObject.TimeOfDay = WeatherObject.TimeOfDayTypes.Morning;
					} else if (period.equals("2")) {
						currentWeatherObject.TimeOfDay = WeatherObject.TimeOfDayTypes.Day;
					} else if (period.equals("3")) {
						currentWeatherObject.TimeOfDay = WeatherObject.TimeOfDayTypes.Evening;
					}

					currentWeatherObject.Type = WeatherObject.WeatherObjectTypes.Forecast;
				}
			} else if (qName.equalsIgnoreCase("symbol") && inForecast && inTabular && currentWeatherObject != null) {
				String desc = attributes.getValue("name");
				currentWeatherObject.Description = desc;

				String image = attributes.getValue("var");
				currentWeatherObject.IconName = image;
			} else if (qName.equalsIgnoreCase("precipitation") && inForecast && inTabular
					&& currentWeatherObject != null) {
				String rain = attributes.getValue("value");
				currentWeatherObject.Rain = rain;
			} else if (qName.equalsIgnoreCase("windSpeed") && inForecast && inTabular && currentWeatherObject != null) {
				String wind = attributes.getValue("mps");
				currentWeatherObject.WindSpeed = wind;

				String wDesc = attributes.getValue("name");
				currentWeatherObject.WindDescription = wDesc;
			} else if (qName.equalsIgnoreCase("temperature") && inForecast && inTabular
					&& currentWeatherObject != null) {
				String temp = attributes.getValue("value");
				currentWeatherObject.OutdoorTemperature = temp;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {

			if (qName.equalsIgnoreCase("forecast")) {
				inForecast = false;
			} else if (qName.equalsIgnoreCase("tabular")) {
				inTabular = false;
			} else if (qName.equalsIgnoreCase("time")) {
				if (currentWeatherObject != null) {
			    	boolean windy = false;
			    	try {
			    		windy = Integer.parseInt(currentWeatherObject.WindSpeed) >= 10;
			    	} catch (Exception e) {
			    		Util.logException(e);
			    	}
			    	
			    	try {
			    		String iconName = GetRealIconName(currentWeatherObject.IconName, windy);
			    		currentWeatherObject.Icon = WeatherObject.buildImage(WeatherService.class.getResource("/img/50x50/" + iconName));
			    	} catch (Exception e) {
			    		Util.logException(e);
			    	}
			    	
					forecasts.add(currentWeatherObject);
					currentWeatherObject = null;
				}
			}
		}
	}
}