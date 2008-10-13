package net.gumbercules.loot;

import java.util.Date;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;

public class Transaction
{
	public static final int DEPOSIT		= 0;
	public static final int WITHDRAW	= 1;
	public static final int CHECK		= 2;
	
	int id;
	int account;
	boolean posted;
	boolean budget;
	int type;				// DEPOSIT, WITHDRAW, CHECK
	int check_num;
	Date date; 
	String party;
	double amount;
	ArrayList<String> tags;
	
	public Transaction( boolean po, boolean b, Date d, int t, String pa, double a, int c )
	{
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
	
	public int write( int account_num )
	{
		return -1;
	}
		
	public boolean post( boolean p )
	{
		this.posted = p;
		
		return true;
	}
	
	public boolean erase()
	{
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
	
	public boolean getTags()
	{
		return true;
	}
}
