package net.gumbercules.loot.repeat;

import net.gumbercules.loot.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		RepeatAdapter ra = new RepeatAdapter(this, R.layout.repeat_row);
		setListAdapter(ra);
		fillRepeatList();
	
		setContentView(R.layout.repeat_manager);

		getListView().setOnCreateContextMenuListener(this);
	}
	
	private void fillRepeatList()
	{
		RepeatSchedule rs;
		RepeatAdapter ra = (RepeatAdapter)getListAdapter();
		ra.clear();
		int[] ids = RepeatSchedule.getRepeatIds();
		if (ids == null)
		{
			return;
		}

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
			
			ra.add(rs);
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
		// TODO: open new activity to edit this repeat
	}
}
