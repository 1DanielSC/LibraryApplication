package network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HTTPHandler implements NetworkAccess {

    public ServerSocket socket;

	public Socket jmeterSocket;

	public int serverPort;

    public HTTPHandler(int port) throws IOException{
        
        this.socket = new ServerSocket(port);
        this.serverPort = port;
        
    }

    
    
    public Message receive() throws IOException{
        try {
            Socket nextClient = this.socket.accept();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(nextClient.getInputStream()));
            
            String headerLine = in.readLine();
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
            System.out.println("HTTP method= " + httpMethod);

            if(httpMethod.equals("HTTP/1.0")){
                System.out.println("HTTP: ENTREI AQUIIII");
                return this.receiveResponse(in);
            }
            else if(httpMethod.equals("GET")){
                String resourceRequested = tokenizer.nextToken();
                if(resourceRequested.equals("/buy") ||
                resourceRequested.equals("/sell") ||
                resourceRequested.equals("/login") ||
                resourceRequested.equals("/login/create user")){
                    this.jmeterSocket = nextClient;
                }
                System.out.println("");
                String headers = in.readLine();
                String emptyLine = in.readLine();
                String body = in.readLine();
                //System.out.println("Body: " + body);
                in.readLine();
                in.readLine();
                in.readLine();
                
                Message bodyObject = null;
                try {
                	bodyObject = this.deserializeMessage(body.getBytes());
                }catch(Exception e) {
                	body = in.readLine();
                	bodyObject = this.deserializeMessage(body.getBytes());
                }
                

                
                if(bodyObject.getAction().equals("/sell") || 
                bodyObject.getAction().equals("/buy") || 
                bodyObject.getAction().equals("/login") || 
                bodyObject.getAction().equals("/login/create user")){
                    
                    this.jmeterSocket = nextClient;
                    bodyObject.setPort(nextClient.getPort());
                }
                
                
                System.out.println("HTTP Handler: recebi: " + bodyObject.toString());
                return bodyObject;
            }
            else{
                System.out.println("The HTTP method is not recognized");
                return null;
            }
            
            
        } catch (IOException e) {
            e.printStackTrace();
            try {
				this.socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
        }
        
        return null;
    }
    
    
    public Message receiveResponse(BufferedReader in) throws IOException{
        try {
            System.out.println("HTTP: receiving response...");
            String headers = in.readLine();
            String emptyLine = in.readLine();
            String body = in.readLine();
            
            Message bodyObject = this.deserializeMessage(body.getBytes());
            System.out.println("HTTP: response received: " + bodyObject.toString());
            return bodyObject;
        } catch (IOException e) {
            e.printStackTrace();
            try {
				this.socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
        }
        
        return null;
    }
    
    public void send(Message message) throws IOException{
        try {
            if(message.getError() == null || message.getError().equals("")){

                System.out.println("sendRequest: " + message.toString());
                this.sendRequest(message);
            }
            else{
                System.out.println("sendResponse: " + message.toString());
                this.sendResponse(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendRequest(Message message) throws IOException{
        Socket client = null;
        DataOutputStream out = null;
        try {
            client = new Socket("localhost", message.getPort());
            out = new DataOutputStream(client.getOutputStream());
            
            message.setPort(this.serverPort);

            String headerLine = "GET resource HTTP/1.0\r\n";
            String httpHeaders = "\r\n";
            String emptyLine="\r\n";
            
            byte[] msg = this.serializeMessage(message);
            String body = new String(msg);
            
            out.writeBytes(headerLine);
            out.writeBytes(httpHeaders);
            out.writeBytes(emptyLine);
            out.writeBytes(body);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
			try {
				if(out != null) out.close();
				if(client != null) client.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }

    public void sendResponse(Message message){
        Socket client = null;
        DataOutputStream out = null;

        try {
            if(message.getAction().equals("send back to JMeter") && this.serverPort == 9050){
				this.sendJMeter(message);
				return;
			}

            client = new Socket("localhost", message.getPort());
            out = new DataOutputStream(client.getOutputStream());
            String headerLine;
            if(message.getError().equals("OK")){
                headerLine = "HTTP/1.0 200 OK" + "\r\n";
            }
            else if(message.getError().equals("Error: invalid token")){
                headerLine = "HTTP/1.0 403 Forbidden" + "\r\n";
            }
            else{
                headerLine = "HTTP/1.0 404 Not Found" + "\r\n";
            }

            String httpHeaders = "\r\n";
            String emptyLine="\r\n";

            byte[] msg = this.serializeMessage(message);
            String body = new String(msg);

            out.writeBytes(headerLine);
            out.writeBytes(httpHeaders);
            out.writeBytes(emptyLine);
            out.writeBytes(body);
            
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
			try {
				if(out != null) out.close();
				if(client != null) client.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }



    public void sendJMeter(Message message){
		DataOutputStream out = null;
		try {

            String headerLine;
            if(message.getError().equals("OK")){
                headerLine = "HTTP/1.0 200 OK" + "\r\n";
            }
            else if(message.getError().equals("Error: invalid token")){
                headerLine = "HTTP/1.0 403 Forbidden" + "\r\n";
            }
            else{
                headerLine = "HTTP/1.0 404 Not Found" + "\r\n";
            }
            String httpHeaders = "\r\n";
            String emptyLine="\r\n";

            byte[] msg = this.serializeMessage(message);
            String body = new String(msg);


			message.setPort(this.serverPort);
			out = new DataOutputStream(this.jmeterSocket.getOutputStream());
            
			
            out.writeBytes(headerLine);
            out.writeBytes(httpHeaders);
            out.writeBytes(emptyLine);
            out.writeBytes(body);
			
       		out.flush();

			System.out.println("-----SENT TO JMETER--------");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
			
		}finally{
			try {
				if(out != null) out.close();
				if(this.jmeterSocket != null) this.jmeterSocket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}

    



	public void send(Message message, Integer port) throws IOException{
        if(message.getError() == null || message.getError().equals(""))
            this.sendRequest(message, port);
        else
            this.sendResponse(message, port); 
    }


    public void sendRequest(Message message, int port) throws IOException{
        Socket client = null;
        DataOutputStream out = null;
        try {
            client = new Socket("localhost", port);
            out = new DataOutputStream(client.getOutputStream());
            
            String headerLine = "GET resource HTTP/1.0\r\n";
            String httpHeaders = "\r\n";
            String emptyLine="\r\n";
            
            byte[] msg = this.serializeMessage(message);
            String body = new String(msg);
            
            out.writeBytes(headerLine);
            out.writeBytes(httpHeaders);
            out.writeBytes(emptyLine);
            out.writeBytes(body);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
			try {
				if(out != null) out.close();
				if(client != null) client.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }

    public void sendResponse(Message message, int port){
        Socket client = null;
        DataOutputStream out = null;

        try {
            //if(message.getAction().equals("send back to JMeter") && this.serverPort == 9050){
			//	this.sendJMeter(message);
			//	return;
			//}

            client = new Socket("localhost", port);
            out = new DataOutputStream(client.getOutputStream());
            String headerLine;
            if(message.getError().equals("OK")){
                headerLine = "HTTP/1.0 200 OK" + "\r\n";
            }
            else if(message.getError().equals("Error: invalid token")){
                headerLine = "HTTP/1.0 403 Forbidden" + "\r\n";
            }
            else{
                headerLine = "HTTP/1.0 404 Not Found" + "\r\n";
            }

            String httpHeaders = "\r\n";
            String emptyLine="\r\n";

            byte[] msg = this.serializeMessage(message);
            String body = new String(msg);

            out.writeBytes(headerLine);
            out.writeBytes(httpHeaders);
            out.writeBytes(emptyLine);
            out.writeBytes(body);
            
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
			try {
				if(out != null) out.close();
				if(client != null) client.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }







	public void send(DatabaseMessage databaseMessage) throws IOException{
        
        if(databaseMessage.getError() == null || databaseMessage.getError().equals(""))
            this.sendDBrequest(databaseMessage);
        else
            this.sendDBresponse(databaseMessage);
    }



    public void sendDBrequest(DatabaseMessage databaseMessage){
        Socket client = null;
        DataOutputStream out = null;
        try {
            System.out.println("sending to DB on port=" + databaseMessage.getPort());
            client = new Socket("localhost", databaseMessage.getPort());
            out = new DataOutputStream(client.getOutputStream());
            databaseMessage.setPort(this.serverPort);
            String headerLine = "GET resource HTTP/1.0\r\n";
            String httpHeaders = "\r\n";
            String emptyLine="\r\n";
            
            byte[] msg = this.serializeMessage(databaseMessage);
            String body = new String(msg);
            
            out.writeBytes(headerLine);
            out.writeBytes(httpHeaders);
            out.writeBytes(emptyLine);
            out.writeBytes(body);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
			try {
				if(out != null) out.close();
				if(client != null) client.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }

    public void sendDBresponse(DatabaseMessage databaseMessage){
        Socket client = null;
        DataOutputStream out = null;

        try {
            System.out.println("sendDBresponse:" + databaseMessage.toString());
            client = new Socket("localhost", databaseMessage.getPort());
            out = new DataOutputStream(client.getOutputStream());
            String headerLine;
            if(databaseMessage.getError().equals("OK")){
                headerLine = "HTTP/1.0 200 OK" + "\r\n";
            }
            else if(databaseMessage.getError().equals("Error: invalid token")){
                headerLine = "HTTP/1.0 403 Forbidden" + "\r\n";
            }
            else{
                headerLine = "HTTP/1.0 404 Not Found" + "\r\n";
            }

            String httpHeaders = "\r\n";
            String emptyLine="\r\n";

            byte[] msg = this.serializeMessage(databaseMessage);
            String body = new String(msg);

            out.writeBytes(headerLine);
            out.writeBytes(httpHeaders);
            out.writeBytes(emptyLine);
            out.writeBytes(body);
            
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
			try {
				if(out != null) out.close();
				if(client != null) client.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
    }




	public DatabaseMessage receiveDatabaseMessage() throws IOException{
        try {
            Socket nextClient = this.socket.accept();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(nextClient.getInputStream()));
            
            String headerLine = in.readLine();
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
            if(httpMethod.equals("HTTP/1.0")){
                return this.receiveDBResponse(in);
            }
            else if(httpMethod.equals("GET")){
                String resourceRequested = tokenizer.nextToken();
                if(resourceRequested.equals("/buy") ||
                resourceRequested.equals("/sell") ||
                resourceRequested.equals("/login") ||
                resourceRequested.equals("/login/create user")){
                    this.jmeterSocket = nextClient;
                }
                
                System.out.println("HTTP: receiving response");
                String headers = in.readLine();
                String emptyLine = in.readLine();
                String body = in.readLine();
                
                DatabaseMessage bodyObject = this.deserializeDatabaseMessage(body.getBytes());
                System.out.println("receiveFromDB: " + bodyObject.toString());
                
                System.out.println("HTTP Handler: recebi: " + bodyObject.toString());
                return bodyObject;
            }
            else{
                System.out.println("The HTTP method is not recognized");
                return null;
            }
            
            
        } catch (IOException e) {
            e.printStackTrace();
            try {
				this.socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
        }
        
        return null;
    }

	

    public DatabaseMessage receiveDBResponse(BufferedReader in){
        try {
            
            String headers = in.readLine();
            String emptyLine = in.readLine();
            String body = in.readLine();
            
            DatabaseMessage bodyObject = this.deserializeDatabaseMessage(body.getBytes());
            System.out.println("receiveDBResponse: " + bodyObject.toString());

            return bodyObject;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.socket.close();
            } catch (IOException e2) {
				e2.printStackTrace();
			}
        }
        
        return null;
    }






	public void register() throws IOException{ 
    }

    private DatabaseMessage deserializeDatabaseMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), DatabaseMessage.class);
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


	private byte[] serializeMessage(DatabaseMessage databaseMessage){

		Gson gson = new GsonBuilder()
		        .setLenient().serializeNulls()
		        .create();
		
		byte[] serializedMessage = new byte[1024];
		String stringMessage = gson.toJson(databaseMessage);
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

	public int getPort() throws IOException{
        return this.socket.getLocalPort();
    }
	public InetAddress getInetAddress(){
        return this.socket.getInetAddress();
    }
}
