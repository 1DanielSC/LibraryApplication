package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.Server;
import model.User;
import network.Message;
import network.NetworkAccess;
import network.TCPHandler;
import network.UDPHandler;

public class UserDatabase implements Server{
    private List<User> database;

    public NetworkAccess socket;

    public UserDatabase(String databasePort, String connectionType) {
        this.database = new ArrayList<>();
        this.connect(databasePort, connectionType);
        
        try { 
            System.out.println("User Database server succesfully started on port " + this.socket.getPort() + ".");
            while(true){
                Message packetReceived = this.socket.receive();
                System.out.println("UserDatabase: receiving: " + packetReceived.toString());


                Message replyMessage = this.processPacket(packetReceived);
                replyMessage.setPort(packetReceived.getPort());
				replyMessage.setAddress(packetReceived.getAddress());
    
                System.out.println("UserDatabase: sending back to Auth: " + replyMessage.toString());
                this.socket.send(replyMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(String databasePort, String connectionType){
        System.out.println("Starting UserDatabase server on port " + databasePort + "...");
        try {
            switch(connectionType.toLowerCase()){
                case "udp":
                    this.socket = new UDPHandler(Integer.parseInt(databasePort));
                    this.registerIntoLoadBalancer(databasePort);
                    break;

                case "tcp":
                    this.socket = new TCPHandler(Integer.parseInt(databasePort));
                    break; 
                case "http": break; //TODO

                default:
                    System.out.println("Unknown connection type. Aborting server...");
                    System.exit(1);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(User user){
        this.database.add(user);
        System.out.println(this.database.toString()); 
    }

    public void removeById(Integer id){
        this.database.remove(id);
    }

    public boolean isPresent(User user){
        return this.database.contains(user);
    }

    public User findByName(String username){
        for (User user : database) {
            if(user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    public Message processPacket(Message message){
        Message response = new Message();
        switch (message.getAction()) {
            case "create user":
                this.add(new User(message.getUsername(), message.getPassword()));
                response.setError("OK");
                return response;
            
            case "login":
                User user = this.findByName(message.getUsername());
                if(user != null){
                    if(user.getPassword().equals(message.getPassword()))
                        response.setError("OK");
                    else
                        response.setError("Error: invalid password");
                }
                else{
                    response.setError("Error: user not found");
                }
                return response;

            default:
                System.out.println("Error: action not recognized");
                break;
        }
        return null;
    }


    public void registerIntoLoadBalancer(String databasePort){

    }
    
    public static void main(String[] args) {
    	new UserDatabase(args[0], args[1]);
    }
}
