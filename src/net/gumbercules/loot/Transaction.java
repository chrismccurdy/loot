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
		this.posted = false;
		this.budget = false;
		this.date = null;
		this.type = WITHDRAW;
		this.amount = 0.00;
		this.check_num = -1;
		this.party = null;
		this.tags = new ArrayList<String>();
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
	
	public String tagListToString()
	{
		if ( tags.size() == 0 )
			return null;
		
		String tag_str = new String();
		for ( String tag : tags )
			tag_str += tag;
		
		return tag_str;
	}
	
	public String[] getTagList()
	{
		String[] tagList = new String[tags.size()];
		for ( int i = 0; i < tagList.length; ++i )
			tagList[i] = tags.get(i);
		
		return tagList;
	}
	
	private String[] tagStringToList( String tags )
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
	
	public int transfer(Account acct2)
	{
		Account acct1 = Account.getAccountById(this.account);
		if ( acct1 == null || acct2 == null )
			return -1;
	
		String detail1, detail2;
		
		// check to see if we're updating a transfer or creating a new one
		int trans_id2 = getTransferId();
		Transaction trans2, old_trans = null;
		trans2 = new Transaction(this, false);
		boolean purged = false, update = false;
		
		if (trans_id2 > -1)
		{
			trans2.id = trans_id2;
			update = true;
			old_trans = Transaction.getTransactionById(trans_id2, false);
			if (old_trans == null)
			{
				purged = true;
				old_trans = Transaction.getTransactionById(trans_id2, true);
			}
		}

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
		
		// update the initialBalance of the secondary account if the transaction was purged
		if (update && purged)
		{
			double a, b;
			if (trans2.type == Transaction.WITHDRAW)
			{
				a = old_trans.amount;
				b = trans2.amount;
			}
			else
			{
				a = trans2.amount;
				b = old_trans.amount;
			}
			
			acct2.initialBalance += a - b;
			acct2.write();
			
			if (ret > -1 && transfer_id > -1)
			{
				lootDB.setTransactionSuccessful();
			}
		}
		else
		{
			// make sure both transfers succeeded and link them together
			if (( ret > -1 && transfer_id > -1 ) && linkTransfer(ret, transfer_id))
			{
				lootDB.setTransactionSuccessful();
			}
			else
			{
				ret = -1;
			}
		}
			
		lootDB.endTransaction();
		
		return ret;
	}
	
	public boolean eraseTransfer()
	{
		Transaction trans2 = Transaction.getTransactionById(getTransferId(), false);
		boolean purged = false;
		if ( trans2 == null )
		{
			purged = true;
			trans2 = Transaction.getTransactionById(getTransferId(), true);
		}
		if ( trans2 == null )
		{
			// remove the transfer link because the linked transaction no longer exists
			trans2 = new Transaction();
			removeTransfer(trans2);
			return false;
		}
		if ( !this.erase() || !trans2.erase() )
		{
			return false;
		}
		
		// if the second transaction has been purged, update the account balance accordingly
		if (purged)
		{
			Account acct2 = Account.getAccountById(trans2.account);
			double amt = trans2.amount;
			if ( trans2.type == Transaction.WITHDRAW )
			{
				amt = -trans2.amount;
			}
			acct2.initialBalance -= amt;
			acct2.write();
		}
		
		return removeTransfer(trans2);
	}
	
	protected boolean linkTransfer(int id1, int id2)
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
		SQLiteDatabase lootDB = Database.getDatabase();
		String[] columns = {"trans_id1", "trans_id2"};
		String selection = "trans_id1 = " + this.id + " or trans_id2 = " + this.id;
		Cursor cur = lootDB.query("transfers", columns, selection, null, null, null, null);

		int ret = -1;
		if (cur.getCount() > 0)
			ret = cur.getInt(0);
		cur.close();
		return ret;
	}
}
