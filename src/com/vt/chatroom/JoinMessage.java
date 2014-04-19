package com.vt.chatroom;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

public class JoinMessage extends CMessage{
	
	private static String byteArrayToHexString(byte[] b) {
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result +=
		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
	}
	
	public JoinMessage(String name, String room) {
		super(CMessage.verison, "join");
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			message.put("chatroom", byteArrayToHexString(md.digest(room.getBytes())));
			message.put("clientid", byteArrayToHexString(md.digest(name.getBytes())));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
