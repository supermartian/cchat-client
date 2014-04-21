package com.vt.chatroom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainActivity extends Activity implements WSMsgListener {

	public String TAG = "ChatRoom";
	public Button login;
	private EditText userTxt;
	private EditText roomTxt;
	private AlertDialog.Builder alertDialogBuilder;
	private WSHandler handler;
	private MainActivity thisActivity = this;
	private String serverAddr = "ws://106.186.28.188:8888";
	
    private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
            case 0:
            	startChat();
            	break;
            case 1:
            	login.setEnabled(false);
            	setProgressBarIndeterminateVisibility(true);
            	join();
            	break;
            case 2:
            	break;
            case 3:
            	popupError((Integer) message.obj);
            	break;
            }
        }
    };
    
    private void popupError(int code) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	// Add the buttons
    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	           }
    	       });
    	builder.setMessage(new Integer(code).toString());
    	builder.create().show();
    }
    
    private class textChangeListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (roomTxt.getText().toString().length() != 0 &&
					userTxt.getText().toString().length() != 0) {
				login.setEnabled(true);
			} else {
				login.setEnabled(false);
			}
		}
    	
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.my_layout);   
        
        login = (Button) findViewById(R.id.login);
        roomTxt = (EditText) findViewById(R.id.roomname);
        userTxt = (EditText) findViewById(R.id.nickName);
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Message");
        
        login.setEnabled(false);
        
        roomTxt.addTextChangedListener(new textChangeListener());
        userTxt.addTextChangedListener(new textChangeListener());
            
    	handler = WSHandler.getHandlerInstance();
    	handler.setListener(thisActivity);
    	
        login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
            	handler.open(serverAddr);
            }
        });
    }
    
    @Override
    public void onResume()
    {
        handler = WSHandler.getHandlerInstance();
        handler.setListener(this);
        if (roomTxt.getText().toString().length() != 0 &&
        		userTxt.getText().toString().length() != 0) {
        	login.setEnabled(true);
        }
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        setProgressBarIndeterminateVisibility(false);
    }

    public void join()
    {
        String user = userTxt.getText().toString();
        String roomName = roomTxt.getText().toString();
        
    	JoinMessage joinMsg = new JoinMessage(user, roomName);
    	joinMsg.send(handler);
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
		if (errcode != 0) {
			Message updatemsg = new Message();
			updatemsg.what = 3;
			updatemsg.obj = errcode;
			messageHandler.sendMessage(updatemsg);
		}
	}
	
	@Override
	public void onOpen() {
		messageHandler.sendEmptyMessage(1);
	}

}
