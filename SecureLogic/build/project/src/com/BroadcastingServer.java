package com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import Tab.SecureTab;
import Tab.TemperatureTab;
import application.Util;

public class BroadcastingServer extends Thread {

    protected DatagramSocket socket = null;
    protected boolean running;
    protected byte[] buf = new byte[256];
    private SecureTab theTab;
    
    public BroadcastingServer(SecureTab tab) throws IOException {
    	theTab = tab;
        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(4145));
    }

    public void run() {
        running = true;

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                byte[] source = packet.getData();
                byte[] deviceArray = new byte[4];
                byte[] temperatureArray = new byte[4];
                System.arraycopy(source, 0, deviceArray, 0, deviceArray.length);
                System.arraycopy(source, deviceArray.length, temperatureArray, 0, temperatureArray.length);
                int deviceId = fromByteArray(deviceArray);
                int temperature = fromByteArray(temperatureArray);
                if (deviceId == 0 && temperature == 0) {
                	//This a notification from a new terminal, and it needs all set alarms
                	Util.sendAllCurrentAlarmTemperatures();
                } else {
                	//New alarm temperature
                	Util.setAlarmTemperature(deviceId, temperature);
                }
            } catch (IOException e) {
            	Util.logException(e);
            }
        }
        socket.close();
    }
    
	public void stopServer() {
		running = false;
		try {
			if(this != null && this.isAlive()) 
			{
				this.interrupt();
			}
		} 
		catch(Exception e) 
		{
			Util.logException(e);
		}
	}
	
	private int fromByteArray(byte[] bytes) {
	     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}
}