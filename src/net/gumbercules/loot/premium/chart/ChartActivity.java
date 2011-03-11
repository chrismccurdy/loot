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

package net.gumbercules.loot.premium.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import net.gumbercules.loot.R;
import net.gumbercules.loot.backend.BigMoney;
import net.gumbercules.loot.backend.MemoryStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ChartActivity extends Activity
{
	public static final String KEY_BEGIN_DATE	= "k_begin";
	public static final String KEY_END_DATE		= "k_end";
	public static final String KEY_ACCOUNTS		= "k_accounts";
	public static final String KEY_CHART_TYPE	= "k_chart";
	public static final String KEY_XAXIS		= "k_xaxis";
	public static final String KEY_YAXIS		= "k_yaxis";
	public static final String KEY_GROUPING		= "k_grouping";
	public static final String KEY_QUERY		= "k_query";
	public static final String KEY_QUERY_FIELDS	= "k_fields";
	public static final String KEY_FILENAME		= "k_filename";
	
	private static final int SAVE_ID			= Menu.FIRST;
	
	private static final String TAG	= "net.gumbercules.loot.premium.ChartActivity";
	
	private int[] mAccounts;
	private int mChartType;
	private int mGrouping;
	private int mXaxis;
	private int mYaxis;
	private long mBeginDate;
	private long mEndDate;
	private int mQueryFields;
	private String mFilename;
	private String[] mQuery;
	private ChartHandler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.chart);
		WebView wv = (WebView)findViewById(R.id.chart_view);
		
		Bundle extra = getIntent().getExtras();
		
		if (extra == null)
		{
			return;
		}

		getBundledData(extra);
		ProgressDialog pd = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		pd.setTitle(R.string.generating_chart);
		pd.setMessage(getResources().getString(R.string.generating_chart_body));
		pd.setIndeterminate(true);
		pd.show();

		mHandler = new ChartHandler(this, wv, pd, mChartType, mXaxis, mYaxis, mGrouping);
		
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(mHandler, "chart_handler");
		wv.loadUrl("file:///android_asset/flot/html/graph.html");
		
		FrameLayout cv = (FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content);
		final View zoom = wv.getZoomControls();
		cv.addView(zoom, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
		zoom.setVisibility(View.GONE);

		if (mFilename != null)
		{
			try
			{
				mHandler.loadChart(mFilename);
			}
			catch (Exception e)
			{
				Toast.makeText(this, R.string.load_error, Toast.LENGTH_LONG).show();
				Log.e(TAG + ".onCreate()", "An error occurred while loading the graph");
				Log.e(TAG + ".onCreate()", Log.getStackTraceString(e));
			}
		}
		else
		{
			final ArrayList<Long> dates = getDateRanges(mBeginDate, mEndDate, mXaxis);
			Thread t = new Thread()
			{
				public void run()
				{
					getData(dates, buildUri(mQueryFields, mYaxis));
				}
			};
			t.start();
		}
	}
	
	private void getBundledData(Bundle extra)
	{
		mBeginDate = extra.getLong(KEY_BEGIN_DATE);
		mEndDate = extra.getLong(KEY_END_DATE);
		mChartType = extra.getInt(KEY_CHART_TYPE);
		mXaxis = extra.getInt(KEY_XAXIS);
		mYaxis = extra.getInt(KEY_YAXIS);
		mGrouping = extra.getInt(KEY_GROUPING);
		mQueryFields = extra.getInt(KEY_QUERY_FIELDS);
		mFilename = extra.getString(KEY_FILENAME);
		
		if (mFilename != null)
		{
			return;
		}

		String[] tmp = extra.getString(KEY_ACCOUNTS).split(",");
		int len = tmp.length;
		mAccounts = new int[len];
		
		for (int i = 0; i < len; ++i)
		{
			try
			{
				mAccounts[i] = new Integer(tmp[i]);
			}
			catch (NumberFormatException e) { }
		}
		
		mQuery = extra.getString(KEY_QUERY).split(",");
		len = mQuery.length;
		for (int i = 0; i < len; ++i)
		{
			mQuery[i] = mQuery[i].trim();
		}
	}
	
	private ArrayList<Long> getDateRanges(long begin, long end, int range)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(begin);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		ArrayList<Long> ranges = new ArrayList<Long>();
		
		long time = cal.getTimeInMillis();
		if (mChartType == ChartMenuActivity.CHART_TYPE_PIE)
		{
			ranges.add(time);
		}
		else
		{
			int field = Calendar.DATE;
			int factor = 1;
			if (range == ChartMenuActivity.X_AXIS_DAILY)
			{
				field = Calendar.DATE;
			}
			else if (range == ChartMenuActivity.X_AXIS_WEEKLY)
			{
				field = Calendar.DATE;
				factor = 7;
			}
			else if (range == ChartMenuActivity.X_AXIS_BIWEEKLY)
			{
				field = Calendar.DATE;
				factor = 14;
			}
			else if (range == ChartMenuActivity.X_AXIS_MONTHLY)
			{
				field = Calendar.MONTH;
			}
			else if (range == ChartMenuActivity.X_AXIS_YEARLY)
			{
				field = Calendar.YEAR;
			}
			
			do
			{
				ranges.add(time);
				cal.add(field, factor);
				time = cal.getTimeInMillis();
			} while (time <= end);
		}

		cal.setTimeInMillis(end);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		ranges.add(cal.getTimeInMillis());
		
		return ranges;
	}
	
	private BigDecimal[] getInitialBalances(long start_date, String[] accounts)
	{
		String[] acc_proj = new String[] { "id", "balance" };
		String[] trans_proj = new String[] { "amount" };
		String date_range = " date < " + start_date;
		String selection;
		Cursor cur;
		BigDecimal[] balances = new BigDecimal[accounts.length];
		Uri account_uri = Uri.parse("content://net.gumbercules.loot.accountprovider/");
		Uri trans_uri = Uri.parse("content://net.gumbercules.loot.transactionprovider/transaction");
		
		for (int i = 0; i < accounts.length; ++i)
		{
			// get initial balance from account
			cur = managedQuery(account_uri, acc_proj, accounts[i].replace("account", "id"), null, null);
			
			if (cur == null || !cur.moveToFirst())
			{
				cur.close();
				continue;
			}
			
			balances[i] = BigDecimal.ZERO;
			
			do
			{
				balances[i] = balances[i].add(BigMoney.money(cur.getString(1)));
			} while (cur.moveToNext());
			cur.close();
			
			// get the balance before the start date
			selection = date_range + " and " + accounts[i];
			cur = managedQuery(trans_uri, trans_proj, selection, null, null);
			
			if (cur == null || !cur.moveToFirst())
			{
				cur.close();
				continue;
			}
			
			do
			{
				balances[i] = balances[i].add(BigMoney.money(cur.getString(0)));
			} while (cur.moveToNext());
			cur.close();
		}
		
		return balances;
	}
	
	private void getData(ArrayList<Long> ranges, Uri uri)
	{
		int len = ranges.size();
		String date_range = null;
		String selection = null;
		Cursor cur = null;
		String[] projection = new String[] { "id", "amount" };
		long start_date = 0;
		int account_data_set, selection_data_set, count, id, prev_id;
		BigDecimal total, amount;
		int total_count = 0;
		BigDecimal total_amount = BigDecimal.ZERO;
		BigDecimal total_balance = BigDecimal.ZERO;
		BigDecimal[] running_balance = null;
		boolean negative = true;
		
		String[] searchAccounts = getSearchAccounts(mAccounts);
		running_balance = getInitialBalances(ranges.get(0), searchAccounts);
		
		for (int i = 0; i < len - 1; ++i)
		{
			start_date = ranges.get(i);
			date_range = " (date >= " + start_date + " and date < " + ranges.get(i + 1) + ") ";
			account_data_set = 0;

			for (String account : searchAccounts)
			{
				selection_data_set = 0;
				for (String query : getSearchQueries(mQuery, mQueryFields))
				{
					selection = date_range + " and " + account + " and " + query;
					cur = managedQuery(uri, projection, selection, null, null);
					
					count = 0;
					id = 0;
					prev_id = 0;
					total = BigDecimal.ZERO;
					amount = BigDecimal.ZERO;

					if (cur.moveToFirst())
					{
						do
						{
							id = cur.getInt(0);
							amount = BigMoney.money(cur.getString(1));
							
							if (id != prev_id)
							{
								++count;
								total = total.add(amount);
								running_balance[account_data_set] = running_balance[account_data_set].add(amount);
								
								if (amount.compareTo(BigDecimal.ZERO) == 1) // amount > 0.0
								{
									negative = false;
								}
							}
							
							prev_id = id;
						} while (cur.moveToNext());
					}
					
					mHandler.addToDataset(start_date, account_data_set, account,
							selection_data_set, query, count, total, running_balance[account_data_set]);
					
					// total over all points
					total_count += count;
					total_amount = total_amount.add(total);

					cur.close();
					++selection_data_set;
				}
				
				total_balance = total_balance.add(running_balance[account_data_set++]);
			}
						
			mHandler.addTicks(start_date);
			mHandler.addTotals(total_count, total_amount, total_balance);
			mHandler.setNegativeDataset(negative);
		}
	}
	
	private Uri buildUri(int fields, int y_axis)
	{
		String uri = "content://net.gumbercules.loot.transactionprovider/transaction";
		
		return Uri.parse(uri);
	}
	
	private String[] getSearchAccounts(int[] accounts)
	{
		String[] queries = null;
		
		if (accounts == null)
		{
			queries = new String[1];
			queries[0] = "account = -1";
			
			return queries;
		}
		
		int len = accounts.length;
		if (mGrouping == ChartMenuActivity.GROUPING_ACCOUNTS || 
				mGrouping == ChartMenuActivity.GROUPING_ALL)
		{
			return getAccountsConcat(accounts);
		}
		else
		{
			queries = new String[len];
		}
		
		for (int i = 0; i < len; ++i)
		{
			queries[i] = "account = " + accounts[i];
		}
		
		return queries;
	}
	
	private String[] getAccountsConcat(int[] accounts)
	{
		String query = "(";
		for (int a : accounts)
		{
			query += a + ",";
		}

		query += "-1)";
		String[] queries = new String[1];

		queries[0] = "account in " + query;
		
		return queries;
	}
	
	private String[] getSearchQueries(String[] query, int fields)
	{
		String[] queries = null;
		
		if (query == null || (query.length >= 1 && query[0].equals("")))
		{
			queries = new String[1];
			if (fields == ChartMenuActivity.SEARCH_FIELD_PARTIES)
			{
				queries[0] = "party like '%'";
			}
			else if (fields == ChartMenuActivity.SEARCH_FIELD_TAGS)
			{
				queries[0] = "name like '%'";
			}
			else if (fields == ChartMenuActivity.SEARCH_FIELD_BOTH)
			{
				queries[0] = "(name like '%' or party like '%')";
			}
			
			return queries;
		}
		
		int len = query.length;
		if (mGrouping == ChartMenuActivity.GROUPING_SEARCH || 
				mGrouping == ChartMenuActivity.GROUPING_ALL)
		{
			return getQueriesConcat(query, fields);
		}
		else
		{
			queries = new String[len];
		}

		for (int i = 0; i < len; ++i)
		{
			if (fields == ChartMenuActivity.SEARCH_FIELD_PARTIES)
			{
				queries[i] = "party = " + DatabaseUtils.sqlEscapeString(query[i]);
			}
			else if (fields == ChartMenuActivity.SEARCH_FIELD_TAGS)
			{
				queries[i] = "name = " + DatabaseUtils.sqlEscapeString(query[i]);
			}
			else if (fields == ChartMenuActivity.SEARCH_FIELD_BOTH)
			{
				queries[i] = "(party = " + DatabaseUtils.sqlEscapeString(query[i]) + 
						" or name = " + DatabaseUtils.sqlEscapeString(query[i]) + ")";
			}
		}
		
		return queries;
	}
	
	private String[] getQueriesConcat(String[] query, int fields)
	{
		String parties = "(";
		String tags = "(";
		for (String q : query)
		{
			parties += DatabaseUtils.sqlEscapeString(q) + ",";
			tags += DatabaseUtils.sqlEscapeString(q) + ",";
		}

		parties += "null)";
		tags += "null)";
		String[] queries = new String[1];

		if (fields == ChartMenuActivity.SEARCH_FIELD_PARTIES)
		{
			queries[0] = "party in " + parties;
		}
		else if (fields == ChartMenuActivity.SEARCH_FIELD_TAGS)
		{
			queries[0] = "name in " + tags;
		}
		else if (fields == ChartMenuActivity.SEARCH_FIELD_BOTH)
		{
			queries[0] = "(party in " + parties + " or name in " + tags + ")";
		}
		
		return queries;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, SAVE_ID, 0, R.string.save_chart)
			.setShortcut('1', 'c')
			.setIcon(android.R.drawable.ic_menu_save);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case SAVE_ID:
				saveChart();
		    	break;
		}
		
		return true;
	}
	
	private void saveChart()
	{
		// get filename from user
		final EditText edit = new EditText(this);
		final Context me = this;
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.save_chart)
				.setMessage(R.string.enter_filename)
				.setView(edit)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (which == DialogInterface.BUTTON_POSITIVE)
						{
							// tell handler to save the graph
							try
							{
								mHandler.saveChart(edit.getText().toString());
							}
							catch (Exception e)
							{
								Toast.makeText(me, R.string.save_error, Toast.LENGTH_LONG).show();
								Log.e(TAG + ".saveChart()", "An error occurred while saving the graph");
								Log.e(TAG + ".saveChart()", Log.getStackTraceString(e));
							}
							dialog.dismiss();
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (which == DialogInterface.BUTTON_NEGATIVE)
						{
							dialog.cancel();
						}
					}
				})
				.create();
		
		if (MemoryStatus.checkMemoryStatus(this, true))
		{
			dialog.show();
		}
	}
}
