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
