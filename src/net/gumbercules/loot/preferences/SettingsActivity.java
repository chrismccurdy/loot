package net.gumbercules.loot.preferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.gumbercules.loot.R;
import net.gumbercules.loot.backend.Database;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

public class SettingsActivity extends PreferenceActivity
{
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
		
		Preference premium = (Preference)findPreference("premium_enabled");
		ContentResolver cr = getContentResolver();
		String type = cr.getType(Uri.parse("content://net.gumbercules.loot.premium.settingsprovider/settings"));
		
		String[] prefs = {"color_withdraw", "color_budget_withdraw", "color_deposit",
				"color_budget_deposit", "color_check", "color_budget_check", 
				"use_custom_colors", "date_format", /*"cal_enabled", "calendar_tag"*/};

		if (type != null)
		{
			premium.setTitle(R.string.premium_enabled_title);
			premium.setSummary(R.string.premium_enabled_body);
			
			setupPremiumSettings(prefs);
		}
		else
		{
			premium.setTitle(R.string.premium_disabled_title);
			premium.setSummary(R.string.premium_disabled_body);
			
			Preference cb_pref = null;
			for (String pref : prefs)
			{
				cb_pref = (Preference)findPreference(pref);
				cb_pref.setEnabled(false);
			}
			
		}
		
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
				if (newValue.equals(""))
					return false;
				
				int val = Integer.valueOf((String)newValue);
				if (val <= 0)
					return false;
				
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
	
	private void setupPremiumSettings(String[] prefs)
	{
		final String[] cr_keys = {"aw", "bw", "ad", "bd", "ac", "bc",
				 "custom", "date_format", "calendar", "tag"};
		
		int i = 0;
		final ContentResolver cr = getContentResolver();
		final String uri = "content://net.gumbercules.loot.premium.settingsprovider/";
		for (String pref : prefs)
		{
			final String key = cr_keys[i++];
			if (pref.equals("date_format"))
			{
				EditTextPreference date = (EditTextPreference)findPreference(pref);
				date.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
				{
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						ContentValues cv = new ContentValues();
						cv.put(key, (String)newValue);
						cr.update(Uri.parse(uri + key), cv, null, null);
						
						return true;
					}
				});
			}
			else if (pref.equals("calendar_tag"))
			{
				// set up edittext preference
				EditTextPreference tag = (EditTextPreference)findPreference(pref);
				tag.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
				{
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						if (newValue.equals(""))
							return false;

						ContentValues cv = new ContentValues();
						cv.put(key, (String)newValue);
						cr.update(Uri.parse(uri + "calendar/" + key), cv, null, null);
						
						return true;
					}
				});
			}
			else if (pref.equals("cal_enabled"))
			{
				// set up checkbox preference
				CheckBoxPreference calendar = (CheckBoxPreference)findPreference(pref);
				calendar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
				{
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						boolean val = new Boolean(newValue.toString());
						ContentValues cv = new ContentValues();
						cv.put(key, Boolean.toString(val));
						cr.update(Uri.parse(uri + key), cv, null, null);
						
						return true;
					}	
				});
			}
			else if (pref.equals("use_custom_colors"))
			{
				CheckBoxPreference cb = (CheckBoxPreference)findPreference(pref);
				cb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
				{
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						String[] colors = {"color_withdraw", "color_budget_withdraw", "color_deposit",
								"color_budget_deposit", "color_check", "color_budget_check"};
						SharedPreferences.Editor editor = preference.getEditor();
						ContentResolver cr = getContentResolver();
						String uri = "content://net.gumbercules.loot.premium.settingsprovider/color/";
						
						boolean val = new Boolean(newValue.toString());
						int j = 0;
						Cursor cur;
						for (String key : colors)
						{
							if (val)
							{
								cur = cr.query(Uri.parse(uri + cr_keys[j]), null, null, null, null);
								if (cur != null)
								{
									if (cur.moveToFirst())
										editor.putInt(key, cur.getInt(1));
									cur.close();
								}
							}
							else
							{
								editor.remove(key);
							}
							editor.commit();
							++j;
						}
						
						return true;
					}
				});
			}
			else
			{
				// set up color picker preference
				ColorPickerPreference picker = (ColorPickerPreference)findPreference(pref);
				picker.setDialogMessage("");
				picker.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
				{
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						int val = new Integer(newValue.toString());
						ContentValues cv = new ContentValues();
						cv.put(key, Integer.toString(val));
						cr.update(Uri.parse(uri + "color/" + key), cv, null, null);
						
						return true;
					}
				});
			}
		}
	}
}
