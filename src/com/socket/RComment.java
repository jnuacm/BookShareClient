package com.socket;

import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import com.entity.Comment;
public class RComment extends Request{

	private String library,isbn,submitterAccount,content;
	private Timestamp ts;
	public RComment(String library, String isbn, String submitterAccount, String content, Timestamp ts){
		type  = 6;
		this.library = library;
		this.isbn = isbn;
		this.submitterAccount = submitterAccount;
		this.content = content;
		this.ts = ts;
	}
	
	public void send(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("library", library);
			jsonObject.put("isbn", isbn);
			jsonObject.put("submitterAccount", submitterAccount);
			jsonObject.put("content", content);
			jsonObject.put("ts", ts.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Communication.send(jsonObject);
	}
}
