package net.gumbercules.loot.repeat;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;

import net.gumbercules.loot.R;
import net.gumbercules.loot.account.Account;
import net.gumbercules.loot.backend.Database;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RepeatAdapter extends ArrayAdapter<RepeatSchedule>
{
	private DateFormat mDf;
	private static NumberFormat mNf = null;
	private Context mContext;
	private ArrayList<RepeatSchedule> mRepeatList;
	private LayoutInflater mInflater;
	private int mRowResId;
	
	private final static String PATTERN_DAILY		= "Daily";
	private final static String PATTERN_WEEKLY		= "Weekly";
	private final static String PATTERN_BIWEEKLY	= "Bi-Weekly";
	private final static String PATTERN_MONTHLY		= "Monthly";
	private final static String PATTERN_YEARLY		= "Yearly";
	private final static String PATTERN_CUSTOM		= "Custom";
	private final static String PATTERN_NOREPEAT	= "No Repeat";

	public RepeatAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
		mContext = context;
		mRowResId = textViewResourceId;
		mRepeatList = new ArrayList<RepeatSchedule>();
		mDf = DateFormat.getDateInstance();
		mNf = NumberFormat.getCurrencyInstance();
		String new_currency = Database.getOptionString("override_locale");
		if (new_currency != null && !new_currency.equals("") &&
				!new_currency.equals(mNf.getCurrency().getCurrencyCode()))
		{
			mNf.setCurrency(Currency.getInstance(new_currency));
		}
		
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount()
	{
		return mRepeatList.size();
	}

	@Override
	public RepeatSchedule getItem(int position)
	{
		return mRepeatList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return mRepeatList.get(position).id();
	}

	@Override
	public void add(RepeatSchedule object)
	{
		mRepeatList.add(object);
	}

	@Override
	public void insert(RepeatSchedule object, int index)
	{
		mRepeatList.add(index, object);
	}

	@Override
	public void clear()
	{
		mRepeatList.clear();
	}

	public void remove(int index)
	{
		mRepeatList.remove(index);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		
		if (convertView == null)
		{
			convertView = mInflater.inflate(mRowResId, parent, false);
			
			holder = new ViewHolder();
			holder.party = (TextView)convertView.findViewById(R.id.party_text);
			holder.amount = (TextView)convertView.findViewById(R.id.amount_text);
			holder.account = (TextView)convertView.findViewById(R.id.account_text);
			holder.start_date = (TextView)convertView.findViewById(R.id.begin_date_text);
			holder.end_date = (TextView)convertView.findViewById(R.id.end_date_text);
			holder.pattern = (TextView)convertView.findViewById(R.id.repeat_text);
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}

		RepeatSchedule repeat = mRepeatList.get(position);
		if (repeat == null || repeat.trans == null)
		{
			return convertView;
		}
		
		holder.party.setText(repeat.trans.party);
		holder.amount.setText(mNf.format(repeat.trans.amount));
		holder.account.setText(Account.getAccountById(repeat.trans.account).name);
		holder.start_date.setText(mDf.format(repeat.start));
		Date end = repeat.end;
		String end_date = "No End Date";
		if (end != null)
		{
			end_date = mDf.format(repeat.end);
		}
		holder.end_date.setText(end_date);
		
		String pattern = PATTERN_CUSTOM;
		switch (repeat.iter)
		{
			case 0:
				pattern = PATTERN_NOREPEAT;
				break;
				
			case 1:
				if (repeat.freq == 1)
				{
					pattern = PATTERN_DAILY;
				}
				
				break;
				
			case 2:
				if (repeat.freq == 1)
				{
					pattern = PATTERN_WEEKLY;
				}
				else if (repeat.freq == 2)
				{
					pattern = PATTERN_BIWEEKLY;
				}
				break;
				
			case 3:
				if (repeat.freq == 1)
				{
					pattern = PATTERN_MONTHLY;
				}
				break;
				
			case 4:
				if (repeat.freq == 1)
				{
					pattern = PATTERN_YEARLY;
				}
				break;
				
			default:
				pattern = PATTERN_NOREPEAT;
		}
		
		holder.pattern.setText(pattern);

		return convertView;
	}
	
	private static class ViewHolder
	{
		public TextView party;
		public TextView amount;
		public TextView account;
		public TextView start_date;
		public TextView end_date;
		public TextView pattern;
	}
}
