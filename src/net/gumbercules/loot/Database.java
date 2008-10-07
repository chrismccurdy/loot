package net.gumbercules.loot;

import android.database.sqlite.*;

public class Database
{
	private final static String DB_NAME		= "LootDB";
	private final static String DB_PATH		= "/data/data/loot/" + DB_NAME + ".db";
	private final static int DB_VERSION		= 1;
	private static SQLiteDatabase lootDB	= null;
	
	public Database()
	{
		// if the database is already set, we don't need to open it again
		if ( lootDB != null )
			return;
		
		try
		{
			lootDB = SQLiteDatabase.openDatabase( DB_PATH, null, SQLiteDatabase.OPEN_READWRITE );
			if ( lootDB.getVersion() < DB_VERSION )
				if ( !this.upgradeDB( DB_VERSION ) )
					lootDB = null;
		}
		// catch SQLiteException if the database doesn't exist, then create it
		catch (SQLiteException sqle)
		{
			try
			{
				lootDB = SQLiteDatabase.openOrCreateDatabase( DB_PATH, null);
				if ( !this.createDB() )
					lootDB = null;
			}
			// something went wrong creating the database
			catch ( SQLiteException e )
			{
				lootDB = null;
			}
		}
	}
	
	private boolean createDB()
	{
		return true;
	}
	
	private boolean upgradeDB( int max_version )
	{
		return true;
	}
	
	public static SQLiteDatabase getDatabase()
	{
		return lootDB;
	}
	
	public static void closeDatabase()
	{
		lootDB.close();
		lootDB = null;
	}
}
