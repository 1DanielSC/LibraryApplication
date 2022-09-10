package model;

import java.io.Serializable;

public class Book implements Serializable {
	
	public Integer id;
	public String name;
	public String author;
	public Double price;
	
	public Book(){
		this.name = "";
		this.author = "";
		this.price = 0.00;
	}

	public Book(String name, String author, Double price) {
		this.name = name;
		this.author = author;
		this.price = price;
	}

	public Book(Integer id, String name, String author, Double price){
		this.id = id;
		this.name = name;
		this.author = author;
		this.price = price;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Book [author=" + author + ", id=" + id + ", name=" + name + ", price=" + price + "]";
	}
}
