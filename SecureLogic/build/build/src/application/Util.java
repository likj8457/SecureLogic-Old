package application;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.BroadcastingClient;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class Util {
	private static HashMap<Long, List<String>> buffer = new HashMap<Long, List<String>>();
	private static HashMap<Integer, HashMap<String, Pair<Integer, Integer>>> temperatureHistory; //key is device id, String is the date of reading(MMdd), Pair is first high temp, then low
	private static HashMap<Integer, Integer> alarmTemperatures;
	private static boolean running = false;
	private static Thread thread = null;
	
	public static void LogToFile(String text) {
		try {
			synchronized (buffer) {
				long time = System.currentTimeMillis();
				List<String> list = Arrays.asList(text);
				Set<Long> keySet = buffer.keySet();
				for(Long key : keySet) {
					if (key.equals(Long.valueOf(time))) {
						list = new ArrayList<String>(buffer.get(key));
						list.add(text);
						break;
					}
				}
				buffer.put(Long.valueOf(time), list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void logException(Exception e) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos, true, "utf-8");
			e.printStackTrace(ps);
			String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			ps.close();
			
			LogToFile(content);
		} catch (Exception ex) {
			logException(ex);
		}
	}
	
	public static void startLogger() {
		temperatureHistory = deserializeTemperatureHistory();
		alarmTemperatures = new HashMap<>();
		
		running = true;
		thread = new Thread(new Runnable() {
	         @Override
	         public void run() {
	              while (running) {
	            	  try {
		            	  List<String> lines = new ArrayList<String>();
		            	  synchronized (buffer) {
		            		  if (buffer.size() > 0) {
			            		  for(Map.Entry<Long, List<String>> entry : buffer.entrySet()) {
			            			    Long key = entry.getKey();
			            			    List<String> value = entry.getValue();
	
			            			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
			            			    Date resultdate = new Date(key.longValue());
			            			    for(String text : value) {
			            			    	String line = sdf.format(resultdate) + " " + text;
			            			    	lines.add(line);
			            			    }
			            			}
			            		  buffer.clear();
		            		  }
		            	  }
		            	  
		            	  if (lines.size() > 0) {
		            		  Path file = Paths.get("securelogic.log");
		            		  Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		            	  }
		            	  
		            	  Thread.sleep(5000);
	            	  } catch (Exception e) {
	          			e.printStackTrace();
	            	  }
	              }
	         }
		});
		thread.start();
	}
	
	public static void stopLogger() 
	{
		running = false;
		try {
			if(thread != null && thread.isAlive()) 
			{
				thread.interrupt();
			}
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public static void turnOffScreen() 
	{
		runCommand("xset dpms force off");
	}	
	
	public static void turnOnScreen() 
	{
		runCommand("xset dpms force on");
		disableDPMS();
	}
	
	public static void disableDPMS() {
		runCommand("xset s off -dpms");
	}

	public static void updateFirmware() {
		runCommand("sudo reboot");
	}
	
	private static void runCommand(String command) {
	    Process p;
	    String s;
	    String reply = "";
	    try {
	        p = Runtime.getRuntime().exec(command);
	        BufferedReader br = new BufferedReader(
	            new InputStreamReader(p.getInputStream()));
	        while ((s = br.readLine()) != null)
	            reply += s + "\r\n";
	        p.waitFor();
	        LogToFile("Command run: " + command + " Status: " + p.exitValue() + "\r\n Reply: \r\n" + reply);
	        p.destroy();
	    } catch (Exception e) {
	    	logException(e);
	    }
	}
	
	public static HashMap<String, String> readProperties() {
		HashMap<String, String> map = new HashMap<>();
		Properties prop = new Properties();
    	InputStream input = null;

    	try {
    		String filename = System.getProperty("user.home") + java.io.File.separator + "config.properties";
    		LogToFile("Reading configfile: " + filename);
    		input = new FileInputStream(filename);

    		//load a properties file from class path, inside static method
    		prop.load(input);

    		for(Object o : prop.keySet()) 
    		{
    			String value = prop.getProperty((String)o, "");
    			map.put((String)o, value);
    		}
    	} catch (IOException ex) {
    		Util.logException(ex);
        } finally{
        	if(input!=null){
        		try {
        			input.close();
				} catch (IOException e) {
		    		Util.logException(e);
				}
        	}
        }

		return map;
	}

	public static int[] hex2Rgb(String colorStr) {
		if (colorStr == null || colorStr.length() < 6) {
			return null;
		}
		
		if (!colorStr.startsWith("#")) {
			colorStr = "#" + colorStr;
		}
		
	    return new int[] {
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) };
	}
	
	public static Pair<Integer, Integer> getTemperatureForDate(int deviceId, String date) {
		if (temperatureHistory.containsKey(deviceId)) {
        	if (temperatureHistory.get(deviceId).containsKey(date)) {
        		return temperatureHistory.get(deviceId).get(date);
        	}
        }

		return new Pair<Integer, Integer>(Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public static void updateTemperatureHistory (int deviceId, double dTemp) 
	{
		try {
			int temperature = (int)Math.round(dTemp);
			
			Date date = new Date();
		    SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
		    String dateString = sdf.format(date);

			Pair<Integer, Integer> currentHighLowTemp = new Pair<Integer, Integer>(temperature, temperature);
			if (temperatureHistory.containsKey(deviceId)) {
				if (temperatureHistory.get(deviceId).containsKey(dateString)) {
					Pair<Integer, Integer> highLowTemp = temperatureHistory.get(deviceId).get(dateString);
					boolean update = false;
					
					if (temperature > highLowTemp.getKey()) {
						currentHighLowTemp = new Pair<Integer, Integer>(temperature, highLowTemp.getValue());
						update = true;
					}
					
					if (temperature < highLowTemp.getValue()) {
						currentHighLowTemp = new Pair<Integer, Integer>(highLowTemp.getKey(), temperature);
						update = true;
					}
					
					if (update) {
						temperatureHistory.get(deviceId).put(dateString, currentHighLowTemp);
					}
				} else {
					temperatureHistory.get(deviceId).put(dateString, currentHighLowTemp);
				}
			} else {
				HashMap<String, Pair<Integer, Integer>> value = new HashMap<>();
				value.put(dateString,  currentHighLowTemp);
				temperatureHistory.put(deviceId, value);
			}
		} catch (Exception e) {
			Util.logException(e);
		}
	}
	
	public static void serializeTemperatureHistoryToDisk () {
		try
        {
               FileOutputStream fos = new FileOutputStream("temperatureHistory.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(temperatureHistory);
               oos.close();
               fos.close();
        }
		catch(IOException ioe)
        {
			Util.logException(ioe);
        }
	}
	
	private static HashMap<Integer, HashMap<String, Pair<Integer, Integer>>> deserializeTemperatureHistory () {
		HashMap<Integer, HashMap<String, Pair<Integer, Integer>>> temperatureHistory = null;
		try
	    {
	         FileInputStream fis = new FileInputStream("temperatureHistory.ser");
	         ObjectInputStream ois = new ObjectInputStream(fis);
	         temperatureHistory = (HashMap<Integer, HashMap<String, Pair<Integer, Integer>>>) ois.readObject();
	         ois.close();
	         fis.close();
	    }
		catch(IOException ioe)
	    {
	         Util.logException(ioe);
	    }
		catch(ClassNotFoundException c)
	    {
	         Util.logException(c);
	    }
		
		if (temperatureHistory == null) {
			temperatureHistory = new HashMap<>();
		}
		return temperatureHistory;
	}
	
	public static void setAlarmTemperature(int deviceId, int alarmTemp) {
		if (alarmTemp == getAlarmTemperature(deviceId)) {
			alarmTemperatures.remove(deviceId);
		} else {
			alarmTemperatures.put(deviceId, alarmTemp);
		}
	}
	
	public static int getAlarmTemperature(int deviceId) {
		return alarmTemperatures.get(deviceId) != null ? alarmTemperatures.get(deviceId) : Integer.MIN_VALUE;
	}
	
	public static HashMap<Integer, Integer> getDirectAccessToAlarmStore() {
		return alarmTemperatures;
	}
	
	public static void sendAllCurrentAlarmTemperatures() {
		for(Map.Entry<Integer, Integer> entry : alarmTemperatures.entrySet()) {
		    Integer deviceId = entry.getKey();
		    Integer temperature = entry.getValue();

		    byte[] buffer = concat(intToByteArray(deviceId), intToByteArray(temperature));
			
			try {
				BroadcastingClient bC = new BroadcastingClient();
				bC.broadcastPacket(buffer);
			} catch (Exception e) {
				Util.logException(e);
			}
		}
	}
	
	public static byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	public static byte[] concat(byte[]... arrays) {
	    int length = 0;
	    for (byte[] array : arrays) {
	        length += array.length;
	    }
	    byte[] result = new byte[length];
	    int pos = 0;
	    for (byte[] array : arrays) {
	        for (byte element : array) {
	            result[pos] = element;
	            pos++;
	        }
	    }
	    return result;
	}
	
}
