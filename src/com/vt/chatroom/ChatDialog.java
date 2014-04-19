package com.vt.chatroom;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.token.Token;

public class ChatDialog extends ListActivity implements WSMsgListener
{
    private Button sendButton;
    private EditText sentenceTxt;
    private String user;
    private String roomName;
    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private WSHandler handler;
    private KeyProcessor keyProc;
    
    @SuppressLint("HandlerLeak")
	private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
            case 0:
            	log((String) message.obj);
            	break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstance)
    {
        handler = WSHandler.getHandlerInstance();
        handler.setListener(this);
        keyProc = KeyProcessor.getInstance();
        
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        adapter=new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1,
            listItems);
        setListAdapter(adapter);

        sendButton = (Button) findViewById(R.id.send);
        sentenceTxt = (EditText) findViewById(R.id.sentence);

        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendMessage();
                sentenceTxt.setText("");
            }
        });

        //Get the parameters from the mainActivity
        Bundle bundle = getIntent().getExtras();
        user = bundle.getString("user");
        roomName = bundle.getString("room");
    }

    @Override
    public void onResume() {
        handler.setListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onBackPressed() {
        new LeaveMessage().send(handler);
        handler.close();
        super.onBackPressed();
    }
    
    public void log(String aString)
    {
        listItems.add(aString);
        adapter.notifyDataSetChanged();
    }

    public void handleMessage(Token message) throws JSONException
    {
        JSONObject record = new JSONObject(message.getString("content"));
        String sUser = record.getString("user");
        String sContent = record.getString("content");
   
        log("<" + sUser + ">: " + sContent + "\n");
    }

    public void sendMessage()
    {
        String sentence = sentenceTxt.getText().toString();
        JSONObject message = new JSONObject();
        try {
            message.put("ver", 1);
            message.put("type", "message_0");
            JSONObject content = new JSONObject();
            content.put("user", user);
            content.put("content", sentence);
            content.put("room", roomName);
            String c = KeyProcessor.encryptMessage(content.toString(), keyProc.getSecret().toString(16));
            message.put("content", c);
            handler.send(message.toString());
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

	@Override
	public void onMessage(String message) {
		Message msg = new Message();
		msg.what = 0;
		msg.obj = message;
		messageHandler.sendMessage(msg);
	}

	@Override
	public void onKeyXCHG(int round) {
		if (round != 0) {
			sendButton.setEnabled(false);
		} else {
			sendButton.setEnabled(true);
		}
	}

	@Override
	public void onKick(String reason) {
		
	}

	@Override
	public void onError(int errcode) {
		
	}

	@Override
	public void onOpen() {
	}

}
