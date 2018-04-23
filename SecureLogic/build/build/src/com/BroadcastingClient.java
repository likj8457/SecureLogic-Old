package com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class BroadcastingClient {
    private DatagramSocket socket;
    private InetAddress address;

    public BroadcastingClient() throws Exception {
        this.address = InetAddress.getByName("255.255.255.255");
        
        initializeSocketForBroadcasting();
    }

    private void initializeSocketForBroadcasting() throws SocketException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
    }

    public void broadcastPacket(byte[] buf) throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4145);
        socket.send(packet);
    }

    public void close() {
        socket.close();
    }
}