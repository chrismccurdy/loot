package net.gumbercules.loot;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class TransactionAdapter extends ArrayAdapter<Transaction> implements Filterable
{
	private ArrayList<Transaction> transList;
	private ArrayList<Transaction> mOriginalList;
	private int rowResId;
	private Context context;
	private LayoutInflater mInflater;
	private CharSequence mConstraint;
	private int mAcctId;

	public TransactionAdapter(Context con, int row, ArrayList<Transaction> tr, int acct_id)
	{
		super(con, 0);
		this.transList = tr;
		this.mOriginalList = tr;
		this.rowResId = row;
		this.context = con;
		this.mAcctId = acct_id;
		
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
		mOriginalList = trans;
		new TransactionFilter().filter(mConstraint);
		notifyDataSetChanged();
	}
	
	public ArrayList<Transaction> getList()
	{
		return transList;
	}
	
	public void sort()
	{
		Collections.sort(mOriginalList);
		new TransactionFilter().filter(mConstraint);
		notifyDataSetChanged();
	}

	@Override
	public void add(Transaction object)
	{
		if (object.account == mAcctId)
		{
			mOriginalList.add(object);
			notifyDataSetChanged();
		}
	}
	
	public void add(int[] ids)
	{
		if (ids == null)
			return;
		
		Transaction trans;
		for (int id : ids)
		{
			trans = Transaction.getTransactionById(id);
			if (trans != null && trans.account == mAcctId)
				mOriginalList.add(trans);
		}
		notifyDataSetChanged();
	}

	@Override
	public void insert(Transaction object, int index)
	{
		if (object.account == mAcctId)
		{
			mOriginalList.add(index, object);
			notifyDataSetChanged();
		}
	}

	@Override
	public void remove(Transaction object)
	{
		mOriginalList.remove(object);
		new TransactionFilter().filter(mConstraint);
		notifyDataSetChanged();
	}
	
	public void remove(int[] ids)
	{
		for (int id : ids)
			mOriginalList.remove(getItem(findItemById(id)));
		new TransactionFilter().filter(mConstraint);
		notifyDataSetChanged();
	}
	
	@Override
	public void clear()
	{
		mOriginalList.clear();
		new TransactionFilter().filter(mConstraint);
		notifyDataSetChanged();
	}
	
	@Override
	public Filter getFilter()
	{
		TransactionFilter filter = new TransactionFilter();
		return (Filter)filter;
	}
	
	private View emptyView()
	{
		View v = new View(context);
		v.setVisibility(View.GONE);
		return v;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final Transaction trans = transList.get(position);
		if (trans == null || trans.account == 0)
		{
			Log.e("GET_VIEW", "trans is null");
			return emptyView();
		}

		ViewHolder holder;
		
		if (convertView == null)
		{
			convertView = mInflater.inflate(rowResId, parent, false);
			
			holder = new ViewHolder();
			holder.check = (CheckBox)convertView.findViewById(R.id.PostedCheckBox);
			holder.date = (TextView)convertView.findViewById(R.id.DateText);
			holder.party = (TextView)convertView.findViewById(R.id.PartyText);
			holder.amount = (TextView)convertView.findViewById(R.id.AmountText);
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}

		// find and retrieve the widgets
		CheckBox postedCheck = holder.check;
		TextView dateText = holder.date;
		TextView partyText = holder.party;
		TextView amountText = holder.amount;
		
		if (postedCheck == null)
		{
			Log.e("GET_VIEW", "postedCheck is null");
			return emptyView();
		}
		
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
		
		return convertView;
	}
	
	static class ViewHolder
	{
		CheckBox check;
		TextView date;
		TextView party;
		TextView amount;
	}

	public class TransactionFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence constraint)
		{
			mConstraint = constraint;
			FilterResults results = new FilterResults();
			
			// do the filtering to decide which items we show
			if (constraint == null || constraint.length() == 0)
			{
				ArrayList<Transaction> list = new ArrayList<Transaction>(mOriginalList);
				results.count = list.size();
				results.values = list;
				return results;
			}
			
			boolean matches = false;
			ArrayList<Transaction> tList = new ArrayList<Transaction>(mOriginalList);
			ArrayList<Transaction> values = new ArrayList<Transaction>();
			results.count = 0;
			results.values = values;
			String[] filters = constraint.toString().split(" ");
			
			for (Transaction trans : tList)
			{
				// check for at least one match
				for (String filter : filters)
				{
					filter = "(?i).*" + filter + ".*";
					if (trans.party.matches(filter))
					{
						matches = true;
						break;
					}
					for (String tag : trans.tags)
					{
						if (tag.matches(filter))
						{
							matches = true;
							break;
						}
					}
					if (matches)
						break;
				}
				
				if (matches)
				{
					values.add(trans);
				}
				
				matches = false;
			}
			
			results.values = (Object)values;
			results.count = values.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results)
		{
			transList = (ArrayList<Transaction>)results.values;
			notifyDataSetChanged();
		}
	}
}
