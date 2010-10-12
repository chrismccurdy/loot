package net.gumbercules.loot.premium;

import net.gumbercules.loot.backend.Database;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Synchronization
{
	public long timestamp;
	public String uuid;
	public int account_id;
	
	public Synchronization(String uuid, int account_id)
	{
		this.uuid = uuid;
		this.account_id = account_id;
		this.timestamp = 0;
	}
	
	public Synchronization(String uuid, int account_id, long ts)
	{
		this.timestamp = ts;
		this.account_id = account_id;
		this.uuid = uuid;
	}
	
	public boolean write()
	{
		String insert = "insert into synchronizations (device_uuid,timestamp,account_id) values (?,?,?)";
		Object[] bindArgs = new Object[] { uuid, new Long(timestamp), account_id };
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
			cur = lootDb.query("synchronizations", new String[] { "timestamp" },
					"device_uuid = ? and account_id = ?",
					new String[] { uuid, Integer.toString(account_id) }, null, null, "timestamp desc", "1");
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
