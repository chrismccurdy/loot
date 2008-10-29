package net.gumbercules.loot;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class AccountChooser extends ListActivity
{
	public static final int ACTIVITY_CREATE	= 0;
	public static final int ACTIVITY_EDIT	= 1;
	
	public static final int NEW_ACCT_ID	= Menu.FIRST;

	private ArrayList<Account> accountList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accounts);
		
		accountList = new ArrayList<Account>();
		fillList();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_ACCT_ID, 0, R.string.new_account);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	switch (item.getItemId())
    	{
    	case NEW_ACCT_ID:
    		createAccount();
    		return true;
    	}
    	
		return super.onOptionsItemSelected(item);
	}
	
	private void createAccount()
	{
		Intent i = new Intent(this, AccountEdit.class);
    	startActivityForResult(i, ACTIVITY_CREATE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	
		if (resultCode == RESULT_OK)
			fillList();
	}
	
	private void fillList()
	{
		int[] acctIds = Account.getAccountIds();
		accountList.clear();
		
		if (acctIds != null)
			for ( int id : acctIds )
				accountList.add(Account.getAccountById(id));
			
		AccountAdapter accounts = new AccountAdapter(this, R.layout.account_row, accountList);
		setListAdapter(accounts);
	}
}
