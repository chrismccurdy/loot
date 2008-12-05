package net.gumbercules.loot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PinActivity extends Activity
{
	private Button mUnlockButton;
	private EditText mPinEdit;
	private TextView mInvalidView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		final byte[] pin_hash = Database.getOptionBlob("pin");
		if (pin_hash == null || pin_hash.length == 1)
			startAccountChooser();

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
						startAccountChooser();
						return;
					}
				}

				mInvalidView.setText(R.string.invalid);
				mPinEdit.setText("");
			}
		});
	}
	
	private void startAccountChooser()
	{
        Intent intent = new Intent();
        intent.setClass(PinActivity.this, AccountChooser.class);
        startActivity(intent);
        finish();
	}
}
