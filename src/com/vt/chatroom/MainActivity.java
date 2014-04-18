package com.vt.chatroom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

public class MainActivity extends Activity implements WSMsgListener {

	public String TAG = "ChatRoom";
	public ImageButton login;
	private EditText userTxt;
	private EditText roomTxt;
	private AlertDialog.Builder alertDialogBuilder;
	private Connection conn;
	private WSHandler handler; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_layout);
       
        JWC.init();
        handler = WSHandler.getHandlerInstance();
        handler.setListener(this);
        conn = new Connection();
        login = (ImageButton) findViewById(R.id.login);
        roomTxt = (EditText) findViewById(R.id.roomname);
        userTxt = (EditText) findViewById(R.id.nickName);
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Message");
        conn.execute();
        JWC.addListener(handler);
        
        login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
            	join();
            }
        });
    }
    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override public void onPause()
    {
        super.onPause();
    }

    public void join()
    {
        String user = userTxt.getText().toString();
        String roomName = roomTxt.getText().toString();
        
    	JoinMessage joinMsg = new JoinMessage(user, roomName);
    	joinMsg.send();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void startChat()
    {
        Intent chatDialog = new Intent(MainActivity.this, ChatDialog.class);
        Bundle bundle = new Bundle();
        String user = userTxt.getText().toString();
        String roomName = roomTxt.getText().toString();


        bundle.putString("user", user);
        bundle.putString("room", roomName);

        chatDialog.putExtras(bundle);

        startActivity(chatDialog);
    }

    private class Connection extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... arg0)
        {
            // TODO Auto-generated method stub
            try
            {
                JWC.open();
            }
            catch (WebSocketException ex)
            {
                ex.printStackTrace();
            }
            return null;
        }

    }

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onKeyXCHG(int round) {
		if (round == 0) {
			startChat();
		}
	}
	@Override
	public void onKick() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onError(int errcode) {
		if (errcode == 0) {
			
		}
	}

}
