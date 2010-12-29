package net.gumbercules.loot.preferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.gumbercules.loot.R;
import net.gumbercules.loot.backend.Database;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.EditText;

public class SettingsActivity extends PreferenceActivity
{
	private static final String TAG = "net.gumbercules.loot.preferences.SettingsActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);

		PreferenceScreen about = (PreferenceScreen)findPreference("about");
		try
		{
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			about.setTitle("About - " + pi.versionName);
		}
		catch (NameNotFoundException e)
		{
			about.setTitle("About - Unknown Version");
		}
		
		CheckBoxPreference security = (CheckBoxPreference)findPreference("security");
		security.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean val = new Boolean(newValue.toString());
				if (!val)
					Database.setOption("pin", new byte[] {0});
				
				return true;
			}
		});
		
		EditTextPreference pin = (EditTextPreference)findPreference("pin");
		EditText pinEdit = pin.getEditText();
		pinEdit.setKeyListener(new DigitsKeyListener());
		pin.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				if (newValue.equals(""))
					return false;
				
				String val = (String)newValue;
				byte[] bytes = {0};
				try
				{
					MessageDigest md = MessageDigest.getInstance("SHA-1");
					md.update(val.getBytes());
					bytes = md.digest();
				}
				catch (NoSuchAlgorithmException e)
				{
					return false;
				}
				
				Database.setOption("pin", bytes);
				
				return true;
			}
		});
		
		// online backup is not available before froyo
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
		{
			CheckBoxPreference backup = (CheckBoxPreference)findPreference("online_backup");
			backup.setEnabled(false);
		}
		
		CheckBoxPreference auto_purge = (CheckBoxPreference)findPreference("purge");
		auto_purge.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean val = new Boolean(newValue.toString());
				if (!val)
					Database.setOption("auto_purge_days", -1);
				
				return true;
			}
		});
		
		EditTextPreference purge_days = (EditTextPreference)findPreference("purge_days");
		EditText purgeDaysEdit = purge_days.getEditText();
		purgeDaysEdit.setKeyListener(new DigitsKeyListener());
		purge_days.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				if (newValue.equals(""))
					return false;
				
				int val = Integer.valueOf((String)newValue);
				if (val <= 0)
					return false;
				
				Database.setOption("auto_purge_days", val);
					
				return true;
			}
		});
		
		CheckBoxPreference repeat = (CheckBoxPreference)findPreference("repeat");
		repeat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean val = new Boolean(newValue.toString());
				if (!val)
					Database.setOption("post_repeats_early", -1);
					
				return true;
			}
		});
		
		EditTextPreference repeats_early = (EditTextPreference)findPreference("repeat_days");
		EditText repeatsEarlyEdit = repeats_early.getEditText();
		repeatsEarlyEdit.setKeyListener(new DigitsKeyListener());
		repeats_early.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				if (newValue.equals("") || newValue == null)
				{
					Log.i(TAG + ".onCreate$onPreferenceChange", "newValue is empty");
					return false;
				}
				
				int val = Integer.valueOf((String)newValue);
				if (val <= 0)
				{
					Log.i(TAG + ".onCreate$onPreferenceChange", "entered value is <= 0");
					return false;
				}
				
				Database.setOption("post_repeats_early", val);
					
				return true;
			}
		});
		
		CheckBoxPreference override_locale = (CheckBoxPreference)findPreference("override_locale");
		override_locale.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean val = new Boolean(newValue.toString());
				if (!val)
					Database.setOption("override_locale", "");
				
				return true;
			}	
		});
		
		ListPreference currency_list = (ListPreference)findPreference("locale_list");
		currency_list.setEntries(R.array.currency_names);
		currency_list.setEntryValues(R.array.iso_codes);
		
		currency_list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				if (newValue.equals(""))
					return false;
				
				String val = (String)newValue;
				Database.setOption("override_locale", val);
				
				return true;
			}
		});
	}
}
