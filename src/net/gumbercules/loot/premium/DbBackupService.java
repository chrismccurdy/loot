package net.gumbercules.loot.premium;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DbBackupService extends Service
{
	public static final String ACTION_BACKUP = "net.gumbercules.action.BACKUP_DATABASE";

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		Log.i("TEEEEEEEEEEEEEEEEEEEST", "backup service started");
	}
}
