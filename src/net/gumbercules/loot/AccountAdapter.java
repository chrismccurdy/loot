package net.gumbercules.loot;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AccountAdapter extends ArrayAdapter<Account>
{
	private ArrayList<Account> accountList;
	private int rowResId;
	private Context context;
	private LayoutInflater mInflater;

	public AccountAdapter(Context con, int row, ArrayList<Account> acl)
	{
		super(con, 0);
		this.accountList = acl;
		this.rowResId = row;
		this.context = con;
		
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount()
	{
		return accountList.size();
	}

	@Override
	public Account getItem(int position)
	{
		return accountList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return accountList.get(position).id();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Account acct = accountList.get(position);
		View v = createViewFromResource(convertView, parent, rowResId);

		// find and retrieve the widgets
		TextView AccountName = (TextView)v.findViewById(R.id.AccountName);
		TextView AccountBal = (TextView)v.findViewById(R.id.AccountBalance);
		
		if (AccountName != null)
			AccountName.setText(acct.name);
		if (AccountBal != null)
		{
			NumberFormat nf = NumberFormat.getCurrencyInstance();
			Double bal = acct.calculateActualBalance();
			String text;
			if (bal != null)
				text = nf.format(bal);
			else
				text = "Error Calculating Balance";
			AccountBal.setText(text);
		}

		return v;
	}

	private View createViewFromResource(View convertView, ViewGroup parent, int resource)
	{
		View view;
		
		if (convertView == null)
			view = mInflater.inflate(resource, parent, false);
		else
			view = convertView;
	
		return view;
	}
}
