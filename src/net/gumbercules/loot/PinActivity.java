package net.gumbercules.loot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
