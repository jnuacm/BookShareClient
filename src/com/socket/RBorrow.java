package com.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class RBorrow extends Request{

	private String lib,acc,isbn;
	public RBorrow(String isbn, String account, String library){
		type = 4;
		lib = library;
		acc = account;
		this.isbn = isbn;
	}
	
	public String getLib(){
		return lib;
	}
	
	public String getAcc(){
		return acc;
	}
	
	public String getIsbn(){
		return isbn;
	}
	
	public void send(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("library", lib);
			jsonObject.put("account", acc);
			jsonObject.put("isbn", isbn);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Communication.send(jsonObject);
	}
}
