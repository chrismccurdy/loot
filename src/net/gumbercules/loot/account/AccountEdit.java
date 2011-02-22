/*
 * This file is part of the loot project for Android.
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. This program is distributed in the 
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. You should have received a copy of the GNU General 
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2008, 2009, 2010, 2011 Christopher McCurdy
 */

package net.gumbercules.loot.account;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;

import net.gumbercules.loot.R;
import net.gumbercules.loot.backend.CurrencyWatcher;
import net.gumbercules.loot.backend.Database;
import net.gumbercules.loot.backend.NoDecimalCurrencyWatcher;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AccountEdit extends Activity
{
	@SuppressWarnings("unused")
	private static final String TAG = "net.gumbercules.loot.AccountEdit";
	
	private EditText mNameEdit;
	private EditText mBalanceEdit;
	private EditText mPriorityEdit;
	private CheckBox mPrimaryCheckBox;
	private CheckBox mCreditCheckBox;
	private EditText mCreditLimitEdit;
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
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("key_input_no_decimal", false))
		{
			mCurrencyWatcher = new NoDecimalCurrencyWatcher();
		}
		else
		{
			mCurrencyWatcher = new CurrencyWatcher();
		}
		mBalanceEdit.addTextChangedListener(mCurrencyWatcher);
		mPriorityEdit = (EditText)findViewById(R.id.PriorityEdit);
		mPriorityEdit.setKeyListener(new DigitsKeyListener());
		mPrimaryCheckBox = (CheckBox)findViewById(R.id.PrimaryCheckBox);
		mCreditCheckBox = (CheckBox)findViewById(R.id.CreditCheckBox);
		mCreditLimitEdit = (EditText)findViewById(R.id.CreditLimitEdit);
		mCreditLimitEdit.addTextChangedListener(mCurrencyWatcher);
		mDisplaySpinner = (Spinner)findViewById(R.id.DisplaySpinner);
		ImageButton SaveButton = (ImageButton)findViewById(R.id.SaveButton);
		ImageButton CancelButton = (ImageButton)findViewById(R.id.CancelButton);
		
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
		
		final TableRow creditLimitRow = (TableRow)findViewById(R.id.CreditLimitRow);
		mCreditCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				creditLimitRow.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			}
		});
		
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
			{
				mNameEdit.setText(acct.name);
			}
			if (mBalanceEdit != null)
			{
				setupCurrencyEdit(mBalanceEdit, acct.initialBalance);
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
			if (mCreditLimitEdit != null)
			{
				setupCurrencyEdit(mCreditLimitEdit, acct.creditLimit);
			}
		}
	}
	
	private void setupCurrencyEdit(EditText edit, BigDecimal value)
	{
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		String new_currency = Database.getOptionString("override_locale");
		if (new_currency != null && !new_currency.equals(""))
		{
			nf.setCurrency(Currency.getInstance(new_currency));
		}
		String num = nf.format(value);
		StringBuilder sb = new StringBuilder();
		sb.append(mCurrencyWatcher.getAcceptedChars());
		String accepted = "[^\\Q" + sb.toString() + "\\E]";
		num = num.replaceAll(accepted, "");
		
		edit.setText(num);
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
		String creditLimitText = mCreditLimitEdit.getText().toString();
		
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
			acct.initialBalance = new BigDecimal(balText);
		}
		catch (NumberFormatException e)
		{
			// if there is no data (or bad data) in the field, set it to zero
			acct.initialBalance = new BigDecimal(0.0);
		}
		
		try
		{
			acct.priority = new Integer(priText);
		}
		catch (NumberFormatException e)
		{
			acct.priority = 1;
		}
		
		try
		{
			acct.creditLimit = new BigDecimal(creditLimitText);
		}
		catch (NumberFormatException e)
		{
			acct.creditLimit = new BigDecimal(0.0);
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
