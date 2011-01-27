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

package net.gumbercules.loot.premium.widget;

import net.gumbercules.loot.R;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class WidgetConfigure extends ListActivity
{
	private int mAppWidgetId;
	
	public static final String PREFS_PREFIX		= "WIDGET_";
	public static final String WIDGET_BALANCE	= PREFS_PREFIX + "BAL_";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		if (extras != null)
		{
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int account_id = prefs.getInt(PREFS_PREFIX + mAppWidgetId, -1);
		
		Cursor cur = managedQuery(Uri.parse("content://net.gumbercules.loot.accountprovider/"),
				new String[]{"name", "id"}, "purged = 0", null, null);
		
		if (cur != null)
		{
			String[] accounts = new String[cur.getCount()];
			int select = 0;
			if (cur.moveToFirst())
			{
				int i = 0;
				do
				{
					if (cur.getInt(1) == account_id)
					{
						select = i;
					}
					accounts[i++] = cur.getString(0);
				} while (cur.moveToNext());
			}
	
			ArrayAdapter<String> account_adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_single_choice, accounts);
			setListAdapter(account_adapter);
	
			setContentView(R.layout.premium_conf);
	
			ListView list = getListView();
			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			list.setItemChecked(select, true);
		}
		else
		{
			setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_single_choice));
			setContentView(R.layout.loot_not_installed);
			
			Button download = (Button)findViewById(R.id.download);
			
	        download.setOnClickListener(new Button.OnClickListener()
	        {
				public void onClick(View v)
				{
		            Intent intent = new Intent(Intent.ACTION_VIEW);
		            intent.setData(Uri.parse("market://search?q=pname:net.gumbercules.loot"));

		            try
		            {
		                startActivity(intent);
		            }
		            catch (ActivityNotFoundException e)
		            {
		                Toast.makeText(v.getContext(), R.string.no_market, Toast.LENGTH_SHORT).show();
		            }
				}       	
	        });

		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			updateWidget();
			finish();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private void updateWidget()
	{
		ListView list = getListView();
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		
		if (list.getCount() == 0)
		{
			setResult(RESULT_CANCELED, resultValue);
			return;
		}
		
		String account_name = (String)list.getItemAtPosition(list.getCheckedItemPosition());
		Cursor cur = managedQuery(Uri.parse("content://net.gumbercules.loot.accountprovider/"),
				new String[]{"id"}, "name = ?", new String[]{account_name}, null);
		int account_id = 1;
		if (cur.moveToFirst())
		{
			account_id = cur.getInt(0);
		}
		
		Spinner spinner = (Spinner)findViewById(R.id.balance_spinner);
		int balance = spinner.getSelectedItemPosition();
		
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
		prefs.putInt(PREFS_PREFIX + mAppWidgetId, account_id);
		prefs.putInt(WIDGET_BALANCE + mAppWidgetId, balance);
		prefs.commit();
		
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		LootPremiumWidgetProvider.updateWidget(this, manager, mAppWidgetId, account_id, balance);
		
		setResult(RESULT_OK, resultValue);
		finish();
	}
}
