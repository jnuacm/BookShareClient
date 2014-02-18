package com.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Communication {
	private static final String host = "192.168.1.102";
	private static final int targetport = 9876,localport = 10000;
	
	public static void send(JSONObject jsonObject){
		try{
			DatagramSocket socket = new DatagramSocket();
			byte data[] = jsonObject.toString().getBytes("UTF-8");
			DatagramPacket sendPacket = new DatagramPacket(data,data.length,InetAddress.getByName(host),targetport);
			socket.send(sendPacket);
			Log.i("SEND",new String(data,0,data.length));
			socket.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static JSONObject receive(){
		byte data[] = new byte[20480];
		DatagramSocket socket = null;
		DatagramPacket receivePacket = new DatagramPacket(data,data.length);
		try{
			socket = new DatagramSocket(localport);
			Log.i("BEFORE REC",">>>>>>");
			socket.receive(receivePacket);
			socket.close();
			Log.i("REC",new String(receivePacket.getData(),0,receivePacket.getLength()));
			return new JSONObject(new String(receivePacket.getData(),0,receivePacket.getLength(),"UTF-8"));
		}catch(SocketTimeoutException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(JSONException e){
			e.printStackTrace();
		}
		try{
			socket.close();
			return new JSONObject("{\"status\":false}");
		}catch(JSONException e){
			return null;
		}
	}
}
