package com.vt.chatroom;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.rpc.Rpc;
import org.jwebsocket.client.plugins.rpc.RpcListener;
import org.jwebsocket.client.plugins.rpc.Rrpc;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

import android.os.AsyncTask;
import android.util.Log;

public class WSHandler implements WebSocketClientTokenListener  {
	private WSMsgListener msgListener;
	private KeyProcessor keyProc;
	private static WSHandler instance;
	private BaseTokenClient jwc;
	
    private class JWCTask extends AsyncTask<String, Void, String> {
    	   	
        @Override
        protected String doInBackground(String... opt) {
        	try {
        		if (opt[0].equals("open")) {
        			jwc.open(opt[1]);
        			jwc.notifyOpened(null);
        		} else if (opt[0].equals("send")) {
        			jwc.send(opt[1].getBytes());
        		} else if (opt[0].equals("close")) {
        			jwc.close();
        			msgListener.onKick("disconnected");
        		}
			} catch (WebSocketException e) {
				e.printStackTrace();
			}

        	return "";
        }
        
        @Override
        protected void onPostExecute(String result) {
       }
    }
	
	public static WSHandler getHandlerInstance() {
		if (instance == null) {
			instance = new WSHandler();
		}
		
		return instance;
	}
	
	private WSHandler() {
		jwc = new BaseTokenClient();
		jwc.addListener(this);
		jwc.addListener(new RpcListener());//add an rpc listener
        Rpc.setDefaultBaseTokenClient(jwc);//set it to the default btc
        Rrpc.setDefaultBaseTokenClient(jwc);//same here
	}
	
	public void open(String url) {
		new JWCTask().execute("open", url);
	}
	
	public void close() {
		new JWCTask().execute("close");
	}
	
	public void send(String msg) {
		Log.d("PPPPPPPPP", "out:"+msg);
		new JWCTask().execute("send", msg);
	}
	
	@Override
	public void processClosed(WebSocketClientEvent arg0) {
		msgListener.onKick("Server Disconnected");
	}

	@Override
	public void processOpened(WebSocketClientEvent arg0) {
		Log.d("PPPPPPPPP", "open=open=open=open=open=open=open=open=open=open=");
		msgListener.onOpen();
	}

	@Override
	public void processOpening(WebSocketClientEvent arg0) {
		Log.d("PPPPPPPPP", "ope!!!!!!!");
	}

	@Override
	public void processPacket(WebSocketClientEvent arg0, WebSocketPacket arg1) {
	}

	@Override
	public void processReconnecting(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processToken(WebSocketClientEvent arg0, Token token) {
        MessageType type = getMessageType(token.getString("type"));
        Log.d("PPPPPPPPP", "ope!!!!!!!" + token.toString());
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
            		Log.d("PPPPPPPPP", "ope!!!!!!!" + sContent);
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
    	send(ret.toString());
    	msgListener.onMessage("SYSTEM: Now in round "+roundsleft+" with key: "+secret);
    	msgListener.onKeyXCHG(roundsleft);
    	
    }
}
