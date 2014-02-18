package com.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Ackownledge {
	private boolean status;
	public boolean getStatus(){
		return status;
	}
	
	public Ackownledge(boolean status){
		this.status = status;
	}
	
	public static Ackownledge receive(){
		try {
			JSONObject jsonObject = Communication.receive();
			Ackownledge ack = new Ackownledge(jsonObject.getBoolean("status"));
			return ack;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
