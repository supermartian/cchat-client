package com.vt.chatroom;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

public class ChatDialog extends ListActivity implements WebSocketClientTokenListener
{
    private Button sendButton;
    private EditText sentenceTxt;
    private String user;
    private String roomName;
    private Connection conn;
    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    @Override
    public void onCreate(Bundle savedInstance)
    {
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
                // TODO Auto-generated method stub
                log("<" + user + ">: " + sentenceTxt.getText().toString() + "\n");
                sendMessage();
                sentenceTxt.setText("");
            }

        });

        //Get the parameters from the mainActivity
        Bundle bundle = getIntent().getExtras();
        user = bundle.getString("user");
        roomName = bundle.getString("room");
        conn = new Connection();
    }

    @Override
    public void onResume() {
        super.onResume();
        JWC.addListener(this);
    }

    @Override
    public void onPause() {
        JWC.removeListener(this);
        super.onPause();
    }

    public void log(String aString)
    {
        listItems.add(aString);
        adapter.notifyDataSetChanged();

    }
    @Override
    public void processClosed(WebSocketClientEvent arg0)
    {
        // TODO Auto-generated method stub
        log("\n---------colosed\n");

    }
    @Override
    public void processOpened(WebSocketClientEvent arg0)
    {
        // TODO Auto-generated method stub
        log("---Opened---!");
    }
    @Override
    public void processOpening(WebSocketClientEvent arg0)
    {
        // TODO Auto-generated method stub
        log("---closed---");

    }
    @Override
    public void processPacket(WebSocketClientEvent event, WebSocketPacket packet)
    {
        // TODO Auto-generated method stub
    }
    @Override
    public void processReconnecting(WebSocketClientEvent arg0)
    {
        // TODO Auto-generated method stub
        log("--reconnect--");
    }
    @Override
    public void processToken(WebSocketClientEvent event, Token token)
    {
        // TODO Auto-generated method stub

        MessageType type = getMessageType(token.getString("type"));
        switch(type)
        {
            case M1:
                try
                {
                    handleMessage(token);
                }
                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
        }
    }

    public void handleMessage(Token message) throws JSONException
    {
        JSONObject record = new JSONObject(message.getString("content"));
        String sUser = record.getString("user");
        String sContent = record.getString("content");
        if(!sUser.equals(user))
        {
            log("<" + sUser + ">: " + sContent + "\n");
        }
    }

    public MessageType getMessageType(String sType)
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
            message.put("content", content.toString());
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

    public void keyNegociate()
    {

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

}
