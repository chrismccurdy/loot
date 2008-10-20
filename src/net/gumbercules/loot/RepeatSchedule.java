package net.gumbercules.loot;

import java.util.ArrayList;
import java.util.Date;

import android.database.*;
import android.database.sqlite.*;

public class RepeatSchedule
{
	// repetition iterator type
	public static final int NO_REPEAT	= 0;
	public static final int DAILY		= 1;
	public static final int WEEKLY		= 2;
	public static final int MONTHLY		= 3;
	public static final int YEARLY		= 4;
	public static final int CUSTOM		= 5;
	
	// custom weekly repetition
	public static final int SUNDAY		= 1 << 0;
	public static final int MONDAY		= 1 << 1;
	public static final int TUESDAY		= 1 << 2;
	public static final int WEDNESDAY	= 1 << 3;
	public static final int THURSDAY	= 1 << 4;
	public static final int FRIDAY		= 1 << 5;
	public static final int SATURDAY	= 1 << 6;
	
	// custom monthly repetition
	public static final int DAY			= 0;
	public static final int DATE		= 1;
	
	int iter;			// repetition iterator type
	int freq;			// number between repetitions
	int custom;			// used only for custom types
	Date start;			// start date
	Date end;			// end date
	Date due;			// date of the next repetition
	int id;				// id of the database repeat_pattern, if available
	
	public RepeatSchedule()
	{
		this.id = -1;
	}
	
	public RepeatSchedule( int it, int fr, int cu, Date st, Date en, Date du )
	{
		this.id = -1;
		this.iter = it;
		this.freq = fr;
		this.custom = cu;
		this.start = st;
		this.end = en;
		this.due = du;
	}
	
	public int write()
	{
		return -1;
	}
	
	public boolean erase()
	{
		return false;
	}
	
	private boolean erasePattern(boolean eraseTransfers)
	{
		return false;
	}
	
	private boolean eraseTransaction(boolean eraseTransfers)
	{
		return false;
	}
	
	public boolean load(int repeat_id)
	{
		return false;
	}
	
	public static int getRepeatId(int trans_id)
	{
		return -1;
	}
	
	public static RepeatSchedule getSchedule(int repeat_id)
	{
		return null;
	}
	
	public int getTransactionId()
	{
		return -1;
	}
	
	public Transaction getTransaction()
	{
		return null;
	}
	
	public String[] getTags()
	{
		return null;
	}
	
	public static int[] getRepeatIds()
	{
		return null;
	}
	
	public static int[] getDueRepeatIds(Date date)
	{
		return null;
	}
	
	public int getTransferId()
	{
		return -1;
	}
	
	public static int[] processDueRepetitions(Date date)
	{
		return null;
	}
	
	public int writeTransaction(Date date)
	{
		return -1;
	}
	
	public boolean writeTransactionToRepeatTable(int trans_id)
	{
		Transaction trans = Transaction.getTransactionById(trans_id);
		int transfer_id = trans.getTransferId();
		
		// verify that both the transaction and repeat pattern exist
		String query = "select t.id, r.id from transactions as t, repeat_pattern as r where " +
					   "t.id = " + trans_id + " and r.id = " + this.id;
		SQLiteDatabase lootDB = Database.getDatabase();
		Cursor cur = lootDB.rawQuery(query, null);
		
		// no rows exist with those IDs
		if (cur.getCount() == 0)
		{
			cur.close();
			return false;
		}
		cur.close();
		
		String insert = "insert into repeat_transactions (trans_id,repeat_id,account,date,party," +
						"amount,check_num,transfer_id) select id," + this.id + ",account,date,party," +
						"amount,check_num," + transfer_id + " from transactions where id = " + trans_id;
		lootDB.beginTransaction();
		
		try
		{
			lootDB.execSQL(insert);
		}
		catch (SQLException e)
		{
			lootDB.endTransaction();
			return false;
		}
		
		// copy the tags over to the new row
		insert = "update repeat_transactions set tags = ? where trans_id = " + trans_id +
				 " and repeat_id = " + this.id;
		Object[] bindArgs = {trans.tagListToString()};
		
		try
		{
			lootDB.execSQL(insert, bindArgs);
			lootDB.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			return false;
		}
		finally
		{
			lootDB.endTransaction();
		}
		
		return true;
	}
	
	public Date calculateDueDate()
	{
		return null;
	}
}
