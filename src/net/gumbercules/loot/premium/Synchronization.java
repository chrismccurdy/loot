package net.gumbercules.loot.premium;

import net.gumbercules.loot.backend.Database;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Synchronization
{
	public long timestamp;
	public String uuid;
	
	public Synchronization(String uuid)
	{
		this.uuid = uuid;
	}
	
	public Synchronization(String uuid, long ts)
	{
		this.timestamp = ts;
		this.uuid = uuid;
	}
	
	public boolean write()
	{
		String insert = "insert into synchronizations (device_uuid,timestamp) values (?,?)";
		Object[] bindArgs = new Object[] { uuid, new Long(timestamp) };
		boolean ret = true;
		
		SQLiteDatabase lootDB = Database.getDatabase();
		try
		{
			lootDB.beginTransaction();
			lootDB.execSQL(insert, bindArgs);
			lootDB.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			ret = false;
		}
		finally
		{
			lootDB.endTransaction();
		}
		
		return ret;
	}
	
	public void getLatest()
	{
		SQLiteDatabase lootDb = Database.getDatabase();
		Cursor cur = null;
		try
		{
			cur = lootDb.query("synchronizations", new String[] { "timestamp" }, "device_uuid = ?",
					new String[] { uuid }, null, null, "timestamp desc", "1");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (cur != null && cur.moveToFirst())
		{
			timestamp = cur.getLong(0);
		}
		else
		{
			timestamp = -1;
		}
	}
}
