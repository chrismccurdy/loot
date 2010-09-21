package net.gumbercules.loot.premium;

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
		CopyThread ct = new CopyThread(CopyThread.BACKUP, null, getBaseContext());
		ct.start();
		Log.i(TAG + ".onStart", "database backup started at " + System.currentTimeMillis());
	}
}
