package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class TCPHeartbeat implements Heartbeat, Runnable{
    
    public ServerSocket socket;
    public int hbPort;

    public TCPHeartbeat(int port){
        try {
            this.socket = new ServerSocket(port);
            this.hbPort = port;
            Thread thread = new Thread(this);
            thread.start();
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void send(int instanceHbPort) throws SocketTimeoutException{
        Socket connection = null;
        BufferedWriter out = null;
        try {
            
            connection = new Socket("localhost", instanceHbPort);
            connection.setSoTimeout(6000);

            out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            String heartbeatMessage = "Are you alive?";

            out.write(heartbeatMessage);
       		out.flush();
        

        }catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
            e.printStackTrace();
        }finally{
			try {
				if(out != null) out.close();
				if(connection != null) connection.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }

    public boolean receive(){

        try {
            Socket client = this.socket.accept();
            BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String message = input.readLine();

            if(message.trim().equals("I'm alive!"))
                return true;
            else
                return false;

        }
        catch (IOException e) {
            e.printStackTrace();
            try {
				this.socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
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

        Socket connection = null;
        BufferedWriter out = null;

        while (true) {
            System.out.println("TCPHeartbeat: server instance waiting for hb request...");
            try {
                Socket client = this.socket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String message = input.readLine();

                System.out.println("TCPHeartbeat: received=" + message + "; from port=" + client.getPort());

                connection = new Socket("localhost", 7000);
                out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                String heartbeatMessage = "I'm alive!";

                out.write(heartbeatMessage);
                out.flush();
                System.out.println("TCPHeartbeat: sent hb response to LB...");
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    if(out != null) out.close();
                    if(connection != null) connection.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }


}
