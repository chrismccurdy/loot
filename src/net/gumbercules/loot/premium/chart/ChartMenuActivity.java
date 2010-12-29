package net.gumbercules.loot.premium.chart;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import net.gumbercules.loot.R;
import net.gumbercules.loot.account.Account;
import net.gumbercules.loot.premium.DateButton;
import net.gumbercules.loot.premium.MemoryStatus;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;

public class ChartMenuActivity extends TabActivity
{
	private static final int BEGIN_DATE				= 0;
	private static final int END_DATE				= 1;
	
	public static final int CHART_TYPE_BAR			= 0;
	public static final int CHART_TYPE_PIE			= 1;
	public static final int CHART_TYPE_LINE			= 2;
	
	public static final int SEARCH_FIELD_BOTH		= 0;
	public static final int SEARCH_FIELD_PARTIES	= 1;
	public static final int SEARCH_FIELD_TAGS		= 2;
	
	public static final int X_AXIS_DAILY			= 0;
	public static final int X_AXIS_WEEKLY			= 1;
	public static final int X_AXIS_BIWEEKLY			= 2;
	public static final int X_AXIS_MONTHLY			= 3;
	public static final int X_AXIS_YEARLY			= 4;
	
	public static final int Y_AXIS_TOTAL			= 0;
	public static final int Y_AXIS_NUMBER			= 1;
	public static final int Y_AXIS_BALANCE			= 2;
	
	public static final int GROUPING_NONE			= 0;
	public static final int GROUPING_ACCOUNTS		= 1;
	public static final int GROUPING_SEARCH			= 2;
	public static final int GROUPING_ALL			= 3;
	
	private static final int CHART_ID				= Menu.FIRST;
	private static final int LOAD_ID				= Menu.FIRST + 1;
	
	private Spinner mChartTypeSpinner;
	private Spinner mSearchFieldSpinner;
	private Spinner mXAxisSpinner;
	private Spinner mYAxisSpinner;
	private Spinner mGroupingSpinner;
	private EditText mSearchEdit;
	private EditText mBeginDateEdit;
	private EditText mEndDateEdit;
	private ListView mAccountList;
	
	private Date mBeginDate;
	private Date mEndDate;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setupTabs();
		
		mChartTypeSpinner = (Spinner)findViewById(R.id.chart_type_spinner);
		mSearchFieldSpinner = (Spinner)findViewById(R.id.search_fields_spinner);
		mXAxisSpinner = (Spinner)findViewById(R.id.xaxis_spinner);
		mYAxisSpinner = (Spinner)findViewById(R.id.yaxis_spinner);
		mGroupingSpinner = (Spinner)findViewById(R.id.grouping_spinner);
		mSearchEdit = (EditText)findViewById(R.id.query_edit);
		mBeginDateEdit = (EditText)findViewById(R.id.date_begin_edit);
		mEndDateEdit = (EditText)findViewById(R.id.date_end_edit);
		mAccountList = (ListView)findViewById(R.id.list);
		ImageButton beginDateButton = (ImageButton)findViewById(R.id.date_begin_button);
		ImageButton endDateButton = (ImageButton)findViewById(R.id.date_end_button);

		// fill the account list
		int resource = android.R.layout.simple_list_item_multiple_choice;
		String[] accountList = Account.getAccountNames();
		if (accountList == null || accountList.length <= 0)
		{
			mAccountList.setEnabled(false);
			accountList = new String[] { getResources().getString(R.string.no_accounts) };
			resource = android.R.layout.simple_list_item_1;
		}
		
		ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(this, resource, accountList);
		mAccountList.setAdapter(accountAdapter);
		mAccountList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		// pre-select any account if it was called from transaction activity or context menu
		Bundle extra = getIntent().getExtras();
		String name = (extra != null ? extra.getString(Account.KEY_NAME) : null);
		if (name != null)
			mAccountList.setItemChecked(accountAdapter.getPosition(name), true);

		mXAxisSpinner.setSelection(3);
		
		// set up the date choosers
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
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		mBeginDate = DateButton.parseDateEdit(mBeginDateEdit);
		mEndDate = DateButton.parseDateEdit(mEndDateEdit);
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, CHART_ID, 0, R.string.chart_it)
			.setShortcut('1', 'c')
			.setIcon(android.R.drawable.ic_menu_gallery);
		menu.add(0, LOAD_ID, 0, R.string.load_chart)
			.setShortcut('2', 'l')
			.setIcon(android.R.drawable.ic_menu_recent_history);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case CHART_ID:
				createChart();
		    	break;
		    	
			case LOAD_ID:
				loadChart();
				break;
		}
		
		return true;
	}
	
	private void createChart()
	{
		Intent i = new Intent(this, ChartActivity.class);
    	i.putExtra(ChartActivity.KEY_BEGIN_DATE, mBeginDate.getTime());
    	i.putExtra(ChartActivity.KEY_END_DATE, mEndDate.getTime());
    	
    	SparseBooleanArray positions = mAccountList.getCheckedItemPositions();
    	String accounts = "";
    	int id = -1;
    	Account[] accts = Account.getActiveAccounts();
    	for (int j = 0; j < mAccountList.getCount(); ++j)
    	{
    		if (positions.get(j))
    		{
    			for (Account acct : accts)
    			{
    				if (acct.name.equals((String) mAccountList.getAdapter().getItem(j)))
    				{
    					id = acct.id();
    					break;
    				}
    			}
    			
    			if (id != -1)
    			{
    				accounts += id + ",";
    				id = -1;
    			}
    		}
    	}
    	
    	i.putExtra(ChartActivity.KEY_ACCOUNTS, accounts);
    	i.putExtra(ChartActivity.KEY_CHART_TYPE, mChartTypeSpinner.getSelectedItemPosition());
    	i.putExtra(ChartActivity.KEY_XAXIS, mXAxisSpinner.getSelectedItemPosition());
    	i.putExtra(ChartActivity.KEY_YAXIS, mYAxisSpinner.getSelectedItemPosition());
    	i.putExtra(ChartActivity.KEY_GROUPING, mGroupingSpinner.getSelectedItemPosition());
    	i.putExtra(ChartActivity.KEY_QUERY_FIELDS, mSearchFieldSpinner.getSelectedItemPosition());
    	i.putExtra(ChartActivity.KEY_QUERY, mSearchEdit.getText().toString());
    	startActivity(i);
    	
	}
	
	private void loadChart()
	{
		if (!MemoryStatus.checkMemoryStatus(this, false))
		{
			return;
		}
		
		File f = new File(ChartHandler.CHART_LOCATION);
		ArrayList<String> filenames = new ArrayList<String>();
		
		if (f != null && f.exists())
		{
			for (File file : f.listFiles())
			{
				if (file.exists() && !file.isDirectory())
				{
					filenames.add(file.getName());
				}
			}
		}
		
		final Dialog d = new Dialog(this);
		ListView v = new ListView(this);
		d.setContentView(v);
		d.setTitle(R.string.load_chart);
		if (!filenames.isEmpty())
		{
			v.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filenames));
			v.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					d.dismiss();
					String filename = (String)((ListView)parent).getAdapter().getItem(position);
					Intent i = new Intent(view.getContext(), ChartActivity.class);
					i.putExtra(ChartActivity.KEY_FILENAME, filename);
					startActivity(i);
				}
			});
		}
		else
		{
			d.setTitle(R.string.no_charts_to_load);
		}
			
		d.show();
	}

	private void setupTabs()
    {
        TabHost tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.chart_menu, tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec("Accounts")
        		.setIndicator("", getResources().getDrawable(R.drawable.ic_menu_account_list))
                .setContent(R.id.accounts_tab));
        tabHost.addTab(tabHost.newTabSpec("Properties")
        		.setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_agenda))
        		.setContent(R.id.properties_tab));
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
		{
			edit = mBeginDateEdit;
		}
		else if (id == END_DATE)
		{
			edit = mEndDateEdit;
		}
		else
		{
			return;
		}

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
