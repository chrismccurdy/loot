package net.gumbercules.loot;

import java.util.Calendar;
import java.util.Date;

import android.database.*;
import android.database.sqlite.*;

public class RepeatSchedule
implements Cloneable
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
	private Date due;	// date of the next repetition
	private int id;		// id of the database repeat_pattern, if available
	
	public RepeatSchedule()
	{
		this.id = -1;
		this.due = null;
		this.start = null;
		this.end = null;
		this.iter = -1;
		this.freq = -1;
		this.custom = -1;
	}
	
	public RepeatSchedule( int it, int fr, int cu, Date st, Date en )
	{
		this.id = -1;
		this.due = null;
		this.iter = it;
		this.freq = fr;
		this.custom = cu;
		this.start = st;
		this.end = en;
	}
	
	protected Object clone()
	throws CloneNotSupportedException
	{
		return (RepeatSchedule)super.clone();
	}
	
	public int write(int trans_id)
	{
		this.due = this.calculateDueDate();
		if (this.due == null || this.start == null)
			return -1;
		
		if (this.id == -1)
			return newRepeat(trans_id);
		else
			return updateRepeat(trans_id);
	}
	
	private int newRepeat(int trans_id)
	{
		long start_time = this.start.getTime(), end_time = 0;
		try
		{
			end_time = this.end.getTime();
		}
		catch (Exception e) { }
		
		String insert = "insert into repeat_pattern (start_date,end_date,iterator,frequency," +
						"custom,due values (" + start_time + "," + end_time + "," + this.iter +
						"," + this.freq + "," + this.custom + "," + this.due.getTime() + ")";
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();
		
		try
		{
			lootDB.execSQL(insert);
		}
		catch (SQLException e)
		{
			lootDB.endTransaction();
			return -1;
		}
		
		String[] columns = {"max(id)"};
		Cursor cur = lootDB.query("repeat_pattern", columns, null, null, null, null, null);
		this.id = cur.getInt(0);
		cur.close();
		
		if (!this.writeTransactionToRepeatTable(trans_id))
		{
			lootDB.endTransaction();
			return -1;
		}
		
		Transaction trans = Transaction.getTransactionById(trans_id);
		int trans_id2 = trans.getTransferId();
		if (trans_id2 > 0)
		{
			try
			{
				RepeatSchedule repeat2 = (RepeatSchedule) this.clone();
				if (repeat2.write(trans_id2) == -1)
				{
					lootDB.endTransaction();
					return -1;
				}
			}
			catch (CloneNotSupportedException e)
			{
				lootDB.endTransaction();
				return -1;
			}
			
		}

		lootDB.setTransactionSuccessful();
		lootDB.endTransaction();
		
		return this.id;
	}
	
	private int updateRepeat(int trans_id)
	{
		if (this.iter == NO_REPEAT)
		{
			this.erase();
			return -1;
		}

		long end_time = 0;
		try
		{
			end_time = this.end.getTime();
		}
		catch (Exception e) { }

		Transaction trans = Transaction.getTransactionById(trans_id);
		int trans_id2 = trans.getTransferId();
		int repeat_id2 = -1;
		if (trans_id2 > 0)
			repeat_id2 = RepeatSchedule.getRepeatId(trans_id2);
		
		String update = "update repeat_pattern set start_date = " + this.start.getTime() +
						", end_date = " + end_time + ", iterator = " + this.iter +
						", frequency = " + this.freq + ", custom = " + this.custom +
						", due = " + this.due.getTime() + " where id = " + this.id;
		
		if (repeat_id2 != -1)
			update += " or id = " + repeat_id2;
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();
		
		try
		{
			lootDB.execSQL(update);
		}
		catch (SQLException e)
		{
			lootDB.endTransaction();
			return -1;
		}
		
		// instead of trying to update the row, delete it and copy over the updated transaction
		if (!this.eraseTransactionFromRepeatTable(true))
		{
			lootDB.endTransaction();
		}
		
		if (trans_id2 != -1)
		{
			RepeatSchedule repeat2 = RepeatSchedule.getSchedule(repeat_id2);
			if (!this.writeTransactionToRepeatTable(trans_id) ||
				!repeat2.writeTransactionToRepeatTable(trans_id2))
			{
				lootDB.endTransaction();
				return -1;
			}
		}
		else
		{
			if (!this.writeTransactionToRepeatTable(trans_id))
			{
				lootDB.endTransaction();
				return -1;
			}
		}
		
		lootDB.setTransactionSuccessful();
		lootDB.endTransaction();
		return this.id;
	}
	
	public boolean erase()
	{
		return false;
	}
	
	private boolean erasePattern(boolean eraseTransfers)
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
	
	public boolean eraseTransactionFromRepeatTable(boolean delete_transfers)
	{
		Transaction trans = Transaction.getTransactionById(this.getTransactionId());
		int repeat_id2 = -1;
		if (delete_transfers)
		{
			int trans_id2 = trans.getTransferId();
			if (trans_id2 != -1)
				repeat_id2 = RepeatSchedule.getRepeatId(trans_id2);
		}
		
		String del = "delete from repeat_transactions where repeat_id = " + this.id;
		if (repeat_id2 != -1)
			del += " or repeat_id = " + repeat_id2;
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();
		
		try
		{
			lootDB.execSQL(del);
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
		if (this.freq == 0 || this.iter == NO_REPEAT || (this.start.after(this.end) && this.end != null))
			return null;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.start);

		if ( this.iter == DAILY )
		{
			cal.add(Calendar.DAY_OF_MONTH, this.freq);
		}
		else if ( this.iter == WEEKLY )
		{
			if (this.custom == -1)
			{
				// standard weekly repetition
				cal.add(Calendar.DAY_OF_MONTH, this.freq * 7);
			}
			else
			{
				// go to next day in the pattern
				int start_day = cal.get(Calendar.DAY_OF_WEEK);
				int first_day = cal.getFirstDayOfWeek();
				int weekday;
				boolean keep_going = true;
				
				while (keep_going)
				{
					cal.add(Calendar.DAY_OF_MONTH, 1);
					weekday = cal.get(Calendar.DAY_OF_WEEK);
					
					if (weekday == start_day)
					{
						keep_going = false;
						break;
					}
					
					// if we reached the end of the week, add this.freq - 1 weeks
					if (weekday == first_day)
						cal.add(Calendar.DAY_OF_MONTH, 7 * (this.freq - 1));
					
					switch (weekday)
					{
						case Calendar.MONDAY:
							if ((this.custom & RepeatSchedule.MONDAY) > 0)
								keep_going = false;
							break;
							
						case Calendar.TUESDAY:
							if ((this.custom & RepeatSchedule.TUESDAY) > 0)
								keep_going = false;
							break;
							
						case Calendar.WEDNESDAY:
							if ((this.custom & RepeatSchedule.WEDNESDAY) > 0)
								keep_going = false;
							break;
							
						case Calendar.THURSDAY:
							if ((this.custom & RepeatSchedule.THURSDAY) > 0)
								keep_going = false;
							break;
							
						case Calendar.FRIDAY:
							if ((this.custom & RepeatSchedule.FRIDAY) > 0)
								keep_going = false;
							break;
							
						case Calendar.SATURDAY:
							if ((this.custom & RepeatSchedule.SATURDAY) > 0)
								keep_going = false;
							break;
							
						case Calendar.SUNDAY:
							if ((this.custom & RepeatSchedule.SUNDAY) > 0)
								keep_going = false;
							break;
						
						default:
							keep_going = false;
							break;
					}
				}
			}
		}
		else if (this.iter == MONTHLY)
		{
			if (this.custom == DATE)
			{
				cal.add(Calendar.MONTH, this.freq);
			}
			else if (this.custom == DAY)
			{
				int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
				int day_of_week_in_month = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
				cal.add(Calendar.MONTH, this.freq);
				cal.set(Calendar.DAY_OF_WEEK, day_of_week);
				cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, day_of_week_in_month);
			}
		}
		else if (this.iter == YEARLY)
		{
			cal.add(Calendar.YEAR, this.freq);
		}
		
		return cal.getTime();
	}
}
