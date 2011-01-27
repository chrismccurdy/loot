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

package net.gumbercules.loot.premium.export;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.gumbercules.loot.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class ExportThread extends Thread
{
	private static final String TAG = "net.gumbercules.loot.premium.ExportActivity";
	private ProgressDialog mPd;
	private Activity mActivity;
	private Date mBegin;
	private Date mEnd;
	private int mAcctId;
	private int mFormat;
	private String mFilename;
	private int mRows;
	private SimpleDateFormat mSdf;
	
	private String[] mCsvOrder;
	private String mDebitKeyword;
	private String mCreditKeyword;
	
	public ExportThread(Activity a, ProgressDialog pd)
	{
		mPd = pd;
		mActivity = a;
	}
	
	@Override
	public void run()
	{
		Looper.prepare();
		
		mRows = 0;
		boolean success = exportAccount(mBegin, mEnd, mAcctId, mFormat);
		mPd.dismiss();

		String text;
		if (success)
		{
			if (mRows > 0)
			{
				text = mActivity.getResources().getString(R.string.export_done) + " " + mFilename;
			}
			else
			{
				text = mActivity.getResources().getString(R.string.export_no_rows);
			}
		}
		else
		{
			text = mActivity.getResources().getString(R.string.export_failed);
		}
		
		Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
		
		Looper.loop();
	}
	
	private boolean exportAccount(Date begin, Date end, int account_num, int format)
	{
		String[] columns = new String[] {"id", "date", "party", "amount", "check_num", "name"};
		String selection = "purged = 0 and account = " + account_num + " and (date between " +
				begin.getTime() + " and " + end.getTime() + ")";
		
		Cursor cur = mActivity.managedQuery(
				Uri.parse("content://net.gumbercules.loot.transactionprovider/transaction"),
				columns, selection, null, "date asc, id asc");
		
		if (!cur.moveToFirst())
		{
			Log.i(TAG + ".exportAccount", "no rows returned from query");
			return true;
		}
		
		BufferedWriter out = openOutputFile(format);
		if (out == null)
		{
			return false;
		}
		
		writeHeader(out, format);
		
		// iterate over the cursor and write each transaction
		HashMap<String, Object> trans = new HashMap<String, Object>();
		int id, last_id = -1;
		String tag = null;
		int count = cur.getCount();
		int i = 0;
		int progress = 0;
		
		setCsvOptions();
		
		if (format == ExportActivity.FORMAT_QUICKEN)
		{
			mSdf = getDateFormat("MM/dd/yyyy");
		}
		else if (format == ExportActivity.FORMAT_CSV)
		{
			mSdf = getDateFormat("yyyy/MM/dd");
		}
		
		do
		{
			id = cur.getInt(0);
			if (id != last_id)
			{
				if (trans.size() != 0)
				{
					exportTransaction(out, trans, format);
					progress = (int)((++i / (float)count) * 100);
					mPd.setProgress(progress);
				}
				trans.clear();
				trans.put("id", id);
				trans.put("date", cur.getLong(1));
				trans.put("party", cur.getString(2));
				trans.put("amount", cur.getDouble(3));
				trans.put("check_num", cur.getInt(4));
				trans.put("tags", "");
			}

			if (!cur.isNull(5))
			{
				tag = cur.getString(5);
				trans.put("tags", ((String)trans.get("tags")) + " " + tag);
			}
		} while (cur.moveToNext());
		
		exportTransaction(out, trans, format);
		mPd.setProgress(100);
		
		cur.close();
		writeFooter(out, format);
		try
		{
			out.close();
		}
		catch (IOException e)
		{
			Log.w(TAG + ".exportAccount",
					"IOException while closing the BufferedWriter. Contents of " +
					"buffer may not have been flushed.");
		}
		
		return true;
	}
	
	private String[] parseCsvString(String str)
	{
		if (str == null || str.equals("null"))
		{
			return null;
		}
		
		CsvReader reader;
		reader = new CsvReader(new ByteArrayInputStream(str.getBytes()),
				Charset.defaultCharset());
		
		String[] values = null;
		int size = 0;
		
		try
		{
			reader.readRecord();
		}
		catch (IOException e)
		{
			Log.e(TAG + ".parseCsvString", "IO exception reading record");
			return null;
		}
		
		size = reader.getColumnCount();
		
		if (size > 0)
		{
			values = new String[size];
		}
		
		for (int i = 0; i < size; ++i)
		{
			try
			{
				values[i] = reader.get(i);
			}
			catch (IOException e)
			{
				Log.e(TAG + ".parseCsvString", "IO exception getting value for column " + i);
			}
		}
		
		return values;
	}

	private void setCsvOptions()
	{
		String csv_uri = "content://net.gumbercules.loot.premium.settingsprovider/csv/";
		
		// parse the csv order
		mCsvOrder = new String[] { "%d", "%c", "%p", "%a", "%t" };
		ContentResolver cr = mActivity.getContentResolver();
		Cursor cur = cr.query(Uri.parse(csv_uri + "order"), null, null, null, null);
		if (cur != null && cur.moveToFirst())
		{
			String[] tmp = parseCsvString(cur.getString(1));
			if (tmp != null)
			{
				mCsvOrder = tmp;
			}
		}
		
		// parse the credit and debit values
		mCreditKeyword = "credit";
		mDebitKeyword = "debit";
		
		cur = cr.query(Uri.parse(csv_uri + "credit_type"), null, null, null, null);
		if (cur != null && cur.moveToFirst())
		{
			String[] tmp = parseCsvString(cur.getString(1));
			if (tmp != null)
			{
				mCreditKeyword = tmp[0];
			}
		}
		
		cur = cr.query(Uri.parse(csv_uri + "debit_type"), null, null, null, null);
		if (cur != null && cur.moveToFirst())
		{
			String[] tmp = parseCsvString(cur.getString(1));
			if (tmp != null)
			{
				mDebitKeyword = tmp[0];
			}
		}
	}

	private void exportTransaction(BufferedWriter out, HashMap<String, Object> trans, int format)
	{
		String outString = getExportString(format, trans);
		try
		{
			out.write(outString);
			out.flush();
			++mRows;
		}
		catch (IOException e)
		{
			Log.w(TAG + ".exportTransaction",
					"an IO error occurred while writing this transaction");
		}
	}

	private String getQuickenExportString(HashMap<String, Object> trans)
	{
		String out = "";
		
		Date d = new Date((Long)trans.get("date"));

		String date = mSdf.format(d);
		out += "D" + date + "\n";
		out += "P" + trans.get("party") + "\n";
		
		double amount = (Double)trans.get("amount");
		String format_str = (amount > 0 ? "%010.2f" : "%011.2f");
		
		String amt = String.format(format_str, amount);
		out += "T" + amt + "\n";
		
		int check_num = (Integer)trans.get("check_num");
		if (check_num > 0)
		{
			out += "N" + check_num + "\n";
		}
		
		if (trans.containsKey("tags"))
		{
			out += "M" + (String)trans.get("tags") + "\n";
		}
		
		out += "^\n";
		
		return out;
	}
	
	private SimpleDateFormat getDateFormat(String default_date)
	{
		ContentResolver cr = mActivity.getContentResolver();
		String uri = "content://net.gumbercules.loot.premium.settingsprovider/date_format";
		Cursor cur = cr.query(Uri.parse(uri), null, null, null, null);
		
		if (cur != null && cur.moveToFirst())
		{
			String date = cur.getString(1);
			if (date != null && !date.equals("null") && !date.equals(""))
			{
				default_date = date;
			}
		}

		return new SimpleDateFormat(default_date);
	}
	
	private String getCsvExportString(HashMap<String, Object> trans)
	{
		CharArrayWriter caw = new CharArrayWriter(256);
		CsvWriter writer = new CsvWriter(caw, ',');
		int len = mCsvOrder.length;
		String value;
		
		try
		{
			for (int i = 0; i < len; ++i)
			{
				value = getCsvColumnValue(mCsvOrder[i], trans);
				writer.write(value, true);
			}
			writer.endRecord();
			writer.close();
		}
		catch (IOException e)
		{
			Log.e(TAG + ".getCsvExportString", "Error writing to the CsvWriter");
		}
		
		caw.flush();
		return caw.toString();
	}
	
	private String getCsvColumnValue(String column, HashMap<String, Object> trans)
	{
		String value = "";
		
		if (column.equals("%d"))
		{
			Date d = new Date((Long)trans.get("date"));
			value = mSdf.format(d);
		}
		else if (column.equals("%c"))
		{
			value = ((Integer)trans.get("check_num")).toString();
		}
		else if (column.equals("%p"))
		{
			value = (String)trans.get("party");
		}
		else if (column.equals("%a"))
		{
			value = ((Double)trans.get("amount")).toString();
		}
		else if (column.equals("%b"))
		{
			Double amt = (Double)trans.get("amount");
			value = Double.toString(Math.abs(amt));
		}
		else if (column.equals("%t"))
		{
			value = (String)trans.get("tags");
		}
		else if (column.equals("%y"))
		{
			double amt = (Double)trans.get("amount");
			if (amt < 0.0)
			{
				value = mDebitKeyword;
			}
			else
			{
				value = mCreditKeyword;
			}
		}
		else if (column.equals("%z"))
		{
			// do nothing
		}
		
		return value;
	}
	
	private void writeFooter(BufferedWriter out, int format)
	{
		String outStr = null;
		if (format == ExportActivity.FORMAT_QUICKEN)
		{
			outStr = "\n\n\n";
		}
		
		if (outStr != null)
		{
			try
			{
				out.write(outStr);
				out.flush();
			}
			catch (IOException e) {	}
		}
	}

	private void writeHeader(BufferedWriter out, int format)
	{
		String outStr = null;
		if (format == ExportActivity.FORMAT_QUICKEN)
		{
			outStr = "!Type:Bank\n";
		}
		
		if (outStr != null)
		{
			try
			{
				out.write(outStr);
				out.flush();
			}
			catch (IOException e) {	}
		}
	}
	
	private BufferedWriter openOutputFile(int format)
	{
		File outFile = new File(Environment.getExternalStorageDirectory() + "/loot/export_" +
				System.currentTimeMillis() +
				getExportExtension(format));
		mFilename = outFile.getPath();
		
		File parentFile = outFile.getParentFile();
		if (!parentFile.exists())
		{
			if (!parentFile.mkdirs())
			{
				Log.e(TAG + ".openOutputFile", "could not create the required directories");
				return null;
			}
		}
		
		if (!outFile.exists())
		{
			try
			{
				if (!outFile.createNewFile())
				{
					Log.e(TAG + ".openOutputFile", "could not create the output file");
					return null;
				}
			}
			catch (IOException e)
			{
				Log.e(TAG + ".openOutputFile",
						"an IO error occurred trying to create the output file");
				return null;
			}
			catch (SecurityException e)
			{
				Log.e(TAG + ".openOutputFile",
						"could not get permission to create the output file");
				return null;
			}
		}

		BufferedWriter out;
		try
		{
			out = new BufferedWriter(new FileWriter(outFile), 8096);
		}
		catch (IOException e)
		{
			Log.e(TAG + ".openOutputFile", "could not open the output file for writing");
			return null;
		}
		
		return out;
	}
	
	private String getExportExtension(int format)
	{
		switch (format)
		{
			case ExportActivity.FORMAT_QUICKEN:
				return ".qif";
				
			case ExportActivity.FORMAT_CSV:
				return ".csv";
				
			default:
				return null;
		}
	}
	
	private String getExportString(int format, HashMap<String, Object> trans)
	{
		switch (format)
		{
			case ExportActivity.FORMAT_QUICKEN:
				return getQuickenExportString(trans);
				
			case ExportActivity.FORMAT_CSV:
				return getCsvExportString(trans);
				
			default:
				return null;
		}
	}

	public void setBegin(Date mBegin)
	{
		this.mBegin = mBegin;
	}

	public Date getBegin()
	{
		return mBegin;
	}

	public void setEnd(Date mEnd)
	{
		this.mEnd = mEnd;
	}

	public Date getEnd()
	{
		return mEnd;
	}

	public void setAcctId(int mAcctId)
	{
		this.mAcctId = mAcctId;
	}

	public int getAcctId()
	{
		return mAcctId;
	}

	public void setFormat(int mFormat)
	{
		this.mFormat = mFormat;
	}

	public int getFormat()
	{
		return mFormat;
	}
}
