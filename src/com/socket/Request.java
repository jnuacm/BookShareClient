package com.socket;

import org.json.JSONObject;

public abstract class Request {
	protected int type;
	public abstract void send();
	public static Request parse(JSONObject jsonObject){
		return null;
	}
}
