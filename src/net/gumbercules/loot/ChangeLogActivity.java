package net.gumbercules.loot;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChangeLogActivity extends ListActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.changelog);
		setListAdapter(new ChangeLogAdapter(this));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		((ChangeLogAdapter)l.getAdapter()).toggle(position);
	}

	public class ChangeLogAdapter extends BaseAdapter
	{
		private String[] mVersions;
		private String[] mLogs;
		private boolean[] mExpanded;
		
		private Context mContext;
		
		public ChangeLogAdapter(Context context)
		{
			mContext = context;
			mVersions = context.getResources().getStringArray(R.array.versions);
			mLogs = context.getResources().getStringArray(R.array.changelog);
			mExpanded = new boolean[mLogs.length];
		}
		
		public int getCount()
		{
			return mVersions.length;
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			ChangeLogView clv;
			
			if (convertView == null)
			{
				clv = new ChangeLogView(mContext, mVersions[position], mLogs[position], mExpanded[position]);
			}
			else
			{
				clv = (ChangeLogView)convertView;
				clv.setTitle(mVersions[position]);
				clv.setLog(mLogs[position]);
				clv.setExpanded(mExpanded[position]);
			}
			
			return clv;
		}
		
        public void toggle(int position)
        {
            mExpanded[position] = !mExpanded[position];
            notifyDataSetChanged();
        }
	}
	
	public class ChangeLogView extends LinearLayout
	{
		private TextView mTitle;
		private TextView mLog;
		
		public ChangeLogView(Context context, String title, String log, boolean expanded)
		{
			super(context);
			
			setOrientation(VERTICAL);
			
			mTitle = new TextView(context);
			mTitle.setText(title);
			mTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 
            		LayoutParams.WRAP_CONTENT));
			
			mLog = new TextView(context);
			mLog.setText(log);
			mLog.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            addView(mLog, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 
            		LayoutParams.WRAP_CONTENT));
            
            setExpanded(expanded);
		}
		
        public void setTitle(String title)
        {
            mTitle.setText(title);
        }
        
        public void setLog(String words)
        {
            mLog.setText(words);
        }
        
        public void setExpanded(boolean expanded)
        {
            mLog.setVisibility(expanded ? VISIBLE : GONE);
        }
	}
}
