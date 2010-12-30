package net.gumbercules.loot.premium.export;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import net.gumbercules.loot.R;
import net.gumbercules.loot.account.Account;
import net.gumbercules.loot.backend.MemoryStatus;
import net.gumbercules.loot.premium.DateButton;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class ExportActivity extends Activity
{
	private static final int BEGIN_DATE	= 0;
	private static final int END_DATE	= 1;
	
	public static final int FORMAT_QUICKEN	= 0;
	public static final int FORMAT_CSV		= 1;
	
	private Spinner mAccountSpinner;
	private Spinner mFormatSpinner;
	private EditText mBeginDateEdit;
	private EditText mEndDateEdit;
	
	private Date mBeginDate;
	private Date mEndDate;
	
	private HashMap<String, Integer> mAccountList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.export);
		
		mAccountSpinner = (Spinner)findViewById(R.id.account_spinner);
		mFormatSpinner = (Spinner)findViewById(R.id.format_spinner);
		mBeginDateEdit = (EditText)findViewById(R.id.begin_date_edit);
		mEndDateEdit = (EditText)findViewById(R.id.end_date_edit);
		ImageButton exportButton = (ImageButton)findViewById(R.id.export_button);
		ImageButton beginDateButton = (ImageButton)findViewById(R.id.begin_date_button);
		ImageButton endDateButton = (ImageButton)findViewById(R.id.end_date_button);

		mAccountList = new HashMap<String, Integer>();
		Account[] accts = Account.getActiveAccounts();
		String[] tmp = new String[accts.length];
		String name = null;
		
		if (accts == null || accts.length <= 0)
		{
			mAccountList.put(getResources().getString(R.string.no_accounts), -1);
			mAccountSpinner.setEnabled(false);
			exportButton.setEnabled(false);
		}
		else
		{
			int len = accts.length;
			for (int i = 0; i < len; ++i)
			{
				name = accts[i].name;
				mAccountList.put(name, accts[i].id());
				tmp[i] = name;
			}
		}
		
		ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, tmp);
		accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAccountSpinner.setAdapter(accountAdapter);
		
		Bundle extra = getIntent().getExtras();
		name = (extra != null ? extra.getString(Account.KEY_NAME) : null);
		if (name != null)
		{
			mAccountSpinner.setSelection(accountAdapter.getPosition(name));
		}
		
		mEndDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(mEndDate);
		cal.add(Calendar.MONTH, -1);
		mBeginDate = cal.getTime();
		
		DateButton.setDateEdit(mBeginDate, mBeginDate, mBeginDateEdit);
		DateButton.setDateEdit(mEndDate, mEndDate, mEndDateEdit);
				
		mBeginDateEdit.setEnabled(false);
		mEndDateEdit.setEnabled(false);
		
		beginDateButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				showDialog(BEGIN_DATE);
			}
		});
		endDateButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				showDialog(END_DATE);
			}
		});
		
		final ExportActivity ea = this;
		exportButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String acct_name = (String)mAccountSpinner.getSelectedItem();
				int id = mAccountList.get(acct_name);
				int fmt = mFormatSpinner.getSelectedItemPosition();
				
				if (id == -1 || fmt == Spinner.INVALID_POSITION)
					return;
				
				ProgressDialog pd = new ProgressDialog(v.getContext());
				pd.setCancelable(true);
				pd.setMax(100);
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

				ExportThread et = new ExportThread(ea, pd);
				et.setBegin(mBeginDate);
				et.setEnd(mEndDate);
				et.setAcctId(id);
				et.setFormat(fmt);
				
				pd.show();
				et.start();
			}
		});
		
		if (!MemoryStatus.checkMemoryStatus(this, true))
		{
			exportButton.setEnabled(false);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		EditText edit = null;
		DatePickerDialog.OnDateSetListener listener = null;
		
		if (id == BEGIN_DATE)
		{
			edit = mBeginDateEdit;
			listener = mBeginDateSetListener;
		}
		else if (id == END_DATE)
		{
			edit = mEndDateEdit;
			listener = mEndDateSetListener;
		}
		else
			return null;
		
		int[] ymd = DateButton.dateEditToYMD(edit);
		return new DatePickerDialog(this, listener, ymd[0], ymd[1], ymd[2]);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		EditText edit = null;
		
		if (id == BEGIN_DATE)
			edit = mBeginDateEdit;
		else if (id == END_DATE)
			edit = mEndDateEdit;
		else
			return;

		int[] ymd = DateButton.dateEditToYMD(edit);
		((DatePickerDialog)dialog).updateDate(ymd[0], ymd[1], ymd[2]);
	}
	
	private DatePickerDialog.OnDateSetListener mBeginDateSetListener =
        new DatePickerDialog.OnDateSetListener()
		{
            public void onDateSet(DatePicker view, int year, int month,  int day)
            {
            	DateButton.setDateEditListener(year, month, day, mBeginDate, mBeginDateEdit);
            }
        };

	private DatePickerDialog.OnDateSetListener mEndDateSetListener =
        new DatePickerDialog.OnDateSetListener()
		{
            public void onDateSet(DatePicker view, int year, int month,  int day)
            {
            	DateButton.setDateEditListener(year, month, day, mEndDate, mEndDateEdit);
            }
        };
}
