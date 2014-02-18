package com.socket;

import org.json.JSONException;
import org.json.JSONObject;

import com.entity.Account;
public class RSignIn extends Request{

	private String account,password,library;
	public RSignIn(String library,String account,String password){
		type  = 1;
		this.library = library;
		this.account = account;
		this.password = password;
	}
	
	public Account getAccount(){
		return new Account(library,account,library,null);
	}
	
	public void send(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("account", account);
			jsonObject.put("library",library);
			jsonObject.put("password", password);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Communication.send(jsonObject);
	}
}
