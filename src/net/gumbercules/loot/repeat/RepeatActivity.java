package net.gumbercules.loot.repeat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import net.gumbercules.loot.R;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.ToggleButton;

public class RepeatActivity extends TabActivity
{
	private EditText mEditText;
	private Spinner mEndSpinner;
	private Spinner mBySpinner;
	private ToggleButton[] mToggleButtons;
	private String mOldDate;
	
	private int mFinishIntent;
	private boolean mFinished;
	
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
        mFinished = false;
        mOldDate = null;
        
		if (savedInstanceState != null)
		{
			mIter = savedInstanceState.getInt(RepeatSchedule.KEY_ITER);
			mFreq = savedInstanceState.getInt(RepeatSchedule.KEY_FREQ);
			mCustom = savedInstanceState.getInt(RepeatSchedule.KEY_CUSTOM);
			mEndDate = new Date(savedInstanceState.getLong(RepeatSchedule.KEY_DATE));
		}
		else
		{
			Bundle extras = getIntent().getExtras();
			mIter = extras.getInt(RepeatSchedule.KEY_ITER);
			mFreq = extras.getInt(RepeatSchedule.KEY_FREQ);
			mCustom = extras.getInt(RepeatSchedule.KEY_CUSTOM);
			mEndDate = new Date(extras.getLong(RepeatSchedule.KEY_DATE));
		}
    }
    
    private void populateFields()
    {
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

        // setting the current tab to the first tab won't send the onTabChanged event
        if (mIter == RepeatSchedule.NO_REPEAT)
        	setup("Never");
        else
        	tabHost.setCurrentTab(mIter);
    }

    private void setup(String tabId)
	{
    	Button saveButton, cancelButton;

    	int save = 0,
    		cancel = 1,
    		freq = 2,
    		end = 3,
    		id = 4;
    	int[] resources = getResources(tabId);

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
    	
    	mEditText = (EditText)findViewById(resources[freq]);
    	if (mEditText != null)
    	{
    		mEditText.setKeyListener(new DigitsKeyListener());
    		if (mFreq > 0)
    			mEditText.setText(Integer.toString(mFreq));
    	}

    	mEndSpinner = (Spinner)findViewById(resources[end]);
    	if (mEndSpinner != null)
    	{
    		ArrayList<String> endDate = new ArrayList<String>();
    		endDate.add("No End Date");
    		endDate.add("Choose...");
    		    		
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
    				android.R.layout.simple_spinner_item, endDate);
    		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
    		mEndSpinner.setAdapter(adapter);

    		if (mEndDate != null && mEndDate.getTime() > 0)
    		{
    			setEndSpinner(mEndDate);
    			mEndSpinner.setSelection(2);
    		}

    		mEndSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
    		{
				@SuppressWarnings("unchecked")
				public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id)
				{
					ArrayAdapter<String> aa = (ArrayAdapter<String>)adapter.getAdapter();
					if (pos == 0)
					{
						try
						{
							aa.remove(mOldDate);
						}
						catch (Exception e) { }
						
						mOldDate = null;
					}
					else if (pos == 1)
					{
						try
						{
							aa.remove(mOldDate);
						}
						catch (Exception e) { }
						showDialog(0);
					}
				}

				public void onNothingSelected(AdapterView<?> adapter) { }
    		});
    	}
    	
    	// if we're in the weekly repeat tab, set up the proper order of days
    	if (resources[id] == TAB_WEEK)
    	{
    		Calendar cal = Calendar.getInstance();
    		int[] toggleResources = {R.id.ToggleButton01, R.id.ToggleButton02, R.id.ToggleButton03,
    				R.id.ToggleButton04, R.id.ToggleButton05, R.id.ToggleButton06, R.id.ToggleButton07};

    		int first = cal.getFirstDayOfWeek() - 1;
    		String[] days = {"S", "M", "T", "W", "T", "F", "S"};
    		int[] tags = {RepeatSchedule.SUNDAY, RepeatSchedule.MONDAY, RepeatSchedule.TUESDAY,
    				RepeatSchedule.WEDNESDAY, RepeatSchedule.THURSDAY, RepeatSchedule.FRIDAY,
    				RepeatSchedule.SATURDAY};
    		
    		ToggleButton button;
    		mToggleButtons = new ToggleButton[7];
    		int i = first;
    		
    		int today = tags[cal.get(Calendar.DAY_OF_WEEK) - 1];
    		for (int res : toggleResources)
    		{
    			button = (ToggleButton)findViewById(res);
    			button.setTextOff(days[i]);
    			button.setTextOn(days[i]);
    			button.setText(days[i]);
    			button.setTag(tags[i]);
    			setDayButtonToggled(button, today, tags[i]);
    			mToggleButtons[i] = button;
    			i = (i + 1) % 7;
    		}
    	}
    	
    	if (resources[id] == TAB_MONTH)
    	{
    		mBySpinner = (Spinner)findViewById(R.id.repeatBySpinner);
    		ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                    this, R.array.repeat_month, android.R.layout.simple_spinner_item);
            repeatAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            mBySpinner.setAdapter(repeatAdapter);
            
            if (mIter == RepeatSchedule.MONTHLY)
            	mBySpinner.setSelection(mCustom);
    	}
	}
    
    private void setDayButtonToggled(ToggleButton button, int today, int tag)
    {
    	// if there is no custom setting for the weekly schedule,
    	// choose today as the default item selected
    	if (mIter == RepeatSchedule.WEEKLY && mCustom != 0)
    	{
    		if ((mCustom & tag) != 0)
    			button.setChecked(true);
    	}
    	else
    	{
    		if (today == tag)
    			button.setChecked(true);
    	}
    }
    
    private int[] getResources(String tabId)
    {
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
    	
    	return resources;
    }

	@SuppressWarnings("unchecked")
	private void setEndSpinner(Date date)
	{
		ArrayAdapter<String> adapter = (ArrayAdapter<String>)mEndSpinner.getAdapter();
		if (adapter.getCount() > 2)
			return;
		
		Calendar cal = Calendar.getInstance();
		if (date != null)
			cal.setTime(date);
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		
		String str = df.format(cal.getTime());
		mOldDate = str;
		adapter.add(str);
	}
	
	private Date parseEndSpinner()
	{
		Date date;
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		
		try
		{
			date = df.parse(mOldDate);
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
		}
		catch (Exception e)
		{
			// set the date to null if there's a parsing error
			// the choice was set to 'No End Date'
			date = null;
		}
		
		return date;
	}
	
	private int[] endSpinnerToYMD()
	{
		int[] ymd = new int[3];
		Date date = parseEndSpinner();
		Calendar cal = Calendar.getInstance();
		if (date != null)
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
            	mOldDate = (String)mEndSpinner.getItemAtPosition(2);
            	mEndSpinner.setSelection(2);
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
		super.onPause();
		saveState(getTabHost().getCurrentTab());
	}
	
	private void saveState(int tabId)
	{
		if (mFinishIntent == RESULT_CANCELED || mFinished)
			return;
		
		long end = parseFields(tabId);

		end(end);
	}
	
	private long parseFields(int tabId)
	{
		long end = 0;
		if (tabId == TAB_NEVER)
		{
			mIter = RepeatSchedule.NO_REPEAT;
			mFreq = -1;
			mCustom = -1;
			return end;
		}
		else if (tabId == TAB_DAY)
			mIter = RepeatSchedule.DAILY;
		else if (tabId == TAB_WEEK)
		{
			mIter = RepeatSchedule.WEEKLY;
			mCustom = 0;
			for (ToggleButton button : mToggleButtons)
			{
				if (button.isChecked())
				{
					int tag = (Integer)button.getTag();
					mCustom |= tag;
				}
			}

			if (mCustom == 0)
			{
				mIter = RepeatSchedule.NO_REPEAT;
				mFreq = -1;
				mCustom = -1;
				return end;
			}
		}
		else if (tabId == TAB_MONTH)
		{
			mIter = RepeatSchedule.MONTHLY;
			mCustom = mBySpinner.getSelectedItemPosition();
		}
		else if (tabId == TAB_YEAR)
			mIter = RepeatSchedule.YEARLY;
		
		try
		{
			mFreq = Integer.parseInt(mEditText.getText().toString());
		}
		catch (Exception e)
		{
			// bail if there was no valid number entered
			mIter = RepeatSchedule.NO_REPEAT;
			mFreq = -1;
			mCustom = -1;
			return end;
		}
		
		mEndDate = parseEndSpinner();
		if (mEndDate != null)
			end = mEndDate.getTime();
		return end;
	}
	
	private void end(long end_date)
	{
		Intent i = new Intent();
		i.putExtra(RepeatSchedule.KEY_ITER, mIter);
		i.putExtra(RepeatSchedule.KEY_FREQ, mFreq);
		i.putExtra(RepeatSchedule.KEY_CUSTOM, mCustom);
		i.putExtra(RepeatSchedule.KEY_DATE, end_date);
		setResult(mFinishIntent, i);
		mFinished = true;
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
		long end_date = parseFields(getTabHost().getCurrentTab());
		outState.putInt(RepeatSchedule.KEY_ITER, mIter);
		outState.putInt(RepeatSchedule.KEY_FREQ, mFreq);
		outState.putInt(RepeatSchedule.KEY_CUSTOM, mCustom);
		outState.putLong(RepeatSchedule.KEY_DATE, end_date);
	}
}
