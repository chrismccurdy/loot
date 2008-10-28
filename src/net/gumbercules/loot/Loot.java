package net.gumbercules.loot;

import java.util.ArrayList;
import java.util.Collections;
import android.app.ListActivity;
import android.os.Bundle;

public class Loot extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ArrayList<Transaction> transList = new ArrayList<Transaction>();
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
	    TransactionAdapter ta = new TransactionAdapter(this, R.layout.trans_row_narrow, transList);
        this.setListAdapter(ta);
    }
}
