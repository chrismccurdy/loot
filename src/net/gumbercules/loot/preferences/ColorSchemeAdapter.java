package net.gumbercules.loot.preferences;

import java.util.ArrayList;

import net.gumbercules.loot.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ColorSchemeAdapter extends ArrayAdapter<String>
{
	private ArrayList<String> mSchemes;
	private LayoutInflater mInflater;
	private ColorSchemePreference mPref;
	private int mDefaultPos;

	public ColorSchemeAdapter(Context context, ColorSchemePreference pref)
	{
		super(context, 0);

		mSchemes = new ArrayList<String>();
		for (String s : context.getResources().getStringArray(R.array.color_schemes))
		{
			mSchemes.add(s);
		}
		
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPref = pref;
		mDefaultPos = pref.getPreferenceManager().getSharedPreferences().getInt("scheme_id", 0);
	}

	@Override
	public int getCount()
	{
		return mSchemes.size();
	}

	@Override
	public String getItem(int position)
	{
		return mSchemes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		String scheme = mSchemes.get(position);
		View v = createViewFromResource(convertView, parent, R.layout.color_scheme);
		
		final ListView parentList = (ListView)parent;
		final int pos = position;

		TextView[] views = new TextView[6];
		int[] resIds = new int[] { R.id.aw_color, R.id.bw_color, R.id.ad_color, R.id.bd_color,
				R.id.ac_color, R.id.bc_color };
		final int max = 6;
		final String[] colors = scheme.split(" ", max);
		
		OnClickListener onClick = new OnClickListener()
		{
			public void onClick(View view)
			{
				parentList.setSelection(pos);
				mPref.dismiss(true, pos);
				SharedPreferences.Editor edit = mPref.getEditor();
				String[] pref_keys = { "aw_color", "bw_color", "ad_color", "bd_color",
						"ac_color", "bc_color" };
				
				for (int i = 0; i < max; ++i)
				{
					edit.putInt(pref_keys[i], Color.parseColor(colors[i]));
				}
				edit.commit();
			}
		};
		
		for (int i = 0; i < max; ++i)
		{
			views[i] = (TextView)v.findViewById(resIds[i]);
			views[i].setBackgroundColor(Color.parseColor(colors[i]));
			views[i].setOnClickListener(onClick);
		}
		
		RadioButton radio = (RadioButton)v.findViewById(R.id.radio);
		radio.setOnClickListener(onClick);
		if (position == mDefaultPos)
		{
			radio.setChecked(true);
		}
		else
		{
			radio.setChecked(false);
		}

		return v;
	}

	private View createViewFromResource(View convertView, ViewGroup parent, int resource)
	{
		View view;
		
		if (convertView == null)
			view = mInflater.inflate(resource, parent, false);
		else
			view = convertView;
	
		return view;
	}
}
