package net.gumbercules.loot;

import java.util.Date;
import java.util.ArrayList;
import android.database.*;
import android.database.sqlite.*;

public class Transaction
{
	public static final int DEPOSIT		= 0;
	public static final int WITHDRAW	= 1;
	public static final int CHECK		= 2;
	
	private int id;
	int account;
	private boolean posted;
	boolean budget;
	int type;				// DEPOSIT, WITHDRAW, CHECK
	int check_num;
	Date date; 
	String party;
	double amount;
	ArrayList<String> tags;
	
	public Transaction( boolean po, boolean b, Date d, int t, String pa, double a, int c )
	{
		this.id = -1;
		this.account = -1;
		this.posted = po;
		this.budget = b;
		this.date = d;
		this.type = t;
		this.amount = a;
		this.check_num = c;
		this.party = pa;
		this.tags = new ArrayList<String>();
	}
	
	public int addTags( String tags )
	{
		int i = 0;
		for ( String tag : this.tagStringToList( tags ) )
			if ( this.tags.add( tag ) )
				++i;
		return i;
	}
	
	public int addTags( String[] tags )
	{
		int i = 0;
		for ( String tag : tags )
		{
			if ( this.tags.add( tag ) )
				++i;
		}
		return i;
	}
	
	public int removeTags( String tags )
	{
		int i = 0;
		for ( String tag : this.tagStringToList( tags ) )
			while ( this.tags.remove(tag) )
				++i;
		return i;
	}
	
	public int removeTags( String[] tags )
	{
		int i = 0;
		for ( String tag : tags )
			// remove all occurrences of tag
			while ( this.tags.remove( tag ) )
				++i;
		return i;
	}
	
	public String tagListToString( String[] tags )
	{
		if ( tags.length == 0 )
			return null;
		
		String tag_str = new String();
		for ( String tag : tags )
			tag_str += tag;
		
		return tag_str;
	}
	
	public String[] tagStringToList( String tags )
	{
		return tags.split(" ");
	}
	
	public int getID()
	{
		return this.id;
	}
	
	public int write( int account_num )
	{
		if (this.id == -1)
			return newTransaction();
		else
			return updateTransaction();
	}
	
	private int newTransaction()
	{
		// insert the new row into the database
		String insert = "insert into transactions (account,date,party,amount,check_num,budget,timestamp) " +
						"values (?,?,?,?,?,?,strftime('%%s','now')";
		
		// invert the amount if it removed money from the account
		double amount = this.amount;
		int check = 0;
		if ( this.type == Transaction.WITHDRAW )
			amount = -amount;
		else if ( this.type == Transaction.CHECK )
		{
			amount = -amount;
			check = this.check_num;
		}
		
		Object[] bindArgs = {new Integer(this.account), this.date, this.party, new Double(amount),
				new Integer(check), new Boolean(this.budget)};
		SQLiteDatabase lootDB = Database.getDatabase();
		try
		{
			lootDB.beginTransaction();
			lootDB.execSQL(insert, bindArgs);
		}
		catch (SQLException e)
		{
			lootDB.endTransaction();
			return -1;
		}
		
		// get the id of that row
		String[] columns = {"max(id)"};
		Cursor cur = lootDB.query("transactions", columns, null, null, null, null, null);
		int id = cur.getInt(0);
		
		// if tag writing is not successful, rollback the changes
		if (writeTags())
		{
			lootDB.setTransactionSuccessful();
			this.id = id;
		}
		lootDB.endTransaction();
		
		Account acct = new Account();
		acct.loadById(Account.getCurrentAccountNum());
		acct.setLastTransactionDate(this.date);
		
		return this.id;
	}
	
	private int updateTransaction()
	{
		String update = "update transactions set account = ?, date = ?, party = ?, amount = ?, " +
						"check_num = ?, budget = ?, timestamp = strftime('%%s','now') where id = ?";
		double amount = this.amount;
		int check = 0;
		if ( this.type == Transaction.WITHDRAW )
		{
			amount = -amount;
		}
		else if ( this.type == Transaction.CHECK )
		{
			amount = -amount;
			check = this.check_num;
		}
		
		int acct_id = this.account;
		if ( this.account == -1 )
			acct_id = Account.getCurrentAccountNum();
		Object[] bindArgs = {new Integer(acct_id), this.date, this.party, new Double(amount),
				new Integer(check), new Boolean(this.budget), this.id};
		
		SQLiteDatabase lootDB = Database.getDatabase();
		try
		{
			lootDB.beginTransaction();
			lootDB.execSQL(update, bindArgs);
		}
		catch (SQLException e)
		{
			lootDB.endTransaction();
			return -1;
		}
		
		// if tag writing is not successful, rollback the changes
		int ret = -1;
		if (eraseTags() && writeTags())
		{
			lootDB.setTransactionSuccessful();
			ret = this.id;
		}
		lootDB.endTransaction();
		
		Account acct = new Account();
		acct.loadById(Account.getCurrentAccountNum());
		acct.setLastTransactionDate(this.date);

		return ret;
	}
	
	private boolean writeTags()
	{
		String insert = "insert into tags (trans_id,name) values (?,?)";
		Object[] bindArgs = new Object[2];
		bindArgs[0] = new Integer(this.id);
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();
		try
		{
			for ( String tag : this.tags )
			{
				bindArgs[1] = tag;
				lootDB.execSQL(insert, bindArgs);
			}
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
	
	private boolean eraseTags()
	{
		String del = "delete from tags where trans_id = ?";
		Object[] bindArgs = {new Integer(this.id)};
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();
		try
		{
			lootDB.execSQL(del, bindArgs);
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
		
	public boolean post( boolean p )
	{
		String post = "update transactions set posted = ?, timestamp = strftime('%%s','now'), " +
					  "budget = 0 where id = ?";
		Object[] bindArgs = {new Boolean(p), new Integer(this.id)};
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();
		try
		{
			lootDB.execSQL(post, bindArgs);
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
		
		Account acct = new Account();
		acct.loadById(Account.getCurrentAccountNum());
		acct.setLastTransactionDate(this.date);

		this.posted = p;
		return true;
	}
	
	public boolean isPosted()
	{
		return posted;
	}
	
	public boolean erase()
	{
		String del = "delete from transactions where id = ?";
		Object[] bindArgs = {new Integer(this.id)};
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();
		try
		{
			lootDB.execSQL(del, bindArgs);
		}
		catch (SQLException e)
		{
			lootDB.endTransaction();
			return false;
		}
		
		if (!eraseTags())
		{
			lootDB.endTransaction();
			return false;
		}
		
		lootDB.setTransactionSuccessful();
		lootDB.endTransaction();
		
		Account acct = new Account();
		acct.loadById(Account.getCurrentAccountNum());
		acct.setLastTransactionDate(this.date);

		return true;
	}
	
	public static String[] getAllTags()
	{
		SQLiteDatabase lootDB;
		try
		{
			lootDB = Database.getDatabase();
		}
		catch ( SQLException e )
		{
			return null;
		}
		
		String[] columns = {"distinct name"};
		Cursor cur = lootDB.query("tags", columns, null, null, null, "name ASC", null);
		ArrayList<String> tags = new ArrayList<String>();
		
		do
		{
			tags.add(cur.getString(0));
		} while (cur.moveToNext());
		
		return (String[])tags.toArray();
	}
	
	public static String[] getAllParties()
	{
		SQLiteDatabase lootDB;
		try
		{
			lootDB = Database.getDatabase();
		}
		catch (SQLException e)
		{
			return null;
		}
		
		String[] columns = {"distinct party"};
		Cursor cur = lootDB.query("transactions", columns, null, null, null, "party ASC", null);
		ArrayList<String> parties = new ArrayList<String>();
		
		do
		{
			parties.add(cur.getString(0));
		} while (cur.moveToNext());
		
		return (String[])parties.toArray();
	}
	
	public void loadTags()
	{
		SQLiteDatabase lootDB = Database.getDatabase();
		String[] columns = {"name"};
		String[] sArgs = {Integer.toString(this.id)};
		Cursor cur = lootDB.query("tags", columns, "trans_id = ?", sArgs, null, null, null);
		
		do
		{
			this.tags.add(cur.getString(0));
		} while (cur.moveToNext());
	}
	
	public static Transaction getTransactionById(int id)
	{
		return null;
	}
}
