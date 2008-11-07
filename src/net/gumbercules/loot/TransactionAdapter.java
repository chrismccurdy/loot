package net.gumbercules.loot;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class TransactionAdapter extends ArrayAdapter<Transaction>
{
	private ArrayList<Transaction> transList;
	private int rowResId;
	private Context context;
	private LayoutInflater mInflater;

	public TransactionAdapter(Context con, int row, ArrayList<Transaction> tr)
	{
		super(con, 0);
		this.transList = tr;
		this.rowResId = row;
		this.context = con;
		
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount()
	{
		return transList.size();
	}

	@Override
	public Transaction getItem(int position)
	{
		return transList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return transList.get(position).id();
	}
	
	public int findItemById(int id)
	{
		for (int i = 0; i < transList.size(); ++i)
		{
			if (transList.get(i).id() == id)
				return i;
		}
		
		return -1;
	}

	public void setResource(int row)
	{
		this.rowResId = row;
	}
	
	public void setList(ArrayList<Transaction> trans)
	{
		transList = trans;
	}
	
	public ArrayList<Transaction> getList()
	{
		return transList;
	}

	@Override
	public void add(Transaction object)
	{
		transList.add(object);
		Collections.sort(transList);
		notifyDataSetChanged();
	}

	@Override
	public void insert(Transaction object, int index)
	{
		transList.add(index, object);
		notifyDataSetChanged();
	}

	@Override
	public void remove(Transaction object)
	{
		transList.remove(object);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final Transaction trans = transList.get(position);
		if (trans == null)
			return null;
		
		View v = createViewFromResource(convertView, parent, rowResId);

		// find and retrieve the widgets
		CheckBox postedCheck = (CheckBox)v.findViewById(R.id.PostedCheckBox);
		TextView dateText = (TextView)v.findViewById(R.id.DateText);
		TextView partyText = (TextView)v.findViewById(R.id.PartyText);
		TextView amountText = (TextView)v.findViewById(R.id.AmountText);
		
		postedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (trans.isPosted() != isChecked)
				{
					trans.post(isChecked);
					TransactionActivity ta = (TransactionActivity) buttonView.getContext();
					ta.setBalances();
				}
			}
		});
		
		// populate the widgets with data
		String partyStr = "";
		if (trans.budget)
			partyStr += "B:";
		if (trans.type == Transaction.CHECK)
			partyStr += trans.check_num;
		else if (trans.type == Transaction.WITHDRAW)
			partyStr += "W";
		else
			partyStr += "D";
		partyStr += ":" + trans.party;
		
		// change the date to the locale date format
		DateFormat df = DateFormat.getDateInstance();
		String dateStr = df.format(trans.date);
		
		// change the numbers to the locale currency format
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		String amountStr = nf.format(Math.abs(trans.amount));
		
		if (postedCheck != null)
			postedCheck.setChecked(trans.isPosted());
		if (dateText != null)
			dateText.setText(dateStr);
		if (partyText != null)
			partyText.setText(partyStr);
		if (amountText != null)
			amountText.setText(amountStr);
		
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
