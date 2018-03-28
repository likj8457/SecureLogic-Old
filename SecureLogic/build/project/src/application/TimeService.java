package application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import javafx.application.Platform;
import javafx.scene.text.Text;

public class TimeService {
	
	private static long minute;
	private static long dateSetOn;
	private static Date date;
	private static boolean running = true;
	private static Thread timerThread;
	private static Text tT;
	private static Text dT;
	private static long lastRunTime = 0;
	
	public static void subscribeToTimeUpdates(Text tT, Text dT)
	{
		try {              
			TimeService.tT = tT;
			TimeService.dT = dT;
		} catch (Exception e) {
			Util.logException(e);
		}
	}
	
	public static void start() throws Exception {
		if (TimeService.tT == null || TimeService.dT == null) {
			throw new Exception ("You have to subscribe first");
		}
		running = true;
		TimeService.run();
	}
	
	private static void run() {
		try 
		{
			timerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while(running) {
					try {
				        long millisTillNextMinute = 60000;
				        long now = System.currentTimeMillis();
				        lastRunTime = now;
				        boolean success = true;
				        
						try {
							NTPUDPClient client = new NTPUDPClient();
							 client.open();
							 InetAddress hostAddr = InetAddress.getByName("time.google.com");
							 TimeInfo info = client.getTime(hostAddr);
							 
							NtpV3Packet message = info.getMessage();
			
					        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
					        
					        if(xmitNtpTime.getTime()/60000 != minute) {
					        	minute = xmitNtpTime.getTime()/60000;
					        	
					        	date = new Date(xmitNtpTime.getTime());
					        	dateSetOn = System.currentTimeMillis();
					        	updateClock (date);
					        }
	
					        long millisPastMinute = xmitNtpTime.getTime() % 60000;
					        millisTillNextMinute = 60000 - millisPastMinute;
							client.close();
						} catch (Exception e) {
							success = false;
							Util.logException(e);
						}
						
						if (!success && (System.currentTimeMillis() - dateSetOn) > 30000) {
							try {
								long millisSinceLastUpdate = System.currentTimeMillis() - dateSetOn;
								Date nextMinute = new Date(date.getTime() + millisSinceLastUpdate);
								if (nextMinute.getTime() - date.getTime() > 59000) {
									updateClock(nextMinute);
								}
								millisTillNextMinute -= (System.currentTimeMillis()-now);
								if (millisTillNextMinute < 0) {
									millisTillNextMinute = 0;
								}
							} catch (Exception e) {
								Util.logException(e);
							}
						}

				        //sleep for millisTillNextMinute, then update time again
				        Thread.sleep(millisTillNextMinute);
					} catch (Exception e) {
						Util.logException(e);
					}
				}
			}
        	   
           });
           timerThread.start();
	     
		} catch (Exception e) {
			Util.logException(e);
		}
	}
	
	private static void updateClock (Date date) {
		DateFormat formatterForTime = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String timeFormatted = formatterForTime.format(date);
        DateFormat formatterForDate = new SimpleDateFormat("EEE MMM d, YYYY", Locale.ENGLISH);
        String dateFormatted = formatterForDate.format(date);
        Calendar cal= Calendar.getInstance();
        cal.setTime(date);
		
        Platform.runLater(new Runnable() {
			@Override
			public void run() {
		        tT.setText(timeFormatted);
		        dT.setText(dateFormatted);
			}
        });
	}
	
	public static boolean isRunning() 
	{
		if (System.currentTimeMillis() - lastRunTime > 60000) 
		{
			running = false;
		}
		return running;
	}

	public static void stop() 
	{
		running = false;
		try {
			if(timerThread != null && timerThread.isAlive()) 
			{
				timerThread.interrupt();
			}
		} 
		catch(Exception e) 
		{
			Util.logException(e);
		}
		
	}
}
