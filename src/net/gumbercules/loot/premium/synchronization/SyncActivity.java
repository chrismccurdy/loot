package net.gumbercules.loot.premium.synchronization;

import java.util.ArrayList;

import net.gumbercules.loot.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class SyncActivity extends Activity
{
	private Intent mIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sync);

		final EditText PCCText = (EditText)findViewById(R.id.PCCText);
		PCCText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				setAllowedClient(s.toString().trim());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
		});
		
		loadAccountList();
		
		ImageButton button = (ImageButton)findViewById(R.id.sync_button);
		button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// TODO: start/stop the server
					SyncClientThread sct = new SyncClientThread(PCCText.getText().toString(), mClientHandler,
							getContentResolver());
					sct.setAccounts(getSelectedAccounts());
					sct.start();
				}
			});
		
		button = (ImageButton)findViewById(R.id.share_button);
		button.setOnClickListener(new View.OnClickListener()
			{	
				@Override
				public void onClick(View v)
				{
					sendConnectionCode();
				}
			});

		mIntent = new Intent(this, NetworkSyncService.class);
		startService(mIntent);
		bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void sendConnectionCode()
	{
		TextView con_code_view = (TextView)findViewById(R.id.CCText);
		final String link = "sync://net.gumbercules.loot/" + con_code_view.getText().toString();
		
		final Intent send_intent = new Intent(Intent.ACTION_SEND);
		send_intent.putExtra(Intent.EXTRA_TEXT, link);
		send_intent.setType("text/plain");
		startActivity(Intent.createChooser(send_intent, "Send connection code..."));
	}
	
	private void setTextCode(String code)
	{
		TextView CCText = (TextView)findViewById(R.id.CCText);
		if (CCText != null)
		{
			CCText.setText(code);
		}
	}
	
	private void setAllowedClient(String client)
	{
		Message msg = Message.obtain(null, NetworkSyncService.MSG_SET_CLIENT);
		try
		{
			mServiceMessenger.send(msg);
		}
		catch (RemoteException e) { }
	}
	
	private void loadAccountList()
	{
		Uri uri = Uri.parse("content://net.gumbercules.loot.accountprovider");
		String[] projection = new String[] { "name" };
		String selection = "purged = 0";
		String order = "priority asc";
		ArrayList<String> accountList = new ArrayList<String>();
		int resource = android.R.layout.simple_list_item_multiple_choice;
		ListView accountListView = (ListView)findViewById(R.id.account_list);
		
		Cursor cur = managedQuery(uri, projection, selection, null, order);
		if (cur == null || !cur.moveToFirst())
		{
			accountListView.setEnabled(false);
			accountList.add(getResources().getString(R.string.no_accounts));
			resource = android.R.layout.simple_list_item_1;
		}
		else
		{
			do
			{
				accountList.add(cur.getString(0));
			} while (cur.moveToNext());
		}
		
		ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(this, resource, accountList);
		accountListView.setAdapter(accountAdapter);
		accountListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
	
	private int getAccountIdByName(String name)
	{
		Cursor cur = managedQuery(Uri.parse("content://net.gumbercules.loot.accountprovider/"),
				new String[]{"id"}, "name = ?", new String[]{name}, null);
		if (cur == null || !cur.moveToFirst())
		{
            if (cur != null)
            {
                cur.close();
            }
			return -1;
		}

		int id = cur.getInt(0);
        cur.close();
		
		return id;
	}

	private String getSelectedAccounts()
	{
		// get a comma-separated list of account ids, and send them back to the client
		final ListView lv = (ListView)findViewById(R.id.account_list);
		SparseBooleanArray positions = lv.getCheckedItemPositions();
		String accounts = "";
		int id = -1;
		for (int j = 0; j < lv.getCount(); ++j)
		{
			if (positions.get(j))
			{
				id = getAccountIdByName((String) lv.getAdapter().getItem(j));
				if (id != -1)
				{
					accounts += id + ",";
				}
			}
		}
		
		return accounts;
	}

	public static final ArrayList<Integer> accountStringToList(String accountStr)
	{
		if (accountStr == null || accountStr.length() == 0)
		{
			return null;
		}
		
		ArrayList<Integer> accountList = new ArrayList<Integer>();
		
		String[] ids = accountStr.split(",");
		for (String id : ids)
		{
			try
			{
				accountList.add(Integer.parseInt(id));
			}
			catch (NumberFormatException e) { }
		}
		
		return accountList;
	}
	
	private SyncServiceConnection mConnection = new SyncServiceConnection();
	private Messenger mServiceCommunicator = new Messenger(new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case NetworkSyncService.MSG_STOP_SERVICE:
					unbindService(mConnection);
					stopService(mIntent);
					break;
					
				case NetworkSyncService.MSG_GET_CODE:
					setTextCode(msg.getData().getString(NetworkSyncService.MSG_CONNECTION_CODE));
					break;
					
				case NetworkSyncService.MSG_NEED_ACCOUNTS:
			    	
			    	//Message new_msg = Message.
			    	// TODO: reply to the client with this message

					break;
					
				default:
					super.handleMessage(msg);
			}
		}
	});
	private Messenger mServiceMessenger;
	
	private Handler mClientHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case NetworkSyncService.MSG_STOP_SERVICE:
					unbindService(mConnection);
					stopService(mIntent);
					// TODO notify the user that it's complete
					break;
					
				case NetworkSyncService.MSG_BAD_CHECKSUM:
					// TODO notify the user of the bad checksum, cancel operations
					break;
					
				default:
					super.handleMessage(msg);
			}
		}
	};
	
	private class SyncServiceConnection implements ServiceConnection
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			mServiceMessenger = new Messenger(service);

			Message msg = Message.obtain(null, NetworkSyncService.MSG_REGISTER);
			msg.replyTo = mServiceCommunicator;
			try
			{
				mServiceMessenger.send(msg);
			}
			catch (RemoteException e) { }
			
			msg = Message.obtain(null, NetworkSyncService.MSG_GET_CODE);
			try
			{
				mServiceMessenger.send(msg);
			}
			catch (RemoteException e) { }
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			mServiceMessenger = null;
		}
	}
}
