package net.gumbercules.loot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationListener;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TransactionActivity extends ListActivity
{
	public static final String KEY_REQ		= "t_req";
	public static final int ACTIVITY_CREATE	= 0;
	public static final int ACTIVITY_EDIT	= 1;
	public static final int ACTIVITY_DEL	= 2;
	
	public static final String KEY_TYPE		= "t_type";
	public static final int TRANSACTION		= 0;
	public static final int TRANSFER		= 1;
	
	public static final int NEW_TRANSACT_ID	= Menu.FIRST;
	public static final int NEW_TRANSFER_ID	= Menu.FIRST + 1;
	public static final int SORT_ID			= Menu.FIRST + 2;
	public static final int SEARCH_ID			= Menu.FIRST + 3;
	public static final int PURGE_ID		= Menu.FIRST + 4;
	public static final int SETTINGS_ID		= Menu.FIRST + 5;
	
	public static final int CONTEXT_EDIT	= Menu.FIRST;
	public static final int CONTEXT_COPY	= Menu.FIRST + 1;
	public static final int CONTEXT_DEL		= Menu.FIRST + 2;
	
	private ArrayList<Transaction> mTransList;
	private Account mAcct;
	
	private TextView budgetValue;
	private TextView balanceValue;
	private TextView postedValue;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
        
    	Bundle bun = getIntent().getExtras();
    	mAcct = Account.getAccountById(bun.getInt(Account.KEY_ID));
    	setTitle("loot :: " + mAcct.name);
    	
    	budgetValue = (TextView)findViewById(R.id.budgetValue);
    	balanceValue = (TextView)findViewById(R.id.balanceValue);
    	postedValue = (TextView)findViewById(R.id.postedValue);
    	
    	// find current orientation and send proper layout to constructor
    	int layoutResId = R.layout.trans_row_narrow;
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
    		layoutResId = R.layout.trans_row_wide;
    	
    	mTransList = new ArrayList<Transaction>();
	    final TransactionAdapter ta = new TransactionAdapter(this, layoutResId, mTransList);
        setListAdapter(ta);
        fillList();
        
        ListView view = getListView();
        registerForContextMenu(view);
        
    	/*@SuppressWarnings("unused")
		OrientationListener orient = new OrientationListener(this)
    	{
			@Override
			public void onOrientationChanged(int orientation)
			{
				if (orientation >= 270 && orientation < 360)
					ta.setResource(R.layout.trans_row_wide);
				else
					ta.setResource(R.layout.trans_row_narrow);
			}
    	};*/
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(0, NEW_TRANSACT_ID, 0, R.string.new_trans)
    		.setIcon(android.R.drawable.ic_menu_add);
    	menu.add(0, NEW_TRANSFER_ID, 0, R.string.transfer)
    		.setIcon(android.R.drawable.ic_menu_send);
    	menu.add(0, SORT_ID, 0, R.string.sort)
    		.setIcon(android.R.drawable.ic_menu_sort_by_size);
    	menu.add(0, SEARCH_ID, 0, R.string.search)
    		.setIcon(android.R.drawable.ic_menu_search);
    	menu.add(0, PURGE_ID, 0, R.string.purge)
    		.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    	menu.add(0, SETTINGS_ID, 0, R.string.settings)
    		.setIcon(android.R.drawable.ic_menu_preferences);
    	
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
    		
    	case SEARCH_ID:
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
    
    public void setBalances()
    {
    	Double posted = mAcct.calculatePostedBalance();
    	Double balance = mAcct.calculateActualBalance();
    	Double budget = mAcct.calculateBudgetBalance();
    	
		// change the numbers to the locale currency format
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		String str;
		
		if (posted != null)
			str = nf.format(posted);
		else
			str = "Error";
		postedValue.setText(str);
		
		if (balance != null)
			str = nf.format(balance);
		else
			str = "Error";
		balanceValue.setText(nf.format(balance));
		
		if (budget != null)
			str = nf.format(budget);
		else
			str = "Error";
		budgetValue.setText(nf.format(budget));
    }
    
    private void fillList()
    {
		int[] transIds = mAcct.getTransactionIds();
		ArrayList<Transaction> transList = mTransList;
		transList.clear();
		
		if (transIds != null)
			for ( int id : transIds )
				transList.add(Transaction.getTransactionById(id));
		Collections.sort(transList);
		
		setBalances();
    }
    
    private void updateList(int trans_id, int request)
    {
    	TransactionAdapter ta = (TransactionAdapter)getListAdapter();
    	Transaction trans;
    	int pos;
    	
    	switch (request)
    	{
    	case ACTIVITY_EDIT:
    		// don't break, the transaction needs to be added back to the list
    		pos = ((TransactionAdapter)getListAdapter()).findItemById(trans_id);
    		ta.remove(ta.getItem(pos));

    	case ACTIVITY_CREATE:
    		trans = Transaction.getTransactionById(trans_id);
    		ta.add(trans);
    		break;
    		
    	case ACTIVITY_DEL:
    		pos = ((TransactionAdapter)getListAdapter()).findItemById(trans_id);
    		ta.remove(ta.getItem(pos));
    		break;
    	}

		setBalances();
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
			
		case CONTEXT_COPY:
			Transaction tr = Transaction.getTransactionById(id);
			tr.setId(-1);
			id = tr.write(mAcct.id());
			updateList(id, ACTIVITY_CREATE);
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
						updateList(id, ACTIVITY_DEL);
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
		
		if (info == null)
		{
			Log.e(TransactionActivity.class.toString(), "info == null");
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
