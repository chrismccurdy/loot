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

package net.gumbercules.loot.repeat;

import net.gumbercules.loot.R;
import net.gumbercules.loot.transaction.TransactionEdit;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class RepeatManagerActivity extends ListActivity
{
	private static final int CONTEXT_EDIT	= Menu.FIRST;
	private static final int CONTEXT_DELETE	= Menu.FIRST + 1;
	
	private static final String TAG	= "net.gumbercules.loot.repeat.RepeatManagerActivity";
	
	public static final String ID_ACCOUNT	= "id_account";
	
	private int mAccountId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle bun = getIntent().getExtras();
		if (bun != null)
		{
			mAccountId = bun.getInt(ID_ACCOUNT);
		}
		else
		{
			mAccountId = 0;
		}
		
		RepeatAdapter ra = new RepeatAdapter(this, R.layout.repeat_row);
		setListAdapter(ra);
		fillRepeatList();
	
		setContentView(R.layout.repeat_manager);

		getListView().setOnCreateContextMenuListener(this);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		fillRepeatList();
	}

	private void fillRepeatList()
	{
		RepeatSchedule rs;
		RepeatAdapter ra = (RepeatAdapter)getListAdapter();
		ra.clear();
		int[] ids = RepeatSchedule.getRepeatIds();
		if (ids != null)
		{
			for (int id : ids)
			{
				rs = RepeatSchedule.getSchedule(id);
				if (rs == null)
				{
					continue;
				}
				
				rs.trans = rs.getTransaction();
				if (rs.trans == null)
				{
					continue;
				}
				
				if (mAccountId != 0 && rs.trans.account != mAccountId)
				{
					continue;
				}
				
				ra.add(rs);
			}
		}
		
		ra.notifyDataSetChanged();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info;
		try
		{
			info = (AdapterContextMenuInfo)item.getMenuInfo();
		}
		catch (ClassCastException e)
		{
			Log.e(TAG + ".onCreateContextMenu()", "Bad ContextMenuInfo", e);
			return false;
		}
		
		int id = (int)getListAdapter().getItemId(info.position);
		final RepeatSchedule rs = RepeatSchedule.getSchedule(id);
		
		switch (item.getItemId())
		{
			case CONTEXT_EDIT:
				editRepeat(id);
				break;
				
			case CONTEXT_DELETE:
				AlertDialog dialog = new AlertDialog.Builder(this)
					.setMessage("Are you sure you wish to delete this repeat schedule?")
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							rs.erase(true);
							fillRepeatList();
						}
					})
					.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) { }
					})
					.create();
				dialog.show();
				break;
		}
		
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		AdapterView.AdapterContextMenuInfo info;
		try
		{
			info = (AdapterContextMenuInfo)menuInfo;
		}
		catch (ClassCastException e)
		{
			Log.e(TAG + ".onCreateContextMenu()", "Bad ContextMenuInfo", e);
			return;
		}
		
		RepeatSchedule rs = (RepeatSchedule)getListAdapter().getItem(info.position);
		if (rs == null)
		{
			return;
		}
		
		menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
		menu.add(0, CONTEXT_DELETE, 0, R.string.del);
	}
	
	private void editRepeat(int id)
	{
		ContentResolver cr = getContentResolver();
		String type = cr.getType(Uri.parse("content://net.gumbercules.loot.premium.settingsprovider/settings"));
		
		if (type == null)
		{
			new AlertDialog.Builder(this)
				.setMessage("Editing is only available with the purchase of Loot Premium.")
				.setPositiveButton(android.R.string.ok, null)
				.show();
		}
		else
		{
	    	Intent i = new Intent(this, TransactionEdit.class);
	    	i.putExtra(RepeatSchedule.KEY_ID, id);
	    	startActivityForResult(i, 0);
		}
	}
}
