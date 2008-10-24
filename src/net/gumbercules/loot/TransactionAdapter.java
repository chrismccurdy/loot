package net.gumbercules.loot;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class TransactionAdapter extends BaseAdapter
{
	private ArrayList<Transaction> transList;
	private int rowResId;

	public TransactionAdapter(ArrayList<Transaction> tr, int row)
	{
		this.transList = tr;
		this.rowResId = row;
	}

	public int getCount()
	{
		return transList.size();
	}

	public Object getItem(int position)
	{
		return transList.get(position);
	}

	public long getItemId(int position)
	{
		return transList.get(position).getID();
	}

	public void setResource(int row)
	{
		this.rowResId = row;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		Transaction trans = transList.get(position);
		View v = (View)parent.findViewById(rowResId);

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

		if (idText != null)
			idText.setText(trans.getID());
		if (postedCheck != null)
			postedCheck.setChecked(trans.isPosted());
		if (dateText != null)
			dateText.setText(trans.date.toString());
		if (partyText != null)
			partyText.setText(partyStr);
		if (amountText != null)
			amountText.setText(Double.toString(trans.amount));
		/* TODO: set balance text
		if (balanceText != null)
			balanceText.setText("");
		*/

		return v;
	}

}
