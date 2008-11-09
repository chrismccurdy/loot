package net.gumbercules.loot;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;

public class TransactionEdit extends Activity
{
	private Transaction mTrans;
	private int mTransId;
	private int mFinishIntent;
	private int mRequest;
	private int mType;
	private int mAccountId;
	private boolean mFinished;

	private RadioButton checkRadio;
	private RadioButton withdrawRadio;
	private RadioButton depositRadio;
	
	private EditText dateEdit;
	private ImageButton dateButton;
	private AutoCompleteTextView partyEdit;
	private EditText amountEdit;
	private EditText checkEdit;
	private EditText tagsEdit;
	
	private Spinner accountSpinner;
	private Spinner repeatSpinner;
	
	private RadioButton budgetRadio;
	private RadioButton actualRadio;
	
	private Button saveButton;
	private Button cancelButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trans);
		
		mFinishIntent = RESULT_CANCELED;
		mFinished = false;

		// get the type code so we know whether to show a transaction or a transfer window
		if (savedInstanceState != null)
		{
			mRequest = savedInstanceState.getInt(TransactionActivity.KEY_REQ);
			mType = savedInstanceState.getInt(TransactionActivity.KEY_TYPE);
			mTransId = savedInstanceState.getInt(Transaction.KEY_ID);
			mAccountId = savedInstanceState.getInt(Account.KEY_ID);
		}
		else
		{
			Bundle extras = getIntent().getExtras();
			mRequest = extras.getInt(TransactionActivity.KEY_REQ);
			mType = extras.getInt(TransactionActivity.KEY_TYPE);
			mTransId = extras.getInt(Transaction.KEY_ID);
			mAccountId = extras.getInt(Account.KEY_ID);
		}

		populateFields();
	}
	
	private void populateFields()
	{
		depositRadio = (RadioButton)findViewById(R.id.depositRadio);
		withdrawRadio = (RadioButton)findViewById(R.id.withdrawRadio);
		checkRadio = (RadioButton)findViewById(R.id.checkRadio);
		
		dateEdit = (EditText)findViewById(R.id.dateEdit);
		dateButton = (ImageButton)findViewById(R.id.datePickerButton);
		
		amountEdit = (EditText)findViewById(R.id.amountEdit);
		amountEdit.setKeyListener(new CurrencyKeyListener());
		tagsEdit = (EditText)findViewById(R.id.tagsEdit);
		
		// create the repeat spinner and populate the values
		// TODO: set a listener to show a dialog when "Custom..." is selected
		repeatSpinner = (Spinner)findViewById(R.id.repeatSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.repeat, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(adapter);

        actualRadio = (RadioButton)findViewById(R.id.ActualRadio);
		budgetRadio = (RadioButton)findViewById(R.id.BudgetRadio);
		
		saveButton = (Button)findViewById(R.id.saveButton);
		cancelButton = (Button)findViewById(R.id.cancelButton);
		
        dateButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                showDialog(0);
            }
        });

		// load the transaction if mTransId > 0
        Transaction trans = null;
		if (mTransId == 0)
		{
			mTrans = new Transaction();
			
			// set the date edittext to the current date by default
			setDateEdit(null);
		}
		else
		{
			mTrans = Transaction.getTransactionById(mTransId);
			trans = mTrans;
			
			if (trans == null)
			{
				Log.e(TransactionEdit.class.toString(), "trans is null in populateFields()");
				return;
			}

			// figure out if this is a normal transaction or a transfer
			if (mTrans.getTransferId() != -1)
				mType = TransactionActivity.TRANSFER;
			else
				mType = TransactionActivity.TRANSACTION;
			
			if (trans.type == Transaction.WITHDRAW)
			{
				withdrawRadio.setChecked(true);
			}
			else if (trans.type == Transaction.DEPOSIT)
			{
				depositRadio.setChecked(true);
			}
			
			if (trans.budget && !trans.isPosted())
			{
				budgetRadio.setChecked(true);
			}
			else
			{
				actualRadio.setChecked(true);
			}
			
			setDateEdit(trans.date);
			NumberFormat nf = NumberFormat.getCurrencyInstance();
			Currency cur = nf.getCurrency();
			amountEdit.setText(nf.format(trans.amount).replace(cur.getSymbol(), ""));
			tagsEdit.setText(trans.tagListToString());
			
			// TODO: set repeat spinner to correct value
		}
        
		if (mType == TransactionActivity.TRANSFER)
		{
	        ArrayAdapter<CharSequence> accountAdapter = showTransferFields();
	        if (mTransId != 0 && accountAdapter != null)
	        {
	        	Account acct = Account.getAccountById(mAccountId);
	        	int pos = accountAdapter.getPosition(acct.name);
	        	accountSpinner.setSelection(pos);
	        }
		}
		else
		{
			showTransactionFields();
			if (trans != null)
			{
				partyEdit.setText(trans.party);

				if (trans.type == Transaction.CHECK)
				{
					checkEdit.setText(new Integer(trans.check_num).toString());
					checkRadio.setChecked(true);
				}
			}
		}

		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				mFinishIntent = RESULT_OK;
				onPause();
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				setResult(mFinishIntent);
				finish();
			}
		});
	}

	private void setDateEdit(Date date)
	{
		Calendar cal = Calendar.getInstance();
		if (date != null)
			cal.setTime(date);
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		dateEdit.setText(df.format(cal.getTime()));
	}
	
	private Date parseDateEdit()
	{
		Date date;
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		
		try
		{
			date = df.parse(dateEdit.getText().toString());
		}
		catch (ParseException e)
		{
			// set the date to today if there's a parsing error
			date = new Date();
		}
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		
		return date;
	}
	
	private int[] dateEditToYMD()
	{
		int[] ymd = new int[3];
		Date date = parseDateEdit();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		ymd[0] = cal.get(Calendar.YEAR);
		ymd[1] = cal.get(Calendar.MONTH);
		ymd[2] = cal.get(Calendar.DAY_OF_MONTH);
		
		return ymd;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		int[] ymd = dateEditToYMD();
		return new DatePickerDialog(this, mDateSetListener, ymd[0], ymd[1], ymd[2]);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		int[] ymd = dateEditToYMD();
		((DatePickerDialog)dialog).updateDate(ymd[0], ymd[1], ymd[2]);
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener()
		{
            public void onDateSet(DatePicker view, int year, int month,  int day)
            {
            	Calendar cal = Calendar.getInstance();
            	cal.set(Calendar.YEAR, year);
            	cal.set(Calendar.MONTH, month);
            	cal.set(Calendar.DAY_OF_MONTH, day);
            	setDateEdit(cal.getTime());
            }
        };

	private void showTransactionFields()
	{
		// set the check radio to enable/disable and automatically populate the check entry field
		checkRadio.setOnCheckedChangeListener( new RadioButton.OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				checkEdit = (EditText)findViewById(R.id.checkEdit);
				if (isChecked)
				{
					if (checkEdit.getText().toString().equals(""))
					{
						// autopopulate the edit with the next check number
						Account acct = Account.getAccountById(mAccountId);
						int check_num = acct.getNextCheckNum();
						checkEdit.setText(new Integer(check_num).toString());
					}
				}
				checkEdit.setEnabled(isChecked);
			}
		});
		
		partyEdit = (AutoCompleteTextView)findViewById(R.id.partyEdit);
		checkEdit = (EditText)findViewById(R.id.checkEdit);
		
		// set the autocompletion values for partyEdit
		String[] parties = Transaction.getAllParties();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, parties);
		partyEdit.setAdapter(adapter);
	}
	
	private ArrayAdapter<CharSequence> showTransferFields()
	{
		// if we're showing a transfer window, hide the check button, check field, and party field
		checkRadio.setVisibility(RadioButton.GONE);
		
		TableRow row = (TableRow)findViewById(R.id.partyRow);
		row.setVisibility(TableRow.GONE);
		row = (TableRow)findViewById(R.id.checkRow);
		row.setVisibility(TableRow.GONE);
		row = (TableRow)findViewById(R.id.accountRow);
		row.setVisibility(TableRow.VISIBLE);
		
		accountSpinner = (Spinner)findViewById(R.id.accountSpinner);
		String[] names = Account.getAccountNames();
		
		// if there is only one account in the database, tell the user they can't transfer and cancel
		if (names.length == 1)
		{
			// TODO: display message box
			setResult(mFinishIntent);
			finish();
			return null;
		}
		
		String[] acctNames = new String[names.length - 1];
		
		Account acct = Account.getAccountById(mAccountId);
		int i = 0;
		for ( String name : names )
			if (!name.equalsIgnoreCase(acct.name))
				acctNames[i++] = name;
		
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_item, acctNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(adapter);
        
        return adapter;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		saveState();
	}

	private void saveState()
	{
		if (mFinishIntent == RESULT_CANCELED || mFinished)
			return;

		Transaction trans;
		Account acct2 = null;

		if (mTransId != 0)
			trans = mTrans;
		else
		{
			trans = new Transaction();
		}

		if (mType == TransactionActivity.TRANSACTION)
			trans.party = partyEdit.getText().toString();
		else
			acct2 = Account.getAccountByName((String)accountSpinner.getSelectedItem());
		
		trans.addTags(tagsEdit.getText().toString());
		
		// get the date of the transaction and set time values to 0
		trans.date = parseDateEdit();
		
		// get the amount of the transaction
		try
		{
			trans.amount = new Double(amountEdit.getText().toString());
		}
		catch (NumberFormatException e)
		{
			trans.amount = 0.0;
		}

		// get the type of transaction
		if (checkRadio.isChecked())
		{
			trans.type = Transaction.CHECK;

			try
			{
				trans.check_num = new Integer(checkEdit.getText().toString());
			}
			catch (NumberFormatException e)
			{
				trans.type = Transaction.WITHDRAW;
				trans.check_num = 0;
			}
		}
		else if (withdrawRadio.isChecked())
		{
			trans.type = Transaction.WITHDRAW;
		}
		else
		{
			trans.type = Transaction.DEPOSIT;
		}
		
		// get if it's a budget transaction
		trans.budget = budgetRadio.isChecked();
		
		// TODO: set repeat values

		int id = -1;
		if (mType == TransactionActivity.TRANSACTION)
			id = trans.write(mAccountId);
		else
		{
			trans.account = mAccountId;
			id = trans.transfer(acct2);
		}
		
		Log.e("TransactionEdit", "mType = " + mType);
		Log.e("TransactionEdit", "id = " + id);
		
		mFinished = true;
		if (id != -1)
		{
			mTransId = id;
			Intent i = new Intent();
			Bundle b = new Bundle();
			b.putInt(Transaction.KEY_ID, mTransId);
			b.putInt(TransactionActivity.KEY_REQ, mRequest);
			i.putExtras(b);
			setResult(mFinishIntent, i);
		}
		else
		{
			setResult(RESULT_CANCELED);
		}

		finish();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		populateFields();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(TransactionActivity.KEY_REQ, mRequest);
		outState.putInt(TransactionActivity.KEY_TYPE, mType);
		if (mAccountId > 0)
			outState.putInt(Account.KEY_ID, mAccountId);
		if (mTransId > 0)
			outState.putInt(Transaction.KEY_ID, mTransId);
	}
}
