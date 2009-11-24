package net.gumbercules.loot;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChangeLogDialog extends Dialog
{
	private ListView mListView;
	
	protected ChangeLogDialog(Context context)
	{
		super(context);
		
		setTitle(R.string.changelog);
		mListView = new ListView(context);
		mListView.setAdapter(new ChangeLogAdapter(context));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> adapter, View view, int pos, long id)
				{
					((ChangeLogAdapter)adapter.getAdapter()).toggle(pos);
				}
			});
		setContentView(mListView);
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
			return mLogs[position];
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
            Log.i("TEEEEEEEEEEEEEEEEEEEEST", mLogs[position]);
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
			
			mTitle = new TextView(context);
			mTitle.setText(title);
            addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 
            		LayoutParams.WRAP_CONTENT));
			
			mLog = new TextView(context);
			mLog.setText(log);
            addView(mLog, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 
            		LayoutParams.WRAP_CONTENT));
            
            mLog.setVisibility(expanded ? VISIBLE : GONE);
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
