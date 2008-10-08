package net.gumbercules.loot;

public class Account
{
	int id;
	String name;
	double initialBalance;
	private double actual_balance;
	private double posted_balance;
	private double budget_balance;
	
	public Account()
	{
		
	}
	
	public int write()
	{
		return -1;
	}
	
	public boolean update()
	{
		return true;
	}
	
	public boolean erase()
	{
		return true;
	}
	
	public double getActualBalance()
	{
		return this.actual_balance;
	}
	
	public double getPostedBalance()
	{
		return this.posted_balance;
	}
	
	public double getBudgetBalance()
	{
		return this.budget_balance;
	}
	
	public static Account getLastUsedAccount()
	{
		return null;
	}
	
	public boolean setLastUsed()
	{
		return true;
	}
	
	public static String[] getAccountNames()
	{
		return null;
	}
	
	public static int[] getAccountIds()
	{
		return null;
	}
	
	public static Account getAccountByName( String name )
	{
		return null;
	}
}
