package network;

import java.net.InetAddress;

import model.Book;

public class DatabaseMessage extends AbstractMessage {
    
    public Book book;

    public DatabaseMessage(){
        
    }

    public DatabaseMessage(String action, String error, Book book, Integer port, InetAddress address) {
        super(action, error, port, address);
        this.book = book;
    }
    
    public DatabaseMessage(AbstractMessage message, Book book){
        super(message.getAction(), message.getError(), message.getPort(), message.getAddress());
        this.book = book;
    }

    public DatabaseMessage(String action, Book book, Integer port){
        super();
        this.setAction(action);
        this.setPort(port);
        this.book = book;
    }

    public Book getBook() {
        return book;
    }
    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "DatabaseMessage [book=" + book + "]";
    }
    
}
