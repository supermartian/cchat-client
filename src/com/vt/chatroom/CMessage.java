package com.vt.chatroom;
import org.json.JSONException;
import org.json.JSONObject;

public class CMessage {
	public static int verison = 1;
	protected JSONObject message;
	
	public CMessage(int ver, String type) {
		message = new JSONObject();
		try {
			message.put("ver", ver);
			message.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public CMessage(JSONObject m) {
		message = m;	
	}
	
	public void send(WSHandler handler) {
		handler.send(message.toString());
	}
	
	public String toString() {
		return message.toString();
	}
}
