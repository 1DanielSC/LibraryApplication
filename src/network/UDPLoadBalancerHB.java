package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDPLoadBalancerHB implements Heartbeat {
    public DatagramSocket socket;
    public int hbPort;

    public UDPLoadBalancerHB(int port){
        try {
            this.socket = new DatagramSocket(port);
            this.hbPort = port;
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

            System.out.println("UDP loadbalancer: pacote recebido: " + hbMessage);

            if(hbMessage.trim().equals("I'm alive!")){
                System.out.println("UDP LB: the instance is alive!");
                return true;
            }
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
}
