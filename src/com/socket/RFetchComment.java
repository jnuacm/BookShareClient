package com.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class RFetchComment extends Request {
	private String library,isbn;
	public RFetchComment(String library,String isbn){
		type = 8;
		this.library = library;
		this.isbn = isbn;
	}
	@Override
	public void send() {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("library", library);
			jsonObject.put("isbn", isbn);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Communication.send(jsonObject);
	}
	
}
