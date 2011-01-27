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

package net.gumbercules.loot.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.ListView;

public class ColorSchemePreference extends DialogPreference
{
	private ListView mList;
	private int mSelectedPos;
	
	public ColorSchemePreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ColorSchemePreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);
		
		if (positiveResult)
		{
			SharedPreferences.Editor prefs = getEditor();
			prefs.putInt("scheme_id", mSelectedPos);
			prefs.commit();
		}
	}
	
	public void dismiss(boolean positive, int selected)
	{
		mSelectedPos = selected;
		onDialogClosed(positive);
		getDialog().dismiss();
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
		super.onPrepareDialogBuilder(builder);

		mList = new ListView(getContext());
		
		mList.setAdapter(new ColorSchemeAdapter(getContext(), this));
		mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		builder.setView(mList);
		builder.setPositiveButton(null, null);
		builder.setNegativeButton(null, null);
	}
	
}
