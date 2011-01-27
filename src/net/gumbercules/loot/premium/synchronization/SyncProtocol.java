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

import java.util.UUID;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class SyncProtocol
{
	private static final int RADIX						= 36;
	
	// protocol server states
	private static final int WAITING					= 0;
	private static final int REQUESTED_CHECKSUM			= 1;
	private static final int VERIFIED_CLIENT			= 2;
	private static final int SENT_UUID					= 3;
	private static final int WAITING_READY				= 4;
	private static final int WAITING_ACCOUNTS			= 5;
	private static final int SENDING_ACCOUNTS			= 6;
	private static final int WAITING_TRANS				= 7;
	private static final int SENDING_TRANS				= 8;
	private static final int DONE						= 99;
	
	// protocol server errors
	public static final int ERROR_CLIENT_REJECTED		= 0; 
	public static final int STATUS_VERIFY_CLIENT		= 1;
	
	// protocol messages
	public static final String CHECKSUM_NEEDED			= "NEEDCS";
	public static final String CHECKSUM_OK				= "OKCS";
	public static final String CHECKSUM_BAD				= "BADCS";
	public static final String ACCOUNTS_NEEDED			= "NEEDACCTS";
	public static final String ACCOUNTS_RECEIVED		= "RECVACCTS";
	public static final String ACCOUNTS_DONE			= "DONEACCTS";
	public static final String TRANS_NEEDED				= "NEEDTRANS";
	public static final String TRANS_RECEIVED			= "RECVTRANS";
	public static final String TRANS_DONE				= "DONETRANS";
	public static final String CLIENT_READY				= "CRDY";
	public static final String CLIENT_UNVERIFIED		= "CUV";
	public static final String SYNC_DONE				= "BYE";
	
	private int mState = WAITING;
	private String mChecksum;
	private String mAllowedClient = null;
	private String mClientUuid;
	private DataInterface mData;
	private ContentResolver mResolver;
	
	public SyncProtocol(byte[] address)
	{
		mChecksum = calculateChecksum(address);
	}
	
	public String processInput(String in)
	{
		String out = null;
		
		// waiting for process to begin
		if (in != null && in.equals(SYNC_DONE))
		{
			mState = DONE;
			out = SYNC_DONE;
		}
		else if (mState == DONE)
		{
			out = SYNC_DONE;
		}
		else if (mState == WAITING)
		{
			out = CHECKSUM_NEEDED;
			mState = REQUESTED_CHECKSUM;
		}
		// sent initial response to client, waiting on the checksum to verify they're here properly
		else if (mState == REQUESTED_CHECKSUM)
		{
			if (verifyChecksum(in))
			{
				out = CHECKSUM_OK;
				mState = VERIFIED_CLIENT;
			}
			else
			{
				out = CHECKSUM_BAD;
				mState = DONE;
			}
		}
		else if (mState == VERIFIED_CLIENT)
		{
			mClientUuid = in;
			out = SyncProtocol.getUuid();
			mState = SENT_UUID;
		}
		else if (mState == SENT_UUID)
		{
			mData = new DataInterface(mResolver);
			// TODO send the timestamp with the accounts
			//out = SyncProtocol.getTimestamp(mClientUuid, mResolver);
			mState = WAITING_READY;
		}
		else if (mState == WAITING_READY)
		{
			if (in != null && in.equals(CLIENT_READY))
			{
				mState = WAITING_ACCOUNTS;
				out = ACCOUNTS_NEEDED;
			}
		}
		else if (mState == WAITING_ACCOUNTS)
		{
			if (in != null && in.equals(ACCOUNTS_DONE))
			{
				out = TRANS_NEEDED;
				mState = WAITING_TRANS;
			}
			else
			{
				// TODO: parse the JSON and sync with user database
				// state doesn't change if we have more accounts to receive
				out = ACCOUNTS_RECEIVED;
			}
		}
		else if (mState == WAITING_TRANS)
		{
			if (in != null && in.equals(TRANS_DONE))
			{
				// TODO: continue the syncing with transfers, repetitions, images
				out = SYNC_DONE;
			}
			else
			{
				// TODO: parse the JSON and sync with user database
				out = TRANS_RECEIVED;
			}
		}
		
		return out;
	}
	
	public boolean verifyClient(String client)
	{
		// check this client against the allowed client
		// if allowed client is unset, allow any client who connects
		if (mAllowedClient == null || client.equals(mAllowedClient))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean verifyChecksum(String checksum)
	{
		return checksum.equalsIgnoreCase(mChecksum);
	}
	
	public void setChecksum(byte[] address)
	{
		mChecksum = calculateChecksum(address);
	}
	
	public void setAllowedClient(String client)
	{
		mAllowedClient = client;
	}
	
	public void setContentResolver(ContentResolver cr)
	{
		mResolver = cr;
	}
	
	public static String calculateChecksum(byte[] a)
	{
		if (a == null || a.length < 4)
		{
			return null;
		}
		
		return calculateChecksum(new int[] { ubtoi(a[0]), ubtoi(a[1]), ubtoi(a[2]), ubtoi(a[3]) });
	}
	
	public static String calculateChecksum(int[] a)
	{
		int checksum = (((a[1] ^ a[3]) << 8 | (a[0] & a[2])) ^
				((a[1] & a[2]) << 8 | (a[0] ^ a[3]))) ^ NetworkSyncService.PORT;
		return Long.toString(checksum, RADIX);
	}
	
	// convert an unsigned byte to an integer
	private static int ubtoi(byte b)
	{
		return (int)b & 0xFF;
	}

	public static String computeConnectionCode(byte[] address)
	{
		long ip_long = (long)ubtoi(address[0]) << 24 | ubtoi(address[1]) << 16 | 
				ubtoi(address[2]) << 8 | ubtoi(address[3]);
		ip_long &= 0xFFFFFFFF;
		String id = Long.toString(ip_long, RADIX);
		int len = id.length();
		id += calculateChecksum(address) + String.format("%X", len);
		
		return id.toUpperCase();
	}
	
	public static byte[] decodeConnectionCode(String code) throws InvalidChecksumException
	{
		byte[] b_address = new byte[4];
		String[] addr_and_cs = getAddressAndChecksum(code);
		String s_address = addr_and_cs[0];
		String checksum = addr_and_cs[1];
		
		Long i_address = Long.valueOf(s_address, RADIX);
		for (int i = 3; i >= 0; --i)
		{
			b_address[i] = (byte)(i_address & 0xFF);
			i_address = i_address >> 8;
		}
		
		if (!checksum.equalsIgnoreCase(calculateChecksum(b_address)))
		{
			throw new InvalidChecksumException();
		}
		
		return b_address;
	}
	
	public static String[] getAddressAndChecksum(String code)
	{
		String[] ret = new String[2];
		int code_length = code.length();
		int checksum_start = Integer.valueOf(String.valueOf(code.charAt(code_length - 1)));
		ret[0] = code.substring(0, checksum_start);
		ret[1] = code.substring(checksum_start, code_length - 1);
		
		return ret;
	}
	
	public static String byteArrayToIp(byte[] a)
	{
		if (a.length < 4)
		{
			return null;
		}
		
		return String.format("%d.%d.%d.%d", ubtoi(a[0]), ubtoi(a[1]), ubtoi(a[2]), ubtoi(a[3]));
	}
	
	public static String getUuid()
	{
		/*Settings settings = new Settings("/data/data/net.gumbercules.loot.premium/settings");
		settings.read();
		String uuid = settings.getSetting("uuid");
		
		if (uuid == null)
		{
			uuid = UUID.randomUUID().toString();
			settings.updateSetting("uuid", uuid);
			settings.write();
		}
		
		return uuid;*/
		return null;
	}
	
	public static String getTimestamp(String device_uuid, int account_id, ContentResolver cr)
	{
		String out = "0";
		// get greatest timestamp corresponding with the server_uuid from synchronization table
		// in the database, send that timestamp to the server
		Uri uri = Uri.parse("content://net.gumbercules.loot.synchronizationprovider/" +
				device_uuid + "/" + account_id + "/latest");
		Cursor cur = cr.query(uri, null, null, null, null);
		if (cur != null && cur.moveToFirst())
		{
			// traverse the cursor until we get the timestamp value
			do
			{
				if (cur.getString(cur.getColumnIndex("key")).equals("timestamp"))
				{
					out = String.valueOf(cur.getLong(cur.getColumnIndex("value")));
					break;
				}
			} while (cur.moveToNext());

			cur.close();
		}
		
		return out;
	}

	public static class InvalidChecksumException extends Exception
	{
		private static final long serialVersionUID = -6769301166450912413L;
	}
}
