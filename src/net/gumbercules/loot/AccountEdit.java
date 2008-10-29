package net.gumbercules.loot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AccountEdit extends Activity
{
	private EditText NameEdit;
	private EditText BalanceEdit;
	private int rowId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_edit);
		
		NameEdit = (EditText)findViewById(R.id.NameEdit);
		BalanceEdit = (EditText)findViewById(R.id.BalanceEdit);
		Button SaveButton = (Button)findViewById(R.id.SaveButton);
		Button CancelButton = (Button)findViewById(R.id.CancelButton);
		
		rowId = savedInstanceState != null ? savedInstanceState.getInt(Account.KEY_ID) : 0;
		if (rowId == 0)
		{
			Bundle extras = getIntent().getExtras();
			rowId = extras != null ? extras.getInt(Account.KEY_ID) : 0;
		}
		
		SaveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Account acct;

				if (rowId != 0)
					acct = Account.getAccountById(rowId);
				else
					acct = new Account();

				acct.name = NameEdit.getText().toString();
				acct.initialBalance = new Double(BalanceEdit.getText().toString());
				
				setResult(RESULT_OK);
				finish();
			}
		});
		
		CancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(Account.KEY_ID, rowId);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		populateFields();
	}
	
	private void populateFields()
	{
		if (rowId != 0)
		{
			Account acct = Account.getAccountById(rowId);
			
			if (NameEdit != null)
				NameEdit.setText(acct.name);
			if (BalanceEdit != null)
				// TODO: fix to display it as a currency, without the currency symbol
				BalanceEdit.setText(Double.toString(acct.initialBalance));
		}
	}
	
	private void saveState()
	{
		Account acct;
		if (rowId != 0)
			acct = Account.getAccountById(rowId);
		else
			acct = new Account();
		
		acct.name = NameEdit.getText().toString();
		acct.initialBalance = new Double(BalanceEdit.getText().toString());
		
		int id = acct.write();
		if (id != -1)
			rowId = id;
	}
}
