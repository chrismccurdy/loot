package net.gumbercules.loot;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;

public class RepeatActivity extends TabActivity
{
	private EditText editText;
	private Spinner spinner;
	
	private int mFinishIntent;
	private int mFinished;
	
	private int mIter;
	private int mFreq;
	private int mCustom;
	private Date mEndDate;
	
	private final int TAB_NEVER	= 0;
	private final int TAB_DAY	= 1;
	private final int TAB_WEEK	= 2;
	private final int TAB_MONTH	= 3;
	private final int TAB_YEAR	= 4;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mFinishIntent = RESULT_CANCELED;
        
        TabHost tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.repeat, tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec("Never")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel))
                .setContent(R.id.repeat_none));
        tabHost.addTab(tabHost.newTabSpec("Daily")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_day))
                .setContent(R.id.repeat_daily));
        tabHost.addTab(tabHost.newTabSpec("Weekly")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_week))
                .setContent(R.id.repeat_weekly));
        tabHost.addTab(tabHost.newTabSpec("Monthly")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_month))
                .setContent(R.id.repeat_monthly));
        tabHost.addTab(tabHost.newTabSpec("Yearly")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_today))
                .setContent(R.id.repeat_yearly));
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener()
        {
			public void onTabChanged(String tabId)
			{
				setup(tabId);
			}
        });
    }

    private void setup(String tabId)
	{
    	Button saveButton, cancelButton;

    	int save = 0,
    		cancel = 1,
    		freq = 2,
    		end = 3,
    		id = 4;
    	int[] resources = new int[5];

    	if (tabId.equals("Never"))
    	{
    		resources[save] = R.id.neverSaveButton;
    		resources[cancel] = R.id.neverCancelButton;
    		resources[freq] = 0;
    		resources[end] = 0;
    		resources[id] = TAB_NEVER;
    	}
    	else if (tabId.equals("Daily"))
    	{
    		resources[save] = R.id.daySaveButton;
    		resources[cancel] = R.id.dayCancelButton;
    		resources[freq] = R.id.dayEdit;
    		resources[end] = R.id.daySpinner;
    		resources[id] = TAB_DAY;
    	}
    	else if (tabId.equals("Weekly"))
    	{
    		resources[save] = R.id.weekSaveButton;
    		resources[cancel] = R.id.weekCancelButton;
    		resources[freq] = R.id.weekEdit;
    		resources[end] = R.id.weekSpinner;
    		resources[id] = TAB_WEEK;
    	}
    	else if (tabId.equals("Monthly"))
    	{
    		resources[save] = R.id.monthSaveButton;
    		resources[cancel] = R.id.monthCancelButton;
    		resources[freq] = R.id.monthEdit;
    		resources[end] = R.id.monthSpinner;
    		resources[id] = TAB_MONTH;
    	}
    	else if (tabId.equals("Yearly"))
    	{
    		resources[save] = R.id.yearSaveButton;
    		resources[cancel] = R.id.yearCancelButton;
    		resources[freq] = R.id.yearEdit;
    		resources[end] = R.id.yearSpinner;
    		resources[id] = TAB_YEAR;
    	}

    	saveButton = (Button)findViewById(resources[save]);
    	if (saveButton != null)
    	{
    		final int tab_id = resources[id];
    		saveButton.setOnClickListener(new Button.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				mFinishIntent = RESULT_OK;
    				saveState(tab_id);
    			}
    		});
    	}

    	cancelButton = (Button)findViewById(resources[cancel]);
    	if (cancelButton != null)
    		cancelButton.setOnClickListener(new Button.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				setResult(mFinishIntent);
    				finish();
    			}
    		});
    	
    	editText = (EditText)findViewById(resources[freq]);
    	if (editText != null)
    		editText.setKeyListener(new DigitsKeyListener());

    	spinner = (Spinner)findViewById(resources[end]);
    	if (spinner != null)
    	{
    		ArrayList<String> endDate = new ArrayList<String>();
    		endDate.add("No End Date");
    		
    		if (mEndDate != null)
    		{
    			Calendar cal = Calendar.getInstance();
   				cal.setTime(mEndDate);
    			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    			endDate.add(df.format(cal.getTime()));
    		}
    		else
    			endDate.add("Choose...");
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
    				android.R.layout.simple_spinner_item, endDate);
    		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
    		spinner.setAdapter(adapter);
    		
    		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
    		{
				public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id)
				{
					if (pos == 1)
						showDialog(0);
				}

				public void onNothingSelected(AdapterView<?> adapter) { }
    		});
    	}
	}

	private void setEndSpinner(Date date)
	{
		Calendar cal = Calendar.getInstance();
		if (date != null)
			cal.setTime(date);
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		// TODO: set the second item to the given date
		//dateEdit.setText(df.format(cal.getTime()));
	}
	
	private Date parseEndSpinner()
	{
		Date date;
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		
		try
		{
			// TODO: probably not valid
			date = df.parse(spinner.getSelectedItem().toString());
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
	
	private int[] endSpinnerToYMD()
	{
		int[] ymd = new int[3];
		Date date = parseEndSpinner();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		ymd[0] = cal.get(Calendar.YEAR);
		ymd[1] = cal.get(Calendar.MONTH);
		ymd[2] = cal.get(Calendar.DAY_OF_MONTH);
		
		return ymd;
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
            	setEndSpinner(cal.getTime());
            }
        };

    @Override
	protected Dialog onCreateDialog(int id)
	{
		int[] ymd = endSpinnerToYMD();
		return new DatePickerDialog(this, mDateSetListener, ymd[0], ymd[1], ymd[2]);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		int[] ymd = endSpinnerToYMD();
		((DatePickerDialog)dialog).updateDate(ymd[0], ymd[1], ymd[2]);
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	private void saveState(int tabId)
	{
		
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
	}
}
