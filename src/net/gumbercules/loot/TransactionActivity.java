package net.gumbercules.loot;

import java.util.ArrayList;
import java.util.Collections;

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
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TransactionActivity extends ListActivity
{
	public static final String KEY_REQ		= "t_req";
	public static final int ACTIVITY_CREATE	= 0;
	public static final int ACTIVITY_EDIT	= 1;
	
	public static final String KEY_TYPE		= "t_type";
	public static final int TRANSACTION		= 0;
	public static final int TRANSFER		= 1;
	
	public static final int NEW_TRANSACT_ID	= Menu.FIRST;
	public static final int NEW_TRANSFER_ID	= Menu.FIRST + 1;
	public static final int SORT_ID			= Menu.FIRST + 2;
	public static final int GOTO_ID			= Menu.FIRST + 3;
	public static final int PURGE_ID		= Menu.FIRST + 4;
	public static final int SETTINGS_ID		= Menu.FIRST + 5;
	
	public static final int NEW_ID			= Menu.FIRST;
	public static final int EDIT_ID			= Menu.FIRST + 1;
	public static final int DEL_ID			= Menu.FIRST + 2;
	
	public static final int CONTEXT_EDIT	= Menu.FIRST;
	public static final int CONTEXT_COPY	= Menu.FIRST + 1;
	public static final int CONTEXT_DEL		= Menu.FIRST + 2;
	
	private ArrayList<Transaction> mTransList;
	private Account mAcct;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
        
    	Bundle bun = getIntent().getExtras();
    	mAcct = Account.getAccountById(bun.getInt(Account.KEY_ID));
    	setTitle("loot - " + mAcct.name);
    	
    	// TODO: find current orientation and send proper layout to constructor
    	mTransList = new ArrayList<Transaction>();
	    TransactionAdapter ta = new TransactionAdapter(this, R.layout.trans_row_narrow, mTransList);
        setListAdapter(ta);
        fillList();
    	
        /************* TESTING ****************
    	Transaction t;
        java.util.Date date = new java.util.Date();
        for (int i=0; i<10;++i)
        {
	        t = new Transaction(false, false, date, Transaction.CHECK, "Test 1", -5.25, 1001);
	        mTransList.add(t);
	        t = new Transaction(false, false, date, Transaction.DEPOSIT, "Test 2", 25.20, 1001);
	        mTransList.add(t);
	        t = new Transaction(true, false, date, Transaction.WITHDRAW, "Test 3", -15.00, 1001);
	        mTransList.add(t);
        }
        Collections.sort(mTransList);
        /************ END TESTING *************/
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(0, NEW_TRANSACT_ID, 0, R.string.new_trans);
    	menu.add(0, NEW_TRANSFER_ID, 0, R.string.transfer);
    	menu.add(0, SORT_ID, 0, R.string.sort);
    	menu.add(0, GOTO_ID, 0, R.string.goto_);
    	menu.add(0, PURGE_ID, 0, R.string.purge);
    	menu.add(0, SETTINGS_ID, 0, R.string.settings);
    	
    	return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    	case NEW_TRANSACT_ID:
    		createTransaction();
    		return true;
    		
    	case NEW_TRANSFER_ID:
    		createTransfer();
    		return true;
    		
    	case SORT_ID:
    		return true;
    		
    	case GOTO_ID:
    		return true;
    		
    	case PURGE_ID:
    		return true;
    		
    	case SETTINGS_ID:
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    public void createTransaction()
    {
    	Intent i = new Intent(this, TransactionEdit.class);
    	int request = ACTIVITY_CREATE;
    	i.putExtra(TransactionActivity.KEY_REQ, request);
    	i.putExtra(TransactionActivity.KEY_TYPE, TRANSACTION);
    	i.putExtra(Account.KEY_ID, mAcct.id());
    	startActivityForResult(i, request);    	
    }
    
    public void createTransfer()
    {
    	Intent i = new Intent(this, TransactionEdit.class);
    	int request = ACTIVITY_CREATE;
    	i.putExtra(TransactionActivity.KEY_REQ, request);
    	i.putExtra(TransactionActivity.KEY_TYPE, TRANSFER);
    	i.putExtra(Account.KEY_ID, mAcct.id());
    	startActivityForResult(i, request);
    }
    
    public void editTransaction(int id)
    {
    	Intent i = new Intent(this, TransactionEdit.class);
    	i.putExtra(Transaction.KEY_ID, id);
    	startActivityForResult(i, ACTIVITY_EDIT);
    }
    
    private void fillList()
    {
    	// TODO: fix this method to add new items
		int[] transIds = Transaction.getAllIds();
		ArrayList<Transaction> transList = mTransList;
		transList.clear();
		
		if (transIds != null)
			for ( int id : transIds )
				transList.add(Transaction.getTransactionById(id));
		Collections.sort(transList);
    }
    
    private void updateList(int trans_id, int request)
    {
    	ArrayList<Transaction> transList = mTransList;
    	Transaction trans;
    	int pos;
    	
    	switch (request)
    	{
    	case EDIT_ID:
    		// don't break, the transaction needs to be added back to the list
    		pos = ((TransactionAdapter)getListAdapter()).findItemById(trans_id);
    		transList.remove(pos);

    	case NEW_ID:
    		trans = Transaction.getTransactionById(trans_id);
    		transList.add(trans);
    		break;
    		
    	case DEL_ID:
    		pos = ((TransactionAdapter)getListAdapter()).findItemById(trans_id);
    		transList.remove(pos);
    		break;
    	}
    	
    	Collections.sort(transList);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK && data != null)
		{
			Bundle extras = data.getExtras();
			updateList(extras.getInt(Transaction.KEY_ID), extras.getInt(TransactionActivity.KEY_REQ));
		}
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
			Log.e(TransactionActivity.class.toString(), "Bad ContextMenuInfo", e);
			return false;
		}
		
		int id = (int)getListAdapter().getItemId(info.position);
		switch (item.getItemId())
		{
		case CONTEXT_EDIT:
			editTransaction(id);
			return true;
			
		case CONTEXT_DEL:
			final Transaction trans = Transaction.getTransactionById(id);
			AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.account_del_box)
				.setMessage("Are you sure you wish to delete " + trans.party + "?")
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						int id = trans.id();
						trans.erase();
						updateList(id, DEL_ID);
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
			Log.e(TransactionActivity.class.toString(), "Bad ContextMenuInfo", e);
			return;
		}
		
		Transaction trans = (Transaction)getListAdapter().getItem(info.position);
		if (trans == null)
			return;
		
		menu.setHeaderTitle(trans.party);
		
		menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
		menu.add(0, CONTEXT_COPY, 0, R.string.copy);
		menu.add(0, CONTEXT_DEL, 0, R.string.del);
	}
}
