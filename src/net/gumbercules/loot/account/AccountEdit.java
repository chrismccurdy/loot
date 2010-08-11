package net.gumbercules.loot.account;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;

import net.gumbercules.loot.R;
import net.gumbercules.loot.backend.CurrencyWatcher;
import net.gumbercules.loot.backend.Database;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class AccountEdit extends Activity
{
	@SuppressWarnings("unused")
	private static final String TAG = "net.gumbercules.loot.AccountEdit";
	
	private EditText mNameEdit;
	private EditText mBalanceEdit;
	private EditText mPriorityEdit;
	private CheckBox mPrimaryCheckBox;
	private CheckBox mCreditCheckBox;
	private Spinner mDisplaySpinner;
	private int mRowId;
	private int mFinishIntent;
	private CurrencyWatcher mCurrencyWatcher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_edit);
		
		// only assume the finish intent is OK if we explicitly set it
		mFinishIntent = RESULT_CANCELED;
		
		mNameEdit = (EditText)findViewById(R.id.NameEdit);
		mBalanceEdit = (EditText)findViewById(R.id.BalanceEdit);
		mCurrencyWatcher = new CurrencyWatcher();
		mBalanceEdit.addTextChangedListener(mCurrencyWatcher);
		mPriorityEdit = (EditText)findViewById(R.id.PriorityEdit);
		mPriorityEdit.setKeyListener(new DigitsKeyListener());
		mPrimaryCheckBox = (CheckBox)findViewById(R.id.PrimaryCheckBox);
		mCreditCheckBox = (CheckBox)findViewById(R.id.CreditCheckBox);
		mDisplaySpinner = (Spinner)findViewById(R.id.DisplaySpinner);
		Button SaveButton = (Button)findViewById(R.id.SaveButton);
		Button CancelButton = (Button)findViewById(R.id.CancelButton);
		
		mBalanceEdit.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (v instanceof EditText)
				{
					CurrencyWatcher.setInputType((EditText)v);
				}
			}
		});
		CurrencyWatcher.setInputType(mBalanceEdit);
		
		mRowId = savedInstanceState != null ? savedInstanceState.getInt(Account.KEY_ID) : 0;
		if (mRowId == 0)
		{
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getInt(Account.KEY_ID) : 0;
		}
		
		SaveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				mFinishIntent = RESULT_OK;
				setResult(mFinishIntent);
				finish();
			}
		});
		
		CancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				setResult(mFinishIntent);
				finish();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(Account.KEY_ID, mRowId);
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
		if (mRowId != 0)
		{
			Account acct = Account.getAccountById(mRowId);
			
			if (mNameEdit != null)
				mNameEdit.setText(acct.name);
			if (mBalanceEdit != null)
			{
				NumberFormat nf = NumberFormat.getCurrencyInstance();
				String new_currency = Database.getOptionString("override_locale");
				if (new_currency != null && !new_currency.equals(""))
					nf.setCurrency(Currency.getInstance(new_currency));
				String num = nf.format(acct.initialBalance);
				StringBuilder sb = new StringBuilder();
				sb.append(mCurrencyWatcher.getAcceptedChars());
				String accepted = "[^\\Q" + sb.toString() + "\\E]";
				num = num.replaceAll(accepted, "");
				
				mBalanceEdit.setText(num);
			}
			if (mPriorityEdit != null)
			{
				mPriorityEdit.setText(Integer.toString(acct.priority));
			}
			if (mPrimaryCheckBox != null)
			{
				mPrimaryCheckBox.setChecked(acct.isPrimary());
			}
			if (mCreditCheckBox != null)
			{
				mCreditCheckBox.setChecked(acct.credit);
			}
			if (mDisplaySpinner != null)
			{
				mDisplaySpinner.setSelection(acct.balanceDisplay);
			}
		}
	}
	
	private void saveState()
	{
		if (mFinishIntent == RESULT_CANCELED)
			return;
		
		Account acct;
		if (mRowId != 0)
			acct = Account.getAccountById(mRowId);
		else
			acct = new Account();
		
		acct.name = mNameEdit.getText().toString();
		String balText = mBalanceEdit.getText().toString();
		String priText = mPriorityEdit.getText().toString();
		boolean primary = mPrimaryCheckBox.isChecked();
		boolean credit = mCreditCheckBox.isChecked();
		int display = mDisplaySpinner.getSelectedItemPosition();
		
		if (acct.name == "" || balText == "")
		{
			setResult(RESULT_CANCELED);
			return;
		}
		
		try
		{
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			char sep = dfs.getMonetaryDecimalSeparator();
			
			if (sep != '.')
				balText = balText.replaceAll(String.valueOf(sep), ".");
			acct.initialBalance = new Double(balText);
		}
		catch (NumberFormatException e)
		{
			// if there is no data (or bad data) in the field, set it to zero
			acct.initialBalance = 0.0;
		}
		
		try
		{
			acct.priority = new Integer(priText);
		}
		catch (NumberFormatException e)
		{
			acct.priority = 1;
		}

		acct.credit = credit;
		acct.balanceDisplay = display;

		int id = acct.write();

		if (id != -1)
		{
			Intent broadcast = new Intent("net.gumbercules.loot.intent.ACCOUNT_UPDATED", null);
			broadcast.putExtra("account_id", acct.id());
			sendBroadcast(broadcast);

			acct.setPrimary(primary);
			mRowId = id;
		}
	}
}
