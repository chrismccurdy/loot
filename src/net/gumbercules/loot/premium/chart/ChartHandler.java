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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

public class ChartHandler
{
	private static final String TAG				= "net.gumbercules.loot.premium.ChartHandler";
	
	public static final String CHART_LOCATION	= Environment.getExternalStorageDirectory() + "/loot/chart/";
	
	private static final int ID_COUNT	= 0;
	private static final int ID_TOTAL	= 1;
	private static final int ID_BALANCE	= 2;
	
	private Activity mActivity;
	private WebView mWeb;
	private ProgressDialog mPd;
	
	private String mType;
	private int mYaxis;
	private long mXaxis;
	private int mGrouping;
	private boolean mNegative;
	
	private Object[] mTotals;
	
	private HashMap<Integer, String> mAccounts;
	private HashMap<Integer, String> mQueries;
	private ArrayList<Long> mDates;
	private HashMap<String, Object[]> mData;
	private HashMap<String, String> mLabels;
	
	private String mJsonData;
	private String mJsonOptions;
	
	public ChartHandler(Activity act, WebView wv, ProgressDialog pd, int type,
			int xaxis, int yaxis, int group)
	{
		mActivity = act;
		mWeb = wv;
		mPd = pd;
		setType(type);
		setXaxis(xaxis);
		mYaxis = yaxis;
		mGrouping = group;
		mTotals = new Object[3];
		mAccounts = new HashMap<Integer, String>();
		mQueries = new HashMap<Integer, String>();
		mDates = new ArrayList<Long>();
		mData = new HashMap<String, Object[]>();
		mLabels = new HashMap<String, String>();
	}
	
	public void setType(int type)
	{
		switch (type)
		{
			case ChartMenuActivity.CHART_TYPE_BAR:
				mType = "bars";
				break;
				
			case ChartMenuActivity.CHART_TYPE_PIE:
				mType = "pie";
				break;
				
			case ChartMenuActivity.CHART_TYPE_LINE:
				mType = "lines";
				break;
		}
	}
	
	public void setXaxis(int x)
	{
		final long day = 86400000;
		switch (x)
		{
			case ChartMenuActivity.X_AXIS_DAILY:
				mXaxis = day;
				break;
				
			case ChartMenuActivity.X_AXIS_WEEKLY:
				mXaxis = day * 7;
				break;
				
			case ChartMenuActivity.X_AXIS_BIWEEKLY:
				mXaxis = day * 14;
				break;
				
			case ChartMenuActivity.X_AXIS_MONTHLY:
				mXaxis = day * 28;
				break;
				
			case ChartMenuActivity.X_AXIS_YEARLY:
				mXaxis = day * 365;
				break;
		}
	}
	
	public void setView(WebView wv)
	{
		mWeb = wv;
	}
	
	public void addTicks(long start_date)
	{
		if (!mDates.contains(start_date))
		{
			mDates.add(start_date);
			Collections.sort(mDates);
		}
	}
	
	public void addTotals(int count, BigDecimal amount, BigDecimal balance)
	{
		mTotals[ID_COUNT] = count;
		mTotals[ID_TOTAL] = amount;
		mTotals[ID_BALANCE] = balance;
	}
	
	public void addToDataset(long start_date, int account_id, String account_name,
			int query_id, String query, int count, BigDecimal total, BigDecimal balance)
	{
		if (!mAccounts.containsKey(account_id))
		{
			mAccounts.put(account_id, account_name);
		}

		if (!mQueries.containsKey(query_id))
		{
			mQueries.put(query_id, query);
		}

		addTicks(start_date);
		
		Object[] obj = new Object[3];
		obj[ID_COUNT] = count;
		obj[ID_TOTAL] = total;
		obj[ID_BALANCE] = balance;
		
		mData.put(getKey(query_id, account_id, start_date), obj);
	}
	
	public void setNegativeDataset(boolean n)
	{
		mNegative = n;
	}
	
	public String getKey(int x, int y, long z)
	{
		return (x + "," + y + "," + z);
	}
	
	public JSONObject getTypeObject(int length)
	{
		JSONObject type = null;
		try
		{
			type = new JSONObject()
					.put("show", true)
					.put("fill", true);
			if (mType.equals("bars"))
			{
				type.put("barWidth", mXaxis)
						.put("align", "center");
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return type;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJsonOptions()
	{
		JSONObject options = new JSONObject();
		try
		{
			JSONArray ticks = new JSONArray();
			ArrayList<Long> dates = (ArrayList<Long>) mDates.clone();
			for (Long date : dates)
			{
				ticks.put(date);
			}
			
			if (!mType.equals("pie"))
			{
				options.put("xaxis", new JSONObject()
						.put("mode", "time")
						.put("timeformat", "%y-%m-%d")
						.put("ticks", ticks));
				options.put("legend", new JSONObject()
						.put("show", true)
						.put("backgroundOpacity", 0.40));
			}
			else
			{
				options.put("series", new JSONObject()
						.put("pie", new JSONObject()
								.put("show", true)
								.put("label", new JSONObject()
										.put("show", true)
										.put("background", new JSONObject()
												.put("color", "#fff")
												.put("opacity", 0.40)))));
				options.put("legend", new JSONObject()
						.put("show", false));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return options;
	}
	
	public void saveChart(String name)
		throws FileNotFoundException, IOException
	{
		// write mJsonArray & mJsonOptions to a file
		String filename = CHART_LOCATION + name;
		
		File f = new File(filename);
		File parent = f.getParentFile();
		
		if (!parent.exists())
		{
			parent.mkdirs();
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename), 8096);
		bw.write(mJsonData);
		bw.newLine();
		bw.write(mJsonOptions);
		bw.newLine();
		bw.close();
	}
	
	public void loadChart(String filename)
		throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException
	{
		// read file into mJsonArray & mJsonOptions
		BufferedReader br = new BufferedReader(new FileReader(CHART_LOCATION + filename), 8096);
		
		mJsonData = br.readLine();
		mJsonOptions = br.readLine();
		
		br.close();
	}
	
	public String getLabel(String accounts, String query)
	{
		if (mLabels.containsKey(accounts + query))
		{
			return mLabels.get(accounts + query);
		}
		
		String label = "";
		String[] accts;
		
		if (mGrouping == ChartMenuActivity.GROUPING_ACCOUNTS || mGrouping == ChartMenuActivity.GROUPING_ALL)
		{
			accts = accounts.substring(accounts.indexOf("(") + 1).replace(")", "").split(",");
		}
		else
		{
			accts = new String[1];
			try
			{
				accts[0] = accounts.split(" = ")[1];
			}
			catch (Exception e)
			{
				accts[0] = "-1";
				Log.i(TAG + ".getLabel()", "error parsing account id");
			}
		}
		
		String uri = "content://net.gumbercules.loot.accountprovider/";
		
		for (String s : accts)
		{
			int acct_id = Integer.valueOf(s);
			Cursor cur = mActivity.managedQuery(Uri.parse(uri + acct_id),
					new String[]{"name"}, null, null, null);

			if (cur != null)
			{
				if (cur.moveToFirst())
				{
					label += cur.getString(0) + ", ";
				}
				cur.close();
			}
		}
		
		label += query.replace("(", "").replace(")", "").replace(" =", ":").replace(" or", ",");
		label = label.replace("name:", "tag:");
		mLabels.put(accounts + query, label);
		
		return label;
	}
	
	public void loadGraph()
	{
		if (mJsonData == null || mJsonOptions == null)
		{
			getJsonValues();
		}
		
		Log.i(TAG + ".loadGraph()", mJsonData);
		Log.i(TAG + ".loadGraph()", mJsonOptions);

		String opts = mJsonData + ", " +
				mJsonOptions;
		mWeb.loadUrl("javascript:getGraph(" + opts + ");");
		
		mPd.dismiss();
	}
	
	@SuppressWarnings("unchecked")
	private void getJsonValues()
	{
		JSONArray arr = new JSONArray();
		JSONArray data = null;
		JSONArray element = null;
		JSONObject result;
		Object[] obj;
		int id = 0;
		
		if (mYaxis == ChartMenuActivity.Y_AXIS_NUMBER)
		{
			id = ID_COUNT;
		}
		else if (mYaxis == ChartMenuActivity.Y_AXIS_TOTAL)
		{
			id = ID_TOTAL;
		}
		else if (mYaxis == ChartMenuActivity.Y_AXIS_BALANCE)
		{
			id = ID_BALANCE;
		}
		
		for (Integer q_key : mQueries.keySet())
		{
			for (Integer a_key : mAccounts.keySet())
			{
				result = new JSONObject();
				data = new JSONArray();
				
				ArrayList<Long> dates = (ArrayList<Long>) mDates.clone();
				for (Long s_key : dates)
				{
					element = new JSONArray();
					element.put(s_key);
					
					String d_key = getKey(q_key, a_key, s_key);

					if (mData.containsKey(d_key))
					{
						obj = mData.get(d_key);
						
						if (mNegative)
						{
                            // --------------------------------------------------------------
                            // TODO really test this!!!!!!!!!!!
                            // don't know if the exception will still show up with BigDecimal
                            // ---------------------------------------------------------------
							// ugly hack to avoid JSONException
							element.put(((BigDecimal)obj[id]).abs());
						}
						else
						{
							element.put(obj[id]);
						}
					}
					else
					{
						element.put(0);
					}
					
					if (mType.equals("pie"))
					{
						try
						{
							double ratio = ((BigDecimal)element.get(1) / 
									((BigDecimal)mTotals[id])) * 360.0;
							result.put("data", ratio);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						data.put(element);
					}
				}
				
				String label = getLabel(mAccounts.get(a_key), mQueries.get(q_key));
				
				try
				{
					result.put("label", label);
					if (!mType.equals("pie"))
					{
						result.put(mType, getTypeObject(data.length()));
						result.put("data", data);
						if (mType.equals("bars"))
						{
							result.put("stack", true);
						}
					}
					arr.put(result);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				
			}
		}
		
		mJsonData = arr.toString();
		mJsonOptions = getJsonOptions().toString();
	}
}
