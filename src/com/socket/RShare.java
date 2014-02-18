package com.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class RShare extends Request{
	private String isbn,account,library;
	public RShare(String library,String account,String isbn){
		type = 7;
		this.library = library;
		this.account = account;
		this.isbn = isbn;
	}
	
	public void send(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("account", account);
			jsonObject.put("library",library);
			jsonObject.put("isbn", isbn);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Communication.send(jsonObject);
	}
}
