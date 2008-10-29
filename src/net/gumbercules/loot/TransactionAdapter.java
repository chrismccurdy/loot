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
		return transList.get(position).getID();
	}

	public void setResource(int row)
	{
		this.rowResId = row;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Transaction trans = transList.get(position);
		View v = createViewFromResource(convertView, parent, rowResId);

		// find and retrieve the widgets
		TextView idText = (TextView)v.findViewById(R.id.IdText);
		CheckBox postedCheck = (CheckBox)v.findViewById(R.id.PostedCheckBox);
		TextView dateText = (TextView)v.findViewById(R.id.DateText);
		TextView partyText = (TextView)v.findViewById(R.id.PartyText);
		TextView amountText = (TextView)v.findViewById(R.id.AmountText);
		TextView balanceText = (TextView)v.findViewById(R.id.BalanceText);

		// populate the widgets with data
		String partyStr = "";
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
		
		// if we're in a portrait view, add the date below the party
		if (rowResId == R.layout.trans_row_narrow)
			partyStr += "\n" + dateStr;

		if (idText != null)
			idText.setText(Integer.toString(trans.getID()));
		if (postedCheck != null)
			postedCheck.setChecked(trans.isPosted());
		if (dateText != null)
			dateText.setText(dateStr);
		if (partyText != null)
			partyText.setText(partyStr);
		if (amountText != null)
			amountText.setText(amountStr);
		/* TODO: set balance text
		if (balanceText != null)
			balanceText.setText("");
		*/

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
