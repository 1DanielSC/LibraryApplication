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

    public UserDatabase(String databasePort, String connectionType) throws IOException{
        this.database = new ArrayList<>();
        this.connect(databasePort, connectionType);
        System.out.println("User Database server succesfully started on port " + this.socket.getPort() + ".");
        while(true){
            Message packetReceived = this.socket.receive();

            Message replyMessage = this.processPacket(packetReceived);

            this.socket.send(replyMessage);
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
        switch (message.getAction().toUpperCase()) {
            case "CREATE USER":
                this.database.add(new User(message.getUsername(), message.getPassword()));
                message.setError("OK");
                return message;
            
            case "LOGIN":
                User user = this.findByName(message.getUsername());
                if(user != null){
                    if(this.isPresent(user))
                        message.setError("OK");
                    else
                        message.setError("Error: user not found");
                }
                else{
                    message.setError("Error: user not registered");
                }
                return message;

            default:
                System.out.println("Error: action not recognized");
                break;
        }
        return null;
    }


    public void registerIntoLoadBalancer(String databasePort){

    }
}
