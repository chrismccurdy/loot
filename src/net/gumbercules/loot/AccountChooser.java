package net.gumbercules.loot;

import java.util.ArrayList;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountChooser extends ListActivity
{
	public static final int ACTIVITY_CREATE	= 0;
	public static final int ACTIVITY_EDIT	= 1;
	
	public static final int NEW_ACCT_ID		= Menu.FIRST;
	public static final int SETTINGS_ID		= Menu.FIRST + 1;
	
	public static final int CONTEXT_EDIT	= Menu.FIRST + 2;
	public static final int CONTEXT_DEL		= Menu.FIRST + 3;

	private ArrayList<Account> accountList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accounts);

		getListView().setOnCreateContextMenuListener(this);
		
		accountList = new ArrayList<Account>();
		fillList();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Account acct = Account.getAccountById((int)getListAdapter().getItemId(position));
		if (acct == null)
			return;
		
		Intent in = new Intent(this, TransactionActivity.class);
		in.putExtra(Account.KEY_ID, acct.id());
		startActivityForResult(in, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_ACCT_ID, 0, R.string.new_account)
			.setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, SETTINGS_ID, 0, R.string.settings)
			.setIcon(android.R.drawable.ic_menu_preferences);
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
	
	private void editAccount(int id)
	{
		Intent i = new Intent(this, AccountEdit.class);
		i.putExtra(Account.KEY_ID, id);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
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

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info;
		try
		{
			info = (AdapterContextMenuInfo)item.getMenuInfo();
		}
		catch (ClassCastException e)
		{
			Log.e(AccountChooser.class.toString(), "Bad ContextMenuInfo", e);
			return false;
		}
		
		int id = (int)getListAdapter().getItemId(info.position);
		switch (item.getItemId())
		{
		case CONTEXT_EDIT:
			editAccount(id);
			return true;
			
		case CONTEXT_DEL:
			final Account acct = Account.getAccountById(id);
			AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.account_del_box)
				.setMessage("Are you sure you wish to delete " + acct.name + "?")
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						acct.erase();
						fillList();
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) { }
				})
				.create();
			dialog.show();
			
			return true;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		AdapterView.AdapterContextMenuInfo info;
		try
		{
			info = (AdapterContextMenuInfo)menuInfo;
		}
		catch (ClassCastException e)
		{
			Log.e(AccountChooser.class.toString(), "Bad ContextMenuInfo", e);
			return;
		}
		
		Account acct = (Account)getListAdapter().getItem(info.position);
		if (acct == null)
			return;
		
		menu.setHeaderTitle(acct.name);
		
		menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
		menu.add(0, CONTEXT_DEL, 0, R.string.del);
	}
}
