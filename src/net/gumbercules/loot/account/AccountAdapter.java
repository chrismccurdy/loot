/*
 * This file is part of the loot project for Android.
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. This program is distributed in the 
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. You should have received a copy of the GNU General 
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2008, 2009, 2010, 2011 Christopher McCurdy
 */

package net.gumbercules.loot.account;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

import net.gumbercules.loot.R;
import net.gumbercules.loot.backend.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AccountAdapter extends ArrayAdapter<Account>
{
	private ArrayList<Account> mAccountList;
	private int mRowResId;
	private Context mContext;
	private LayoutInflater mInflater;

	public AccountAdapter(Context con, int row, ArrayList<Account> acl)
	{
		super(con, 0);
		mAccountList = acl;
		mRowResId = row;
		mContext = con;
		
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setResource(int resource)
	{
		mRowResId = resource;
	}
	
	@Override
	public int getCount()
	{
		return mAccountList.size();
	}

	@Override
	public Account getItem(int position)
	{
		return mAccountList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return mAccountList.get(position).id();
	}

	@Override
	public void add(Account object)
	{
		mAccountList.add(object);
	}

	@Override
	public void insert(Account object, int index)
	{
		mAccountList.add(index, object);
	}

	public void remove(int index)
	{
		mAccountList.remove(index);
	}
	
	public void setPrimary(int index, boolean set)
	{
		for (int i = 0; i < mAccountList.size(); ++i)
		{
			if (i == index)
			{
				mAccountList.get(i).primary = set;
			}
			else
			{
				mAccountList.get(i).primary = false;
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Account acct = mAccountList.get(position);
		View v = createViewFromResource(convertView, parent, mRowResId);

		// find and retrieve the widgets
		TextView AccountName = (TextView)v.findViewById(R.id.AccountName);
		TextView AccountBal = (TextView)v.findViewById(R.id.AccountBalance);
		View star = (View)v.findViewById(R.id.star_image);
		
		if (AccountName != null)
		{
			AccountName.setText(acct.name);
		}
		if (AccountBal != null)
		{
			NumberFormat nf = NumberFormat.getCurrencyInstance();
			String new_currency = Database.getOptionString("override_locale");
			if (new_currency != null && !new_currency.equals(""))
			{
				nf.setCurrency(Currency.getInstance(new_currency));
			}
			
			Double bal = null;
			
			if (acct.balanceDisplay == 0)	// actual
			{
				bal = acct.getActualBalance();
			}
			else if (acct.balanceDisplay == 1)	// posted
			{
				bal = acct.getPostedBalance();
			}
			else if (acct.balanceDisplay == 2)	// budget
			{
				bal = acct.getBudgetBalance();
			}
			
			String text;
			if (bal != null)
			{
				text = nf.format(bal);
			}
			else
			{
				text = "Error Calculating Balance";
			}
			AccountBal.setText(text);
			
			int textColor = Color.LTGRAY;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			if (prefs.getBoolean("color_balance", false))
			{
				final int red = Color.rgb(255, 50, 50);
				if (acct.credit)
				{
					if (bal > acct.creditLimit)
					{
						textColor = red;
					}
					else if (bal >= (acct.creditLimit * 0.9))
					{
						textColor = Color.YELLOW;
					}
				}
				else
				{
					if (bal < 0.0)
					{
						textColor = red;
					}
				}
			}

			AccountBal.setTextColor(textColor);
		}
		
		int visibility = View.GONE;
		if (acct.isPrimary())
		{
			visibility = View.VISIBLE;
		}
		star.setVisibility(visibility);

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
