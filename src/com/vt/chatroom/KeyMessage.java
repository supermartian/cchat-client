package com.vt.chatroom;

import org.json.JSONException;

public class KeyMessage extends CMessage{

	public KeyMessage(String type, int roundleft, String intrmdt) {
		super(CMessage.verison, type);
		try {
			message.put("roundleft", roundleft);
			message.put("keyintrmdt", intrmdt);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setType(String type) {		
		try {
			message.remove("type");
			message.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
