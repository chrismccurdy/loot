package net.gumbercules.loot;

import java.util.ArrayList;
import java.util.Collections;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class Loot extends ListActivity
{
	public static final int ACTIVITY_CREATE				= 0;
	public static final int ACTIVITY_TRANSFER_CREATE	= 1;
	public static final int ACTIVITY_EDIT				= 2;
	
	public static final int NEW_TRANSACT_ID		= Menu.FIRST;
	public static final int NEW_TRANSFER_ID		= Menu.FIRST + 1;
	public static final int SORT_ID				= Menu.FIRST + 2;
	public static final int SETTINGS_ID			= Menu.FIRST + 3;
	private ArrayList<Transaction> transList;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
        
    	transList = new ArrayList<Transaction>();
    	/************* TESTING ****************/
    	Transaction t;
        java.util.Date date = new java.util.Date();
        for (int i=0; i<10;++i)
        {
	        t = new Transaction(false, false, date, Transaction.CHECK, "Test 1", -5.25, 1001);
	        transList.add(t);
	        t = new Transaction(false, false, date, Transaction.DEPOSIT, "Test 2", 25.20, 1001);
	        transList.add(t);
	        t = new Transaction(true, false, date, Transaction.WITHDRAW, "Test 3", -15.00, 1001);
	        transList.add(t);
        }
        Collections.sort(transList);
        /************ END TESTING *************/
        
	    TransactionAdapter ta = new TransactionAdapter(this, R.layout.trans_row_narrow, transList);
        this.setListAdapter(ta);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(0, NEW_TRANSACT_ID, 0, R.string.new_trans);
    	menu.add(0, NEW_TRANSFER_ID, 0, R.string.transfer);
    	menu.add(0, SORT_ID, 0, R.string.sort);
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
    		
    	case SETTINGS_ID:
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    public void createTransaction()
    {
    	Intent i = new Intent(this, TransactionEdit.class);
    	int request = ACTIVITY_CREATE;
    	i.putExtra("REQUEST", request);
    	startActivityForResult(i, request);    	
    }
    
    public void createTransfer()
    {
    	Intent i = new Intent(this, TransactionEdit.class);
    	int request = ACTIVITY_TRANSFER_CREATE;
    	i.putExtra("REQUEST", request);
    	startActivityForResult(i, request);
    }
}
