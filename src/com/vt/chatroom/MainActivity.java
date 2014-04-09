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

public class MainActivity extends Activity implements WebSocketClientTokenListener {

	public String TAG = "ChatRoom";
	public ImageButton login;
	private EditText userTxt;
	private EditText roomTxt;
	private AlertDialog.Builder alertDialogBuilder;
	private AlertDialog alertDialog;
	private Connection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_layout);

        JWC.init();
        conn = new Connection();
        login = (ImageButton) findViewById(R.id.login);
        roomTxt = (EditText) findViewById(R.id.roomname);
        userTxt = (EditText) findViewById(R.id.nickName);
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Message");
        alertDialog = alertDialogBuilder.create();
        conn.execute();
        login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                try
                {
                    join();
                }
                catch (NoSuchAlgorithmException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (WebSocketException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //startActivity(chatDialog);
            }
        });
    }
    @Override
    public void onResume()
    {
        super.onResume();
        JWC.addListener(this);
    }

    @Override public void onPause()
    {
        JWC.removeListener(this);
        super.onPause();
    }

    public String sha1(String input) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] result = md.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < result.length; i++)
        {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public void join() throws NoSuchAlgorithmException, WebSocketException
    {
        JSONObject joinMessage = new JSONObject();
        try
        {
            String user = userTxt.getText().toString();
            String roomName = roomTxt.getText().toString();

            String clientHash = sha1(user);
            String chatroomHash = sha1(roomName);
            joinMessage.put("ver", 1);
            joinMessage.put("type", "join");
            joinMessage.put("clientid", clientHash);
            joinMessage.put("chatroom", chatroomHash);

            JWC.send(joinMessage.toString());
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public void processClosed(WebSocketClientEvent arg0)
    {
        // TODO Auto-generated method stub

    }
    @Override
    public void processOpened(WebSocketClientEvent arg0)
    {
        // TODO Auto-generated method stub

    }
    @Override
    public void processOpening(WebSocketClientEvent arg0)
    {
        // TODO Auto-generated method stub

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

    }
    @Override
    public void processToken(WebSocketClientEvent event, Token response)
    {
        // TODO Auto-generated method stub
        String type = response.getString("type");

        if(type.equals("error"))
        {
            int errCode = response.getInteger("errcode");
            switch(errCode)
            {
                case 0:
                    //login success and start the chat activity
                    startChat();
                    break;
                default:
                    alertDialog.setMessage(response.toString());
                    alertDialog.show();
            }

        }
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

}
