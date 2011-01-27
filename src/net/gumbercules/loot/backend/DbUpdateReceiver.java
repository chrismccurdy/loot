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

package net.gumbercules.loot.backend;

import net.gumbercules.loot.premium.backup.DbBackupService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DbUpdateReceiver extends BroadcastReceiver
{
	private static final long TIME_OFFSET	= 300000;	// 5 minutes
	private static final String TAG			= "net.gumbercules.loot.backend.DbUpdateReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.getBoolean("auto_backup", false))
		{
			return;
		}
		
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent(DbBackupService.ACTION_BACKUP, null);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		
		long at = System.currentTimeMillis() + TIME_OFFSET;
		
		alarm.cancel(pi);
		alarm.set(AlarmManager.RTC, at, pi);
		
		Log.i(TAG + ".onReceive", "backup alarm scheduled for " + at);
	}
}
