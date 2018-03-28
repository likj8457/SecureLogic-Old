package zwave.vera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

import application.Util;

public class VeraInterface {
	private static HashMap<Integer, Boolean> lightsOn = new HashMap<Integer, Boolean>();
	private static HashMap<Integer, Integer> luxValues = new HashMap<Integer, Integer>();
	private static HashMap<Integer, String> tempValues = new HashMap<Integer, String>();
	private static boolean running;
	private static int[] lightDevices = new int[] {6,28,29,30,35,45,57,69,82,112,129,130,133,134}; //Devices of interest
	private static int[] tempDevices = new int[] {16,32,65,75,90,118,121,124}; //Devices of interest
	private static int[] luxDevices = new int[] {33}; //Devices of interest
	private static String VERA_BASE_URL = "http://192.168.0.120:3480/";
	private static Thread runThread;
		
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
					String jsonString = "";
					JSONObject all = null;
					JSONArray devices = null;
					try {
						jsonString = readStringFromUrl(VERA_BASE_URL + "data_request?id=user_data");
						all = new JSONObject(jsonString);
						devices = all.getJSONArray("devices");
					} catch (Exception e) {
						Util.logException(e);
					}
					//lights
					try {
						for (int i = 0; i < devices.length(); i++) 
						{
							int curDevice = devices.getJSONObject(i).getInt("id");
							if (IntStream.of(lightDevices).anyMatch(x -> x == curDevice)) 
							{
								JSONArray states = devices.getJSONObject(i).getJSONArray("states");
								for (int j = 0; j < states.length(); j++) 
								{
									if (states.getJSONObject(j).getString("service").equals("urn:upnp-org:serviceId:SwitchPower1") &&
											states.getJSONObject(j).getString("variable").equals("Status")) 
									{
										 int value = states.getJSONObject(j).getInt("value");
										 synchronized(lightsOn) {
											 lightsOn.put(curDevice, value == 1);
										 }
									}
								}
							}
						}
					} catch (Exception e) {
						Util.logException(e);
					}
					
					//lux
					try {
						for (int i = 0; i < devices.length(); i++) 
						{
							int curDevice = devices.getJSONObject(i).getInt("id");
							if (IntStream.of(luxDevices).anyMatch(x -> x == curDevice)) 
							{
								JSONArray states = devices.getJSONObject(i).getJSONArray("states");
								for (int j = 0; j < states.length(); j++) 
								{
									if (states.getJSONObject(j).getString("service").equals("urn:micasaverde-com:serviceId:LightSensor1") &&
											states.getJSONObject(j).getString("variable").equals("CurrentLevel")) 
									{
										 int value = states.getJSONObject(j).getInt("value");
										 synchronized(luxValues) {
											 luxValues.put(curDevice, value);
										 }
									}
								}
							}
						}
					} catch (Exception e) {
						Util.logException(e);
					}
					
					// { "service": "urn:upnp-org:serviceId:TemperatureSensor1", "variable": "CurrentTemperature", "id": 3, "value": "22.30" }
					
					//temp
					try {
						DecimalFormat df = new DecimalFormat("#.#");
						for (int i = 0; i < devices.length(); i++) 
						{
							int curDevice = devices.getJSONObject(i).getInt("id");
							if (IntStream.of(tempDevices).anyMatch(x -> x == curDevice)) 
							{
								JSONArray states = devices.getJSONObject(i).getJSONArray("states");
								for (int j = 0; j < states.length(); j++) 
								{
									if (states.getJSONObject(j).getString("service").equals("urn:upnp-org:serviceId:TemperatureSensor1") &&
											states.getJSONObject(j).getString("variable").equals("CurrentTemperature")) 
									{
										 double value = states.getJSONObject(j).getDouble("value");
										 synchronized(tempValues) {
											 tempValues.put(curDevice, df.format(value) + "\u00b0");
										 }
									}
								}
							}
						}
					} catch (Exception e) {
						Util.logException(e);
					}
					
					long andNow = System.currentTimeMillis();
					long diff = andNow - now;
					long sleepTime = 60000 - diff;
					
					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (Exception e) {
							Util.logException(e);
						}
					}
				}
			}
		}) ;
		runThread.start();
	}
	
	public static boolean getLightDeviceStatus (int deviceId) throws Exception {
		synchronized (lightsOn) {
			if (lightsOn.containsKey(deviceId)) {
				return lightsOn.get(deviceId);
			}
		}
		throw new Exception("Device not in subscription");
	}
	
	public static boolean setLightDeviceStatus (int deviceId, int target) throws Exception
	{
		String reply = readStringFromUrl(VERA_BASE_URL + "data_request?id=action&output_format=json&DeviceNum=" + deviceId + "&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=" + target);
		JSONObject all = new JSONObject(reply);
		lightsOn.put(deviceId, target == 1);
		return true;
	}
	
	public static String getTempDeviceStatus (int deviceId) throws Exception {
		synchronized (tempValues) {
			if (tempValues.containsKey(deviceId)) {
				return tempValues.get(deviceId);
			}
		}
		throw new Exception("Device not in subscription");
	}
	
	public static int getLuxDeviceStatus (int deviceId) throws Exception {
		synchronized (luxValues) {
			if (luxValues.containsKey(deviceId)) {
				return luxValues.get(deviceId);
			}
		}
		throw new Exception("Device not in subscription");
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
}
