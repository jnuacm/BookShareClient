package com.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class RQuery extends Request{

	private String author,title,publisher;
	public RQuery(String author, String title, String publisher){
		type = 3;
		this.author = author;
		this.title = title;
		this.publisher = publisher;
	}
	
	public void send(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("author", author);
			jsonObject.put("title", title);
			jsonObject.put("publisher", publisher);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Communication.send(jsonObject);
	}
}
