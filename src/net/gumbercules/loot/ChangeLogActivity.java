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

package net.gumbercules.loot;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChangeLogActivity extends ListActivity
{
	private BitmapDrawable mCollapsed;
	private BitmapDrawable mExpanded;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_more);
		Matrix mtx = new Matrix();
		mtx.postRotate(-90.0f);
		Bitmap new_bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mtx, true);
		mCollapsed = new BitmapDrawable(new_bmp);
		mExpanded = new BitmapDrawable(bmp);
		
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
		private ImageView mImage;
		private TextView mLog;
		
		public ChangeLogView(Context context, String title, String log, boolean expanded)
		{
			super(context);
			
			setOrientation(VERTICAL);
			
			LinearLayout header = new LinearLayout(context);
			header.setOrientation(LinearLayout.HORIZONTAL);
			
			mTitle = new TextView(context);
			mTitle.setText(title);
			mTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            header.addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
            		LayoutParams.WRAP_CONTENT, 1.0f));
            mImage = new ImageView(context);
            mImage.setImageDrawable(mCollapsed);
            header.addView(mImage, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
            		LayoutParams.WRAP_CONTENT));
            
            addView(header, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
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
            mImage.setImageDrawable(expanded ? mExpanded : mCollapsed);
        }
	}
}
