package network;

import java.net.InetAddress;

public class Message {
	public String action;
	public String error;

	public String accessToken;
	
	public Integer id;
	public String name;
	public String author;
	public Double price;
	
	public Integer port;
	public InetAddress address;
	
	public Message(String action, String accessToken, Integer id, String name, String author, Double price, Integer port, InetAddress address) {
		super();
		this.action = action;
		this.accessToken = accessToken;
		this.id = id;
		this.name = name;
		this.author = author;
		this.price = price;
		this.port = port;
		this.address = address;
	}
	
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public Integer getId() {
		return id;
	}
	public Integer getPort() {
		return port;
	}
	public String getError(){
		return error;
	}
	public void setError(String error){
		this.error = error;
	}


	public void setPort(Integer port) {
		this.port = port;
	}


	public InetAddress getAddress() {
		return address;
	}


	public void setAddress(InetAddress address) {
		this.address = address;
	}


	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	
	
}
