package com.entity;
import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;
public class Comment {
	private String library, isbn, submitterAccount, content;
	private Timestamp ts;
	public Comment(String library, String isbn, String submitterAccount, String content, Timestamp ts){
		this.library = library;
		this.isbn = isbn;
		this.submitterAccount = submitterAccount;
		this.content = content;
		this.ts = ts;
	}
	public Comment(JSONObject jsonObject){
		try{
			this.library = jsonObject.getString("library");
			this.isbn = jsonObject.getString("isbn");
			this.submitterAccount = jsonObject.getString("submitterAccount");
			this.content = jsonObject.getString("content");
			this.ts = Timestamp.valueOf(jsonObject.getString("ts"));
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	public String getLibrary(){
		return library;
	}
	public String getIsbn(){
		return isbn;
	}
	public String getSubmitterAccount(){
		return submitterAccount;
	}
	public String getContent(){
		return content;
	}
	public Timestamp getTs(){
		return ts;
	}
}
