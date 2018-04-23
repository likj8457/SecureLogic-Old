package zwave.fibaro;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.IntStream;

import org.json.JSONObject;

import application.Util;

public class HC2Interface {
	private static HashMap<Integer, Boolean> lightsOn = new HashMap<Integer, Boolean>();
	private static HashMap<Integer, RGBState> rgbValues = new HashMap<Integer, RGBState>();
	private static HashMap<Integer, Integer> luxValues = new HashMap<Integer, Integer>();
	private static HashMap<Integer, String> tempValues = new HashMap<Integer, String>();
	private static HashMap<String, String> variableValues = new HashMap<String, String>();
	private static HashMap<Integer, Boolean> garageDoorValues = new HashMap<Integer, Boolean>();
	private static boolean running;
	private static int[] onOffDevices = new int[] {252,349,350,355,356,364,366,368,389,393}; //Devices of interest
	private static int[] garageDoorDevices = new int[] {410}; //Devices of interest
	private static int[] rgbDevices = new int[] {312,372,412}; //Devices of interest
	private static int[] tempDevices = new int[] {247,269,275,289,296,341,345,359,421,436,451}; //Devices of interest
	private static int[] luxDevices = new int[] {437}; //Devices of interest
	private static String[] variables = new String[] {"AlarmType"};
	private static String BASE_URL = "";
	private static String USER_PASS = "";
	private static Thread runThread;
	private static int REQUEST_TYPE_GET = 0;
	private static int REQUEST_TYPE_POST = 1;
		
	public static void init (String baseURL, String userPass) 
	{
		BASE_URL = baseURL;
		USER_PASS = userPass;
	}
	

	public static void stop() {
		running = false;
		try {
			if(runThread != null && runThread.isAlive()) 
			{
				runThread.interrupt();
			}
		} 
		catch(Exception e) 
		{
			Util.logException(e);
		}
	}
	
	public static void start() {
		running = true;
		runThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (running) {
					long now = System.currentTimeMillis();
					
					//onOffDevices
					for(int deviceId : onOffDevices) 
					{
						try {
							String jsonString = requestUrl(BASE_URL + "/api/devices/" + String.valueOf(deviceId), REQUEST_TYPE_GET);
							JSONObject device = new JSONObject(jsonString);
							JSONObject properties = device.getJSONObject("properties");
							lightsOn.put(deviceId, properties.getBoolean("value"));
						} catch (Exception e) {
							Util.logException(e);
						}
					}
					
					//RGBDevices
					for(int deviceId : rgbDevices) 
					{
						try {
							String jsonString = requestUrl(BASE_URL + "/api/devices/" + String.valueOf(deviceId), REQUEST_TYPE_GET);
							JSONObject device = new JSONObject(jsonString);
							JSONObject properties = device.getJSONObject("properties");
							String colors = properties.getString("lastColorSet");
							String[] parts = colors.split(",");
							RGBState rgbState = new RGBState(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), properties.getInt("value"));
							rgbValues.put(deviceId, rgbState);
						} catch (Exception e) {
							Util.logException(e);
						}
					}
					
					//luxDevices
					for(int deviceId : luxDevices) 
					{
						try {
							String jsonString = requestUrl(BASE_URL + "/api/devices/" + String.valueOf(deviceId), REQUEST_TYPE_GET);
							JSONObject device = new JSONObject(jsonString);
							JSONObject properties = device.getJSONObject("properties");
							double dLux = properties.getDouble("value");
							luxValues.put(deviceId, (int)Math.round(dLux));
						} catch (Exception e) {
							Util.logException(e);
						}
					}
					
					//tempDevices
					DecimalFormat df = new DecimalFormat("#.#");
					for(int deviceId : tempDevices) 
					{
						try {
							String jsonString = requestUrl(BASE_URL + "/api/devices/" + String.valueOf(deviceId), REQUEST_TYPE_GET);
							JSONObject device = new JSONObject(jsonString);
							JSONObject properties = device.getJSONObject("properties");
							double value = properties.getDouble("value");
							 synchronized(tempValues) {
								 tempValues.put(deviceId, df.format(value) + "\u00b0");
							 }
							Util.updateTemperatureHistory(deviceId, value);
						} catch (Exception e) {
							Util.logException(e);
						}
					}
					
					//variables
					for(String variable : variables) 
					{
						try {
							String jsonString = requestUrl(BASE_URL + "/api/globalVariables/" + variable, REQUEST_TYPE_GET);
							JSONObject variableJson = new JSONObject(jsonString);
							String value = variableJson.getString("value");
							 synchronized(variableValues) {
								 variableValues.put(variable, value);
							 }
						} catch (Exception e) {
							Util.logException(e);
						}
					}
					
					//garage door
					for(int deviceId : garageDoorDevices) 
					{
						try {
							String jsonString = requestUrl(BASE_URL + "/api/devices/" + String.valueOf(deviceId), REQUEST_TYPE_GET);
							JSONObject device = new JSONObject(jsonString);
							JSONObject properties = device.getJSONObject("properties");
							garageDoorValues.put(deviceId, properties.getString("value").equals("Opened"));
						} catch (Exception e) {
							Util.logException(e);
						}
					}
					
					long andNow = System.currentTimeMillis();
					long diff = andNow - now;
					long sleepTime = 60000 - diff;
					
					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (Exception e) {
							//Since we interrupt on purpose we will sleep for a short while, then continue
							try {
								Thread.sleep(300);
							} catch (Exception ex) {
								//this is not on purpose
								Util.logException(ex);
							}
						}
					}
				}
			}
		}) ;
		runThread.start();
	}
	
	public static void forceUpdate() {
		runThread.interrupt();
	}
	
	public static boolean getLightDeviceStatus (int deviceId) throws Exception {
		synchronized (lightsOn) {
			if (lightsOn.containsKey(deviceId)) {
				return lightsOn.get(deviceId);
			}
		}
		
		if (!IntStream.of(onOffDevices).anyMatch(x -> x == deviceId)) {
			throw new Exception("Device not in subscription");
		} else {
			return false;	
		}
	}
	
	public static boolean setLightDeviceStatus (int deviceId, int target) throws Exception
	{
		String action = "turnOff";
		if (target == 1) 
		{
			action = "turnOn";
		}
		
		String reply = requestUrl(BASE_URL + "/api/devices/" + deviceId + "/action/" + action, REQUEST_TYPE_POST);
		JSONObject all = new JSONObject(reply);
		lightsOn.put(deviceId, target == 1);
		return true;
	}
	
	public static boolean getGarageDoorDeviceStatus (int deviceId) throws Exception {
		synchronized (garageDoorValues) {
			if (garageDoorValues.containsKey(deviceId)) {
				return garageDoorValues.get(deviceId);
			}
		}
		
		if (!IntStream.of(garageDoorDevices).anyMatch(x -> x == deviceId)) {
			throw new Exception("Device not in subscription");
		} else {
			return false;	
		}
	}
	
	public static boolean setGarageDoorDeviceStatus (int deviceId, int target) throws Exception
	{
		String action = "close";
		if (target == 1) 
		{
			action = "open";
		}
		
		String reply = requestUrl(BASE_URL + "/api/devices/" + deviceId + "/action/" + action, REQUEST_TYPE_POST);
		JSONObject all = new JSONObject(reply);
		garageDoorValues.put(deviceId, target == 1);
		return true;
	}
	
	public static RGBState getRGBDeviceStatus (int deviceId) throws Exception {
		synchronized (rgbValues) {
			if (rgbValues.containsKey(deviceId)) {
				return rgbValues.get(deviceId);
			}
		}
		
		if (!IntStream.of(rgbDevices).anyMatch(x -> x == deviceId)) {
			throw new Exception("Device not in subscription");
		} else {
			return null;	
		}
	}
	
	public static boolean setRGBDeviceStatus (int deviceId, RGBState rgbState) throws Exception
	{
		String action = "turnOn";
		if (rgbState.getLevel() == 0) 
		{
			action = "turnOff";
		}
		
		requestUrl(BASE_URL + "/api/devices/" + deviceId + "/action/" + action, REQUEST_TYPE_POST);

		if (rgbState.getLevel() > 0) 
		{
			requestUrl(BASE_URL + "/api/devices/" + deviceId + "/action/setColor", REQUEST_TYPE_POST, String.format("{\"args\" : [%1$d,%2$d,%3$d,%4$d]}", rgbState.getR(), rgbState.getG(), rgbState.getB(), rgbState.getW()));
			requestUrl(BASE_URL + "/api/devices/" + deviceId + "/action/setValue", REQUEST_TYPE_POST, String.format("{\"args\" : [%1$d]}", rgbState.getLevel()));
			 
		}
		
		rgbValues.put(deviceId, rgbState);
		return true;
	}
	
	public static boolean setRGBDeviceOnOff (int deviceId, boolean on) throws Exception
	{
		rgbValues.get(deviceId).setLevel(on?100:0);
		return setRGBDeviceStatus (deviceId, rgbValues.get(deviceId));
	}
	
	public static String getTempDeviceStatus (int deviceId) throws Exception {
		synchronized (tempValues) {
			if (tempValues.containsKey(deviceId)) {
				return tempValues.get(deviceId);
			}
		}		
		
		if (!IntStream.of(tempDevices).anyMatch(x -> x == deviceId)) {
			throw new Exception("Device not in subscription");
		} else {
			return "-";	
		}
	}
	
	public static int getLuxDeviceStatus (int deviceId) throws Exception {
		synchronized (luxValues) {
			if (luxValues.containsKey(deviceId)) {
				return luxValues.get(deviceId);
			}
		}
		
		if (!IntStream.of(luxDevices).anyMatch(x -> x == deviceId)) {
			throw new Exception("Device not in subscription");
		} else {
			return 0;	
		}
	}
	
	public static String getVariableValue (String variable) throws Exception {
		synchronized (variableValues) {
			if (variableValues.containsKey(variable)) {
				return variableValues.get(variable);
			}
		}
		
		if (!Arrays.asList(variables).contains(variable)) {
			throw new Exception("Device not in subscription");
		} else {
			return "";	
		}
	}
	
	public static boolean runScene(int sceneId, String pin) throws Exception {
		String jsonPayload = "";
		if (pin != null && pin.trim().length() > 0) {
			jsonPayload = "{\"pin\": \"" + pin + "\"}";
		}
		
		try {
			String reply = requestUrl(BASE_URL + "/api/scenes/" + sceneId + "/action/start", REQUEST_TYPE_POST, jsonPayload);
			return true;
		} catch(Exception e) {
			if (e.getMessage().startsWith("Request exited with code")) {
				return false;
			} else {
				throw e;
			}
		}
	}
	
	private static String requestUrl(String url, int requestType) throws Exception 
	{
		return requestUrl(url, requestType, "");
	}
	
    private static String requestUrl(String url, int requestType, String jsonPayload) throws Exception 
	{
    	HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    	if (requestType == 1) 
    	{
    		conn.setRequestMethod("POST");
    	} else {
    		conn.setRequestMethod("GET");
    	}
		
		if (USER_PASS != null && USER_PASS.length() > 0) {
		    String authStr = Base64.getEncoder()
		    		.encodeToString(USER_PASS.getBytes());
			//setting Authorization header
		    conn.setRequestProperty("Authorization", "Basic " + authStr);
		}
		
		conn.setRequestProperty( "Content-type", "application/json");
		// change the timeout to taste, I like 1 second
		conn.setReadTimeout(1000);
		
		if (requestType == 1) 
    	{
			conn.setDoOutput(true);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
	        outputStreamWriter.write(jsonPayload);
	        outputStreamWriter.flush();
    	}
		
		int responseCode = conn.getResponseCode();
		
		InputStream inputStream = conn.getInputStream();
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			int cp;
			while ((cp = reader.read()) != -1) {
				stringBuilder.append((char) cp);
			}
		} finally {
			inputStream.close();
		}
		
		if (responseCode != 200 && responseCode != 202) {
			throw new Exception ("Request exited with code: " + responseCode + ", Reply (if any): " + stringBuilder.toString());
		}
		

		return stringBuilder.toString();
	}
}
