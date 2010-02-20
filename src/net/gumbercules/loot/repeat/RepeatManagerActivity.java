package net.gumbercules.loot.repeat;

import net.gumbercules.loot.R;
import android.app.ListActivity;
import android.os.Bundle;

public class RepeatManagerActivity extends ListActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		RepeatAdapter ra = new RepeatAdapter(this, R.layout.repeat_row);
		setListAdapter(ra);
		fillRepeatList();
		
		setContentView(R.layout.repeat_manager);
	}
	
	private void fillRepeatList()
	{
		RepeatSchedule rs;
		RepeatAdapter ra = (RepeatAdapter)getListAdapter();
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
	}
}
