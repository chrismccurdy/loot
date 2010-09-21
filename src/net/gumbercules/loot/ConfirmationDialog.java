package net.gumbercules.loot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ConfirmationDialog extends AlertDialog
{

	public ConfirmationDialog(Context context)
	{
		super(context);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		LayoutInflater inflater = (LayoutInflater)getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View v = inflater.inflate(R.layout.confirmation_dialog, null);
		setView(v);
		
		CheckBox cb = (CheckBox)v.findViewById(R.id.show_again_check);

		cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				Editor prefs = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
				prefs.putBoolean("show_confirmation_on_restore", !isChecked);
				prefs.commit();
			}
			
		});
	}
}
