package net.gumbercules.loot;

import java.util.ArrayList;
import java.util.Date;

import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

public class Account
{
	public static final String KEY_NAME	= "name";
	public static final String KEY_BAL	= "balance";
	public static final String KEY_ID	= "a_id";
	
	private int id;
	String name;
	double initialBalance;
	private static int currentAccount;
	private double actual_balance;
	private double posted_balance;
	private double budget_balance;
	
	public Account()
	{
		this.id = -1;
	}
	
	public Account(String name, double initialBalance)
	{
		this.id = -1;
		this.name = name;
		this.initialBalance = initialBalance;
	}
	
	public int id()
	{
		return this.id;
	}
	
	public int write()
	{
		if (this.id == -1)
			return newAccount();
		else
			return updateAccount();
	}
	
	private int newAccount()
	{
		// insert the new row into the database
		String insert = "insert into accounts (name,balance,timestamp) values (?,?,strftime('%s','now'))";
		Object[] bindArgs = {this.name, new Double(this.initialBalance)};
		SQLiteDatabase lootDB = Database.getDatabase();
		try
		{
			lootDB.beginTransaction();
			lootDB.execSQL(insert, bindArgs);
			lootDB.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			return -1;
		}
		finally
		{
			lootDB.endTransaction();
		}
		
		// get the id of that row
		Cursor cur = lootDB.rawQuery("select max(id) from accounts", null);
		if (!cur.moveToFirst())
		{
			this.id = -1;
		}
		else
			this.id = cur.getInt(0);
		
		cur.close();
		return this.id;
	}
	
	private int updateAccount()
	{
		// update the row in the database
		String update = "update accounts set name = ?, balance = ?, " +
						"timestamp = strftime('%s','now') where id = ?";
		Object[] bindArgs = {this.name, new Double(this.initialBalance), new Integer(this.id)};
		SQLiteDatabase lootDB = Database.getDatabase();
		try
		{
			lootDB.beginTransaction();
			lootDB.execSQL(update, bindArgs);
			lootDB.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			return -1;
		}
		finally
		{
			lootDB.endTransaction();
		}
		
		return this.id;
	}
	
	public boolean erase()
	{
		// mark the row as 'purged' in the database, so it is still recoverable later
		String del = "update accounts set purged = 1, timestamp = strftime('%s','now') where id = ?";
		Object[] bindArgs = {new Integer(this.id)};
		SQLiteDatabase lootDB = Database.getDatabase();
		try
		{
			lootDB.beginTransaction();
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
	
	private Double calculateBalance(String clause)
	{
		SQLiteDatabase lootDB = Database.getDatabase();
		String[] sArgs = {Integer.toString(this.id)};
		Cursor cur = lootDB.rawQuery("select sum(amount) from transactions where " + clause, sArgs);
		
		Double bal = null;
		if (cur.moveToFirst())
		{
			bal = cur.getDouble(0);
		}
		cur.close();
		
		return bal;
	}
	
	public double getActualBalance()
	{
		return this.actual_balance;
	}
	
	public Double calculateActualBalance()
	{
		Double bal = calculateBalance("account = ? and purged = 0 and budget = 0");
		if (bal != null)
		{
			this.actual_balance = bal + this.initialBalance;
			return this.actual_balance;
		}
		return bal;
	}
	
	public double getPostedBalance()
	{
		return this.posted_balance;
	}
	
	public Double calculatePostedBalance()
	{
		Double bal = calculateBalance("account = ? and posted = 1 and purged = 0");
		if (bal != null)
		{
			this.posted_balance = bal + this.initialBalance;
			return this.posted_balance;
		}
		return bal;
	}
	
	public double getBudgetBalance()
	{
		return this.budget_balance;
	}
	
	public Double calculateBudgetBalance()
	{
		Double bal = calculateBalance("account = ? and purged = 0");
		if (bal != null)
		{
			this.budget_balance = bal + this.initialBalance;
			return this.budget_balance;
		}
		return bal;
	}
	
	public static Account getLastUsedAccount()
	{
		Account acct = new Account();
		acct.loadById( (int)Database.getOptionInt("last_used") );
		return acct;
	}
	
	public boolean loadById(int id)
	{
		SQLiteDatabase lootDB;
		try
		{
			lootDB = Database.getDatabase();
		}
		catch (SQLException e)
		{
			return false;
		}
		
		String[] columns = {"id", "name", "balance"};
		String[] sArgs = {Integer.toString(id)};
		Cursor cur = lootDB.query("accounts", columns, "id = ? and purged = 0", sArgs,
				null, null, null, "1");
		if (!cur.moveToFirst())
		{
			cur.close();
			return false;
		}
		
		this.id = cur.getInt(0);
		this.name = cur.getString(1);
		this.initialBalance = cur.getDouble(2);
		cur.close();
		
		return true;
	}
	
	public boolean setLastUsed()
	{
		return Database.setOption("last_used", this.id);
	}
	
	public static int getCurrentAccountNum()
	{
		return currentAccount;
	}
	
	public void setCurrentAccountNum()
	{
		currentAccount = this.id;
	}
	
	public static String[] getAccountNames()
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
		
		String[] columns = {"name"};
		Cursor cur = lootDB.query("accounts", columns, "purged = 0", null, null, null, null);
		ArrayList<String> accounts = new ArrayList<String>();
		
		if (!cur.moveToFirst())
		{
			cur.close();
			return null;
		}
		
		do
		{
			accounts.add(cur.getString(0));
		} while (cur.moveToNext());
		cur.close();
		
		String[] ret = new String[accounts.size()];
		for (int i = 0; i < ret.length; ++i)
			ret[i] = accounts.get(i);
		
		return ret;
	}
	
	public static int[] getAccountIds()
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
		
		String[] columns = {"id"};
		Cursor cur = lootDB.query("accounts", columns, "purged = 0", null, null, null, null);
		if (!cur.moveToFirst())
		{
			cur.close();
			return null;
		}
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		do
		{
			ids.add(cur.getInt(0));
		} while (cur.moveToNext());
		cur.close();
		
		// convert the Integer ArrayList to int[]
		int[] acc_ids = new int[ids.size()];
		for (int i = 0; i < ids.size(); ++i)
		{
			acc_ids[i] = ids.get(i).intValue();
		}
		
		return acc_ids;
	}
	
	public static Account getAccountByName( String name )
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
		
		String[] columns = {"id", "name", "balance"};
		String[] sArgs = {name};
		Cursor cur = lootDB.query("accounts", columns, "name = ? and purged = 0", sArgs,
				null, null, null, "1");
		if (!cur.moveToFirst())
		{
			cur.close();
			return null;
		}
		
		Account acct = new Account();
		acct.id = cur.getInt(0);
		acct.name = cur.getString(1);
		acct.initialBalance = cur.getDouble(2);
		cur.close();
		
		return acct;
	}
	
	public static Account getAccountById( int id )
	{
		Account acct = new Account();
		acct.loadById(id);
		return acct;
	}

	public int getNextCheckNum()
	{
		SQLiteDatabase lootDB = Database.getDatabase();
		Cursor cur = lootDB.rawQuery("select max(check_num) from transactions where account = " + this.id, null);
		if (!cur.moveToFirst())
		{
			cur.close();
			return -1;
		}
		
		int check_num = cur.getInt(0);
		if (check_num >= 0)
			check_num += 1;
		cur.close();
		
		return check_num;
	}
	
	public boolean setLastTransactionDate( Date d )
	{
		return Database.setOption("last_trans_" + this.id, d.getTime());
	}
	
	public long getLastTransactionDate()
	{
		return Database.getOptionInt("last_trans_" + this.id);
	}
}
