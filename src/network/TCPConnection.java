package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TCPConnection {

    public Socket socket;
    public BufferedReader input;

    public TCPConnection(Socket socketFromRequest){
        this.socket = socketFromRequest;
    }


    public Message receive(){

        try {
            
            this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			Message msg = this.deserializeMessage(this.input.readLine().getBytes());

            return msg;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

    public void send(Message message){
        BufferedWriter out = null;
        try {

            out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));

            byte[] msg = this.serializeMessage(message);

			out.write(new String(msg));
       		out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
			try {
				if(out != null) out.close();
				if(this.socket != null) this.socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }

























    private byte[] serializeMessage(Message message){

		Gson gson = new GsonBuilder()
		        .setLenient().serializeNulls()
		        .create();
		
		byte[] serializedMessage = new byte[1024];
		String stringMessage = gson.toJson(message);
		serializedMessage = stringMessage.getBytes();

		return serializedMessage;
	}


    private Message deserializeMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), Message.class);
	}



    private byte[] serializeMessage(DatabaseMessage databaseMessage){

		Gson gson = new GsonBuilder()
		        .setLenient().serializeNulls()
		        .create();
		
		byte[] serializedMessage = new byte[1024];
		String stringMessage = gson.toJson(databaseMessage);
		serializedMessage = stringMessage.getBytes();

		return serializedMessage;
	}

    private DatabaseMessage deserializeDatabaseMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), DatabaseMessage.class);
	}

}
