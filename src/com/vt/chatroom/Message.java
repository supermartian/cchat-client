package com.vt.chatroom;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.kit.WebSocketException;

public class Message {
	public static int verison = 1;
	protected JSONObject message;
	
	public Message(int ver, String type) {
		message = new JSONObject();
		try {
			message.put("ver", ver);
			message.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Message(JSONObject m) {
		message = m;	
	}
	
	public void send() {
		try {
			JWC.send(message.toString());
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}
}
