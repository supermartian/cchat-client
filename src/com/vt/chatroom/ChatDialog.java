package com.vt.chatroom;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.token.Token;

public class ChatDialog extends ListActivity implements WSMsgListener
{
    private ImageButton sendButton;
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
            case 1:
                quit();
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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        adapter=new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1,
            listItems);
        setListAdapter(adapter);

        sendButton = (ImageButton) findViewById(R.id.send);
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
    
    public void quit() {
    	super.onBackPressed();
    }
    
    @Override
    public void onBackPressed() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	// Add the buttons
    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	               new LeaveMessage().send(handler);
    	               handler.close();
    	           }
    	       });
    	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	               // User cancelled the dialog
    	           }
    	       });
    	builder.setMessage(R.string.quitconfirm);
    	builder.create().show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	int id = item.getItemId();
    	switch (id) {
    		case R.id.action_keys:
        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setTitle(R.string.aPrivateKey);
        		builder.setMessage(keyProc.getSecret().toString(16));
        		builder.create().show();
    			break;
    		default:
    			break;
    	}

    	return super.onOptionsItemSelected(item);
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
			setProgressBarIndeterminateVisibility(true);
			sendButton.setEnabled(false);
		} else {
			setProgressBarIndeterminateVisibility(false);
			sendButton.setEnabled(true);
		}
	}

	@Override
	public void onKick(String reason) {
		messageHandler.sendEmptyMessage(1);
	}

	@Override
	public void onError(int errcode) {
		
	}

	@Override
	public void onOpen() {
	}
}
