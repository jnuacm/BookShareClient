package com.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Book {

	private String title,author,publisher,summary,cover,price,isbn;
	public Book(String isbn, String title, String author, String publisher, String summary, String cover, String price){
		this.title = title;
		this.author = author;
		this.publisher = publisher;
		this.summary = summary;
		this.cover = cover;
		this.price = price;
		this.isbn = isbn;
	}
	public Book(JSONObject jsonObject){
		try{
			this.title = jsonObject.getString("title");
			this.author = jsonObject.getString("author");
			this.publisher = jsonObject.getString("publisher");
			this.summary = jsonObject.getString("summary");
			this.cover = jsonObject.getString("cover");
			this.price = jsonObject.getString("price");
			this.isbn = jsonObject.getString("isbn");
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	public String getTitle(){
		return title;
	}
	public String getAuthor(){
		return author;
	}
	public String getPublisher(){
		return publisher;
	}
	public String getSummary(){
		return summary;
	}
	public String getCover(){
		return cover;
	}
	public String getPrice(){
		return price;
	}
	public String getISBN(){
		return isbn;
	}
}
