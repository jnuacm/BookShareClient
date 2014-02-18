package com.socket;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.entity.Account;
public class RSignUp extends Request{

	private String library,account,password,name;
	public RSignUp(String library,String account,String password,String name){
		type = 2;
		this.library = library;
		this.account = account;
		this.password = password;
		this.name = name;
	}
	
	public Account getAccount(){
		return new Account(library,account,password,name);
	}
	
	public void send(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", type);
			jsonObject.put("account", account);
			jsonObject.put("library",library);
			jsonObject.put("password", password);
			jsonObject.put("name", name);
			Log.i("JSON done",jsonObject.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Communication.send(jsonObject);
	}
}
