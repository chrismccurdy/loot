package net.gumbercules.loot;

import java.util.Date;
import java.util.ArrayList;

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
	
	public boolean update( int account_num )
	{
		return true;
	}
	
	public boolean post( boolean p )
	{
		return true;
	}
	
	public boolean erase()
	{
		return true;
	}
	
	public static String[] getAllTags()
	{
		return null;
	}
	
	public static String[] getAllParties()
	{
		return null;
	}
	
	public boolean getTags()
	{
		return true;
	}
}
