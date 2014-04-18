package com.vt.chatroom;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.kit.WebSocketException;
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
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
            JWC.send(message.toString());
        }
        catch (WebSocketException ex)
        {
            ex.printStackTrace();
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void leave() throws JSONException, WebSocketException
    {
        JSONObject leaveMessage = new JSONObject();
        leaveMessage.put("ver", 1);
        leaveMessage.put("type", "leave");
        JWC.send(leaveMessage.toString());
    }

	@Override
	public void onMessage(String message) {
		log(message);
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
	public void onKick() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onError(int errcode) {
		// TODO Auto-generated method stub
		
	}

}
