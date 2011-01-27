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

package net.gumbercules.loot.premium.synchronization;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class DataInterface
{
	// TODO try serializing transactions with google's Protocol Buffers to see if it increases speed
	// TODO and/or check out the Parcelable interface, advertised for high-speed IPC transports
	
	private ArrayList<Account> mAccounts;
	private ArrayList<Integer> mAllowedAccountIds;
	private ArrayList<Transaction> mTransactions;
	private String mRemoteUuid;
	private ContentResolver mResolver;
	// private long mTimestamp;
	// TODO this should be replaced with a HashMap of the allowed accounts and the latest timestamp for each
	
	public DataInterface(ContentResolver cr)
	{
		mResolver = cr;
	}
	
	@SuppressWarnings("unchecked")
	public void setAllowedAccounts(ArrayList<Integer> ids)
	{
		mAllowedAccountIds = (ArrayList<Integer>) ids.clone();
	}
	
	public void setRemoteUuid(String uuid)
	{
		mRemoteUuid = uuid;
	}
	
	public ArrayList<Account> getNextAccounts(int chunk_size)
	{
		if (mAccounts == null)
		{
			loadAccounts();
		}
		
		if (mAccounts == null)
		{
			return null;
		}
		
		if (chunk_size > mAccounts.size())
		{
			chunk_size = mAccounts.size();
		}
		
		ArrayList<Account> ret = new ArrayList<Account>();
		
		for (int i = 0; i < chunk_size; ++i)
		{
			ret.add(mAccounts.get(i));
		}
		
		mAccounts.subList(0, chunk_size).clear();
		
		return ret;
	}
	
	public ArrayList<Transaction> getNextTransactions(int chunk_size)
	{
		if (mTransactions == null)
		{
			loadTransactions();
		}
		
		if (mTransactions == null)
		{
			return null;
		}
		
		if (chunk_size > mTransactions.size())
		{
			chunk_size = mTransactions.size();
		}
		
		ArrayList<Transaction> ret = new ArrayList<Transaction>();
		
		for (int i = 0; i < chunk_size; ++i)
		{
			ret.add(mTransactions.get(i));
		}
		
		mTransactions.removeAll(mTransactions.subList(0, chunk_size));
		
		return ret;
	}
	
	public String accountListToJson(ArrayList<Account> accounts)
			throws JSONException, NoMoreItemsException
	{
		return Account.toJson(accounts);
	}
	
	public String transactionListToJson(ArrayList<Transaction> transactions)
			throws JSONException, NoMoreItemsException
	{
		return Transaction.toJson(transactions);
	}
	
	private void loadAccounts()
	{
		Cursor cur = mResolver.query(Uri.parse("content://net.gumbercules.loot.accountprovider"),
				new String[] { Account.NAME_ID, Account.NAME_NAME, Account.NAME_INIT_BALANCE,
						Account.NAME_CREDIT, Account.NAME_CREDIT_LIMIT, Account.NAME_TIMESTAMP },
				null, null, null);
		
		if (cur == null || !cur.moveToFirst())
		{
			return;
		}
		
		mAccounts = new ArrayList<Account>();
		Account acct = null;
		int id;
		do
		{
			id = cur.getInt(0);
			
			// only add accounts that are permitted to be shared
			if (!mAllowedAccountIds.contains(id))
			{
				continue;
			}
			
			acct = new Account();
			acct.id = cur.getInt(0);
			acct.name = cur.getString(1);
			acct.initial_balance = cur.getDouble(2);
			acct.credit = (cur.getInt(3) == 0 ? false : true);
			acct.credit_limit = cur.getDouble(4);
			acct.timestamp = cur.getLong(5);
			
			mAccounts.add(acct);
		} while (cur.moveToNext());
		cur.close();
	}
	
	private void loadTransactions()
	{
		// TODO this entire structure may need to be re-done in order to facilitate
		// timestamps on a per-account basis to take into consideration accounts which
		// have not been previously synced but may have data older than the timestamp
		
		
		String selection = Transaction.NAME_ACCOUNT + " in (";
		
		// don't use mAccounts here, because that list gets obliterated when sending
		for (int acct_id : mAllowedAccountIds)
		{
			selection += acct_id + ",";
		}
		selection += ") and timestamp > ?";
		selection = selection.replace(",)", ")");
		
		Cursor cur = mResolver.query(
				Uri.parse("content://net.gumbercules.loot.transactionprovider/transaction"),
				new String[] { Transaction.NAME_ID, Transaction.NAME_ACCOUNT, Transaction.NAME_POSTED,
						Transaction.NAME_BUDGET, Transaction.NAME_CHECK_NUMBER,
						Transaction.NAME_DATE, Transaction.NAME_PARTY, Transaction.NAME_AMOUNT,
						Transaction.NAME_TAGS, Transaction.NAME_TIMESTAMP, "name" /* tag name */ },
				selection, new String[] { String.valueOf(0/*timestamp*/) }, 
				Transaction.NAME_ACCOUNT + " asc, " + Transaction.NAME_DATE + " asc");
		
		if (cur == null || !cur.moveToFirst())
		{
			return;
		}
		
		mTransactions = new ArrayList<Transaction>();
		Transaction trans = null;
		int id, last_id = -1;
		do
		{
			id = cur.getInt(0);
			
			if (id != last_id)
			{
				mTransactions.add(trans);
				trans.tags = new ArrayList<String>();
				trans = new Transaction();
				trans.id = id;
				trans.account = cur.getInt(1);
				trans.posted = (cur.getInt(2) == 0 ? false : true);
				trans.budget = (cur.getInt(3) == 0 ? false : true);
				trans.check_num = cur.getInt(4);
				trans.date = cur.getLong(5);
				trans.party = cur.getString(6);
				trans.amount = cur.getDouble(7);
				trans.timestamp = cur.getLong(8);
				
				if (trans.check_num > 0)
				{
					trans.type = Transaction.TYPE_CHECK;
				}
				else if (trans.amount > 0)
				{
					trans.type = Transaction.TYPE_DEPOSIT;
				}
				else
				{
					trans.type = Transaction.TYPE_WITHDRAW;
				}
			}
			
			if (!cur.isNull(10))
			{
				trans.tags.add(cur.getString(9));
			}
		} while (cur.moveToNext());
		cur.close();
		
		mTransactions.add(trans);
	}

	public static class Account
	{
		public static final String NAME_ACCOUNT			= "account";
		public static final String NAME_ID				= "id";
		public static final String NAME_NAME			= "name";
		public static final String NAME_INIT_BALANCE	= "balance";
		public static final String NAME_CREDIT			= "credit_account";
		public static final String NAME_CREDIT_LIMIT	= "credit_limit";
		public static final String NAME_TIMESTAMP		= "timestamp";
		
		int id;
		String name;
		double initial_balance;
		boolean credit;
		double credit_limit;
		long timestamp;
		
		public String toJson() throws JSONException
		{
			JSONObject json_object = new JSONObject();
			
			json_object.put(NAME_ACCOUNT, new JSONObject()
					.put(NAME_ID, id)
					.put(NAME_NAME, name)
					.put(NAME_INIT_BALANCE, initial_balance)
					.put(NAME_CREDIT, credit)
					.put(NAME_CREDIT_LIMIT, credit_limit)
					.put(NAME_TIMESTAMP, timestamp));
			
			return json_object.toString();
		}
		
		public static String toJson(List<Account> accounts) throws JSONException, NoMoreItemsException
		{
			if (accounts == null || accounts.isEmpty())
			{
				throw new NoMoreItemsException();
			}
			
			JSONArray json_array = new JSONArray();
			
			for (Account account : accounts)
			{
				json_array.put(account.toJson());
			}
			
			return json_array.toString();
		}
	}
	
	public static class Transaction
	{
		public static final int TYPE_DEPOSIT	= 0;
		public static final int TYPE_WITHDRAW	= 1;
		public static final int TYPE_CHECK		= 2;
		
		public static final String NAME_TRANSACTION		= "transaction";
		public static final String NAME_ID				= "id";
		public static final String NAME_ACCOUNT			= "account";
		public static final String NAME_POSTED			= "posted";
		public static final String NAME_BUDGET			= "budget";
		public static final String NAME_TYPE			= "type";
		public static final String NAME_CHECK_NUMBER	= "check_num";
		public static final String NAME_DATE			= "date";
		public static final String NAME_PARTY			= "party";
		public static final String NAME_AMOUNT			= "amount";
		public static final String NAME_TAGS			= "name";
		public static final String NAME_TIMESTAMP		= "timestamp";
		
		int id;
		int account;
		boolean posted;
		boolean budget;
		int type;
		int check_num;
		long date;
		String party;
		double amount;
		long timestamp;
		ArrayList<String> tags;
		ArrayList<String> images; // TODO images should be read from the URI and encoded into base64
		
		public String toJson() throws JSONException
		{
			JSONObject json_object = new JSONObject();
			
			JSONArray tag_array = new JSONArray();
			for (String tag : tags)
			{
				tag_array.put(tag);
			}
			
			json_object.put(NAME_TRANSACTION, new JSONObject()
					.put(NAME_ID, id)
					.put(NAME_ACCOUNT, account)
					.put(NAME_POSTED, posted)
					.put(NAME_BUDGET, budget)
					.put(NAME_TYPE, type)
					.put(NAME_CHECK_NUMBER, check_num)
					.put(NAME_DATE, date)
					.put(NAME_PARTY, party)
					.put(NAME_AMOUNT, amount)
					.put(NAME_TIMESTAMP, timestamp)
					.put(NAME_TAGS, tag_array));
			
			return json_object.toString();
		}
		
		public static String toJson(List<Transaction> transactions) throws JSONException, NoMoreItemsException
		{
			if (transactions == null || transactions.isEmpty())
			{
				throw new NoMoreItemsException();
			}
			
			JSONArray json_array = new JSONArray();
			
			for (Transaction trans : transactions)
			{
				json_array.put(trans.toJson());
			}
			
			return json_array.toString();
		}
	}
	
	public static class NoMoreItemsException extends Exception
	{
		private static final long serialVersionUID = -129663765038466541L;
	}
}
