package com.vt.chatroom;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.token.Token;

public class WSHandler implements WebSocketClientTokenListener  {
	private boolean isopen;
	private WSMsgListener msgListener;
	private KeyProcessor keyProc;
	private static WSHandler instance;
	
	public static WSHandler getHandlerInstance() {
		if (instance == null) {
			instance = new WSHandler();
		}
		
		return instance;
	}
	
	private WSHandler() {
		isopen = false;
	}
	
	@Override
	public void processClosed(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processOpened(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processOpening(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPacket(WebSocketClientEvent arg0, WebSocketPacket arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processReconnecting(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processToken(WebSocketClientEvent arg0, Token token) {
        MessageType type = getMessageType(token.getString("type"));
        switch(type)
        {
            case M1:
            	JSONObject record;
            	try {
            		String s = KeyProcessor.decryptMessage(token.getString("content"), keyProc.getSecret().toString(16));
            		record = new JSONObject(s);
            		String sUser = record.getString("user");
            		String sContent = record.getString("content");
            		msgListener.onMessage("<" + sUser + ">: " + sContent + "\n");
            	} catch (JSONException e) {
            		e.printStackTrace();
            	}
            	break;
            case KEY1:
            	handleKey(token);
            	break;
            case ERROR:
            	handleError(token);
            	break;
        }
	}
	
	public void setListener(WSMsgListener l)
	{
		this.msgListener = l;
	}
	
	private void handleError(Token token)
	{
		msgListener.onError(token.getInteger("errcode"));
	}
	
    private MessageType getMessageType(String sType)
    {
        for(MessageType mType: MessageType.values())
        {
            if(mType.type.equals(sType))
            {
                return mType;
            }
        }
        return MessageType.UNKNOW;
    }
    
    private void handleKey(Token message)
    {
    	String prime = message.getString("prime");
    	String intrmdt = message.getString("keyintrmdt");
    	int roundsleft = message.getInteger("roundleft");
    	
    	if (keyProc == null) {
        	SecureRandom sr = new SecureRandom();
        	byte[] output = new byte[16];
        	sr.nextBytes(output);
        	keyProc = KeyProcessor.getInstance();
        	keyProc.setPrime(prime);
        	keyProc.setSecret((new BigInteger(output)).toString(16));
    	} else {
    		keyProc.setPrime(prime);
    	}
    	
    	String secret = keyProc.computeSecret(intrmdt);
    	if (roundsleft == 0) {
    		keyProc.setSecret(secret);
    		intrmdt = "";
    	} else {
    		intrmdt = secret;
    	}
    	
    	KeyMessage ret = new KeyMessage("keyxchg_2", roundsleft, intrmdt);
    	msgListener.onMessage("SYSTEM: Now in round "+roundsleft+" with key: "+secret);
    	msgListener.onKeyXCHG(roundsleft);
    	ret.send();
    }
}
