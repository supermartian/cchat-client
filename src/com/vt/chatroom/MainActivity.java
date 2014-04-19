package com.vt.chatroom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainActivity extends Activity implements WSMsgListener {

	public String TAG = "ChatRoom";
	public ImageButton login;
	private EditText userTxt;
	private EditText roomTxt;
	private AlertDialog.Builder alertDialogBuilder;
	private WSHandler handler;
	private MainActivity thisActivity = this;
	
    private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
            case 0:
            	startChat();
            	break;
            case 1:
            	join();
            	break;
            case 2:
            	break;
            }
        }
    };
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_layout);   
        
        login = (ImageButton) findViewById(R.id.login);
        roomTxt = (EditText) findViewById(R.id.roomname);
        userTxt = (EditText) findViewById(R.id.nickName);
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Message");
            
    	handler = WSHandler.getHandlerInstance();
    	handler.setListener(thisActivity);
    	
        login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
            	handler.open("ws://192.168.1.124:8888");
            }
        });
    }
    @Override
    public void onResume()
    {
        handler = WSHandler.getHandlerInstance();
        handler.setListener(this);
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
    	joinMsg.send(handler);
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

	@Override
	public void onMessage(String message) {
		Log.d("PPPPPPPPP", message);
		
	}
	@Override
	public void onKeyXCHG(int round) {
		Message updatemsg = new Message();
		updatemsg.what = 2;
		updatemsg.obj = round;
		messageHandler.sendMessage(updatemsg);
		
		if (round == 0) {
			messageHandler.sendEmptyMessage(0);
		}
	}
	@Override
	public void onKick(String reason) {
		
	}
	
	@Override
	public void onError(int errcode) {
		if (errcode == 0) {
			
		}
	}
	
	@Override
	public void onOpen() {
		messageHandler.sendEmptyMessage(1);
	}

}
