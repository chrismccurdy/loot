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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

public class TipsDialog extends Dialog
{
	private int mCurrentTip;
	private int mNumTips;
	private String[] mTips;
	
	private TextView mText;

	public TipsDialog(Context context)
	{
		super(context);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.tips);

		mTips = context.getResources().getStringArray(R.array.tips);
		mNumTips = mTips.length;
		mCurrentTip = (int)(Math.random() * 100) % mNumTips;
		
		mText = (TextView)findViewById(R.id.tip_text);
		mText.setText(mTips[mCurrentTip]);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		CheckBox show_tips = (CheckBox)findViewById(R.id.tip_checkbox);
		boolean set_checked = false;
		if (prefs.getBoolean("tips", true))
		{
			set_checked = true;
		}
		show_tips.setChecked(set_checked);
		
		show_tips.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				prefs.edit().putBoolean("tips", isChecked).commit();
			}
		});
		
		ImageButton button = (ImageButton)findViewById(R.id.back_button);
		button.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mCurrentTip > 0)
				{
					mCurrentTip--;
				}
				else
				{
					mCurrentTip = mNumTips - 1;
				}
				
				mText.setText(mTips[mCurrentTip]);
			}
		});
		
		button = (ImageButton)findViewById(R.id.forward_button);
		button.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mCurrentTip = (mCurrentTip + 1) % mNumTips;
				mText.setText(mTips[mCurrentTip]);
			}
		});
	}

}
