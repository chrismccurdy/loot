package net.gumbercules.loot.premium.backup;

import java.io.IOException;

import net.gumbercules.loot.backend.CopyThread;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

public class DbBackupAgent extends BackupAgentHelper
{
	private static final String DB_PREFIX = "DB";

	@Override
	public void onCreate()
	{
		addHelper(DB_PREFIX, new FileBackupHelper(this, CopyThread.TEMP_BACKUP));
	}

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException
	{
		synchronized (CopyThread.sDataLock)
		{
			super.onBackup(oldState, data, newState);
		}
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException
	{
		synchronized (CopyThread.sDataLock)
		{
			super.onRestore(data, appVersionCode, newState);
		}
	}
}
