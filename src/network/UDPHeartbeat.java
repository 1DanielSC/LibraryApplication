package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDPHeartbeat implements Heartbeat, Runnable {

    public DatagramSocket socket;
    public int hbPort;

    public UDPHeartbeat(int port){
        try {
            this.socket = new DatagramSocket(port);
            this.hbPort = port;
            Thread thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(int instanceHbPort) throws SocketTimeoutException{
        String hbMessage = "Are you alive?";
        try {
            byte[] packetBytes = hbMessage.getBytes();
            
            DatagramPacket packetToSend = new DatagramPacket(packetBytes, packetBytes.length, InetAddress.getByName("localhost"), instanceHbPort);

            this.socket.send(packetToSend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean receive(){
        try {
            byte[] packet = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(packet,packet.length);
            
            this.socket.receive(receivedPacket);
            String hbMessage = new String(receivedPacket.getData());

            if(hbMessage.equals("I'm alive!"))
                return true;
            else
                return false;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void setSoTimeout(int timeout){
        try {
            this.socket.setSoTimeout(timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run(){
        while (true) {
            
            try {
                byte[] packet = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(packet,packet.length);
                this.socket.receive(receivedPacket);

                String replyHbMessage = "I'm alive";

                byte[] packetBytes = replyHbMessage.getBytes();
                DatagramPacket packetToSend = new DatagramPacket(packetBytes, packetBytes.length, InetAddress.getByName("localhost"), receivedPacket.getPort());
                this.socket.send(packetToSend);

            } catch (IOException e) {
                
                e.printStackTrace();
            }
        }
    }
}
