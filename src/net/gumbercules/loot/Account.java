package net.gumbercules.loot;

public class Account
{
	int id;
	String name;
	double initialBalance;
	private static int currentAccount;
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
		Account acct = new Account();
		acct.id = Database.getOptionInt("last_used");
		return null;
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
