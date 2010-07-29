package net.gumbercules.loot.premium;

import java.io.IOException;

import net.gumbercules.loot.backend.Database;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

public class DbBackupAgent extends BackupAgentHelper
{
	// TODO: extend BackupAgent instead of BackupAgentHelper to ensure timeliness of backup
	private static final String DB_PREFIX = "DB";

	@Override
	public void onCreate()
	{
		addHelper(DB_PREFIX, new FileBackupHelper(this, Database.DB_PATH));
	}

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException
	{
		super.onBackup(oldState, data, newState);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException
	{
		super.onRestore(data, appVersionCode, newState);
	}
	
	
}
