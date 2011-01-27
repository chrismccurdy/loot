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

package net.gumbercules.loot.premium.backup;

import net.gumbercules.loot.backend.CopyThread;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DbBackupService extends Service
{
	public static final String ACTION_BACKUP	= "net.gumbercules.action.BACKUP_DATABASE";
	private static final String TAG				= "net.gumbercules.loot.premium.DbBackupService";

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		CopyThread ct = new CopyThread(CopyThread.BACKUP, CopyThread.OFFLINE, null, getBaseContext());
		ct.start();
		Log.i(TAG + ".onStart", "database backup started at " + System.currentTimeMillis());
	}
}
