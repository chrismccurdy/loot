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
	
	public Transaction()
	{
		this.id = -1;
		this.account = -1;
	}
	
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
	
	@SuppressWarnings("unchecked")
	public Transaction( Transaction trans, boolean copy_id )
	{
		if (copy_id)
			this.id = trans.id;
		else
			this.id = -1;
		this.account = trans.account;
		this.posted = trans.posted;
		this.budget = trans.budget;
		this.date = trans.date;
		this.type = trans.type;
		this.amount = trans.amount;
		this.check_num = trans.check_num;
		this.party = trans.party;
		this.tags = (ArrayList<String>) trans.tags.clone();
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
		
		Object[] bindArgs = {new Integer(this.account), new Long(this.date.getTime()), this.party,
				new Double(amount), new Integer(check), new Boolean(this.budget)};
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
		Object[] bindArgs = {new Integer(acct_id), new Long(this.date.getTime()), this.party,
				new Double(amount), new Integer(check), new Boolean(this.budget), this.id};
		
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
		return getTransactionById(id, false);
	}
	
	public static Transaction getTransactionById(int id, boolean purged)
	{
		String id_query = "id = ?";
		if (!purged)
			id_query += " and purged = 0";
		
		SQLiteDatabase lootDB = Database.getDatabase();
		String[] columns = {"posted", "date", "party", "amount", "check_num", "account", "budget"};
		String[] sArgs = {Integer.toString(id)};
		Cursor cur = lootDB.query("transactions", columns, id_query, sArgs, null, null, null);
		
		double amount = cur.getDouble(3);
		int check = cur.getInt(4);
		int type;
		boolean posted = Database.getBoolean(cur.getInt(0));
		boolean budget = Database.getBoolean(cur.getInt(6));
		Date date = new Date(cur.getLong(1));
		if ( check > 0 )
		{
			type = Transaction.CHECK;
			amount = -amount;
		}
		else
		{
			if ( amount >= 0 )
				type = Transaction.DEPOSIT;
			else
			{
				type = Transaction.WITHDRAW;
				amount = -amount;
			}
		}
		
		Transaction trans = new Transaction(posted, budget, date, type, cur.getString(2), amount, check);
		trans.account = cur.getInt(5);
		trans.id = id;
		trans.loadTags();
		cur.close();
		
		return trans;
	}
	
	public int transfer(Account acct1, Account acct2)
	{
		if ( acct1 == null || acct2 == null )
			return -1;
	
		String detail1, detail2;
		Transaction trans2 = new Transaction(this, false);
		
		if ( this.type == Transaction.DEPOSIT )
		{
			detail1 = "from ";
			detail2 = "to ";
			trans2.type = Transaction.WITHDRAW;
		}
		else
		{
			detail1 = "to ";
			detail2 = "from ";
			trans2.type = Transaction.DEPOSIT;
		}
		
		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();

		// write this transaction to the database
		this.party = "Transfer " + detail1 + acct2.name;
		int ret = this.write(acct1.id());
		
		trans2.party = "Transfer " + detail2 + acct1.name;
		int transfer_id = trans2.write(acct2.id());
		
		// make sure both transfers succeeded and link them together
		if (( ret > -1 && transfer_id > -1 ) && linkTransfer(ret, transfer_id))
		{
			lootDB.setTransactionSuccessful();
		}
		else
		{
			ret = -1;
		}
		
		lootDB.endTransaction();
		
		return ret;
	}
	
	/* possibly unnecessary with the change of write() to write both new and updated transactions
	 * 
	public boolean updateTransfer(Account acct1, Account acct2)
	{
		return true;
	}*/
	
	public boolean eraseTransfer()
	{
		Transaction trans2 = Transaction.getTransactionById(getTransferId());
		boolean purged = false;
		if ( trans2 == null )
		{
			purged = true;
			
		}
		return true;
	}
	
	private boolean linkTransfer(int id1, int id2)
	{
		if (!removeTransfer(Transaction.getTransactionById(id2)))
		{
			return false;
		}
				
		String transfer = "insert into transfers (trans_id1, trans_id2) values (?,?)";
		Object[] bindArgs = {new Integer(id1), new Integer(id2)};

		SQLiteDatabase lootDB = Database.getDatabase();
		lootDB.beginTransaction();

		try
		{
			lootDB.execSQL(transfer, bindArgs);
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
	
	public boolean removeTransfer(Transaction trans)
	{
		String del = "delete from transfers where trans_id1 in (?,?) or trans_id2 in (?,?)";
		Integer id1, id2;
		id1 = new Integer(this.id);
		id2 = new Integer(trans.id);
		Object[] bindArgs = {id1, id2, id1, id2};
		
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
	
	public int getTransferId()
	{
		return -1;
	}
}
