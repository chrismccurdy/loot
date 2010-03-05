package net.gumbercules.loot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.gumbercules.loot.account.Account;
import net.gumbercules.loot.account.AccountChooser;
import net.gumbercules.loot.backend.Database;
import net.gumbercules.loot.transaction.TransactionActivity;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PinActivity extends Activity
{
	public static final String SHOW_ACCOUNTS = "show";
	public static final String CHECKSUM = "checksum";
	
	private Button mUnlockButton;
	private EditText mPinEdit;
	private TextView mInvalidView;
	private Bundle mBundle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Intent i = getIntent();
		mBundle = null;

		if (i != null && i.hasCategory("net.gumbercules.category.LAUNCHER"))
		{
			mBundle = i.getExtras();
		}

		final byte[] pin_hash = Database.getOptionBlob("pin");
		if (pin_hash == null || pin_hash.length == 1)
		{
			startAccountChooser(true);
			finish();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pin);
		
		mUnlockButton = (Button)findViewById(R.id.unlockButton);
		mPinEdit = (EditText)findViewById(R.id.pinEdit);
		mInvalidView = (TextView)findViewById(R.id.invalidView);
		
		Button clearButton = (Button)findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				mPinEdit.setText("");
				mInvalidView.setText("");
			}
		});
		
		int[] buttonIds = {R.id.Button00, R.id.Button01, R.id.Button02, R.id.Button03,
				R.id.Button04, R.id.Button05, R.id.Button06, R.id.Button07, R.id.Button08, R.id.Button09};
		Button button;
		for (int resId : buttonIds)
		{
			button = (Button)findViewById(resId);
			if (button == null)
				continue;
			button.setOnClickListener(new Button.OnClickListener()
			{
				public void onClick(View v)
				{
					mPinEdit.append(v.getTag().toString());
					mInvalidView.setText("");
				}
			});
		}
		
		mPinEdit.setOnKeyListener(new EditText.OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				mInvalidView.setText("");
				return false;
			}	
		});
		
		mUnlockButton.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				String pin = mPinEdit.getText().toString();
				mPinEdit.setText("");

				byte[] bytes;
				try
				{
					MessageDigest md = MessageDigest.getInstance("SHA-1");
					md.update(pin.getBytes());
					bytes = md.digest();
				}
				catch (NoSuchAlgorithmException e)
				{
					return;
				}
				
				if (bytes.length == pin_hash.length)
				{
					int i = bytes.length - 1;
					for (; i >= 0; --i)
					{
						if (bytes[i] != pin_hash[i])
							break;
					}
					
					if (i <= 0)
					{
						startAccountChooser(true);
						return;
					}
				}

				mInvalidView.setText(R.string.invalid);
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
			return true;
		}
		
		return false;
	}

	private void startAccountChooser(boolean show)
	{
        if (mBundle != null)
        {
			if (mBundle.getInt("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID) !=
					AppWidgetManager.INVALID_APPWIDGET_ID)
			{
				int account = mBundle.getInt("account_id");
				
				if (account > 0)
				{
					Intent trans_intent = new Intent(this, TransactionActivity.class);
					trans_intent.putExtra(Account.KEY_ID, account);
					startActivity(trans_intent);
					return;
				}
			}
        }
        
        Intent intent = new Intent(this, AccountChooser.class);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       	prefs.edit().putBoolean(SHOW_ACCOUNTS, show).commit();
        startActivity(intent);
	}
}
