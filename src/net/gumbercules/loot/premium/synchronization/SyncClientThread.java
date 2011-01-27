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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

import net.gumbercules.loot.premium.synchronization.DataInterface.Account;
import net.gumbercules.loot.premium.synchronization.DataInterface.NoMoreItemsException;
import net.gumbercules.loot.premium.synchronization.DataInterface.Transaction;
import net.gumbercules.loot.premium.synchronization.SyncProtocol.InvalidChecksumException;

import org.json.JSONException;

import android.content.ContentResolver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SyncClientThread extends Thread
{
	private String mConnectionCode;
	private Socket mSocket;
	private PrintWriter mOut;
	private BufferedReader mIn;
	private Handler mExtHandler;
	private ContentResolver mResolver;
	private DataInterface mData;
	private ArrayList<Integer> mAccounts;
	private String mServerUuid;
	private int mState = INIT_WAITING;

	public int mChunkSize;
	
	// protocol client states
	public static final int INIT_WAITING	= 0;
	public static final int SENT_CHECKSUM	= 1;
	public static final int SENT_UUID		= 2;
	public static final int SENT_TIMESTAMP	= 3;
	public static final int CLIENT_READY	= 4;
	public static final int ACCOUNTS_DONE	= 5;
	public static final int TRANS_DONE		= 6;
	public static final int DONE			= 99;
	
	public SyncClientThread(String connection_code, Handler ext_handler, ContentResolver cr)
	{
		mConnectionCode = connection_code;
		mExtHandler = ext_handler;
		mResolver = cr;
	}

	public void setAccounts(String selectedAccounts)
	{
		mAccounts = SyncActivity.accountStringToList(selectedAccounts);
	}
	
	@Override
	public void run()
	{
		try
		{
			// need to get the address from the connection code
			byte[] address = SyncProtocol.decodeConnectionCode(mConnectionCode);
			mSocket = new Socket(SyncProtocol.byteArrayToIp(address), NetworkSyncService.PORT);
			
			// TODO: on successful connection to the remote server, the client should send a message
			// to the activity to stop the server, so no more connections are made
			
			mOut = new PrintWriter(mSocket.getOutputStream(), true);
			mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			// TODO move this into a state in processInput
			// after the client and server have exchanged UUIDs to determine when the last
			// sync with this device was
			// mData = new DataInterface();
			mChunkSize = 5;

			String outputLine, inputLine = null;

			while ((inputLine = mIn.readLine()) != null)
			{
				Log.i("TEEEEEEEEEEEEEEEEST", "GOT LINE:     " + inputLine);
				outputLine = processInput(inputLine);
				Log.i("TEEEEEEEEEEEEEEEEST", "SENDING LINE: " + outputLine);
				if (outputLine == null || outputLine.equals(SyncProtocol.SYNC_DONE))
				{
					break;
				}
				else if (outputLine.equals(SyncProtocol.CHECKSUM_BAD))
				{
					// TODO change this to send it through a message to the activity
					// throw new InvalidClientException(outputLine);
				}

				mOut.println(outputLine);
			}
		}
		catch (ConnectException e)
		{
			// TODO connection refused: send a message to the activity
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalidChecksumException e)
		{
			Message msg = Message.obtain(mExtHandler, NetworkSyncService.MSG_BAD_CHECKSUM);
			msg.sendToTarget();
		}
		
		cleanup();
		
		// send a message to the activity to stop the service
		Log.i("TEEEEEEEEEEEEEEEEST", "done with the syncing process");
		Message msg = Message.obtain(mExtHandler);
		msg.what = NetworkSyncService.MSG_STOP_SERVICE;
		msg.sendToTarget();
	}
	
	private void cleanup()
	{
		if (mOut != null)
		{
			mOut.close();
		}
		
		try
		{
			if (mIn != null)
			{
				mIn.close();
			}
		}
		catch (IOException e) { }
		try
		{
			if (mSocket != null)
			{
				mSocket.close();
			}
		}
		catch (IOException e) { }
	}
	
	private String processInput(String in)
	{
		String out = null;
		
		if (mState == INIT_WAITING)
		{
			if (in.equals(SyncProtocol.CHECKSUM_NEEDED))
			{
				String[] addr_and_cs = SyncProtocol.getAddressAndChecksum(mConnectionCode);
				out = addr_and_cs[1]; // sending the checksum only
				mState = SENT_CHECKSUM;
			}
			else if (in.equals(SyncProtocol.CLIENT_UNVERIFIED))
			{
				out = SyncProtocol.SYNC_DONE;
				mState = DONE;
			}
		}
		else if (mState == SENT_CHECKSUM)
		{
			if (in.equals(SyncProtocol.CHECKSUM_BAD))
			{
				out = SyncProtocol.SYNC_DONE;
				mState = DONE;
			}
			else if (in.equals(SyncProtocol.CHECKSUM_OK))
			{
				out = SyncProtocol.getUuid();
				mState = SENT_UUID;
			}
		}
		else if (mState == SENT_UUID)
		{
			// TODO send the timestamps with the accounts
			mServerUuid = in;
			//out = SyncProtocol.getTimestamp(in, mResolver);
			mState = SENT_TIMESTAMP;
		}
		else if (mState == SENT_TIMESTAMP)
		{
			mData = new DataInterface(mResolver);
			mData.setRemoteUuid(mServerUuid);
			mData.setAllowedAccounts(mAccounts);
			out = SyncProtocol.CLIENT_READY;
			mState = CLIENT_READY;
		}
		else if (mState == CLIENT_READY)
		{
			if (in.equals(SyncProtocol.ACCOUNTS_NEEDED) || in.equals(SyncProtocol.ACCOUNTS_RECEIVED))
			{
				// send the accounts
				// change the state if there are no more accounts left
				try
				{
					out = Account.toJson(mData.getNextAccounts(mChunkSize));
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				catch (NoMoreItemsException e)
				{
					mState = ACCOUNTS_DONE;
					out = SyncProtocol.ACCOUNTS_DONE;
				}
			}
		}
		else if (mState == ACCOUNTS_DONE)
		{
			if (in.equals(SyncProtocol.TRANS_NEEDED))
			{
				// send the transactions
				// change the state if there are no more transactions left
				try
				{
					out = Transaction.toJson(mData.getNextTransactions(mChunkSize));
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				catch (NoMoreItemsException e)
				{
					mState = TRANS_DONE;
					out = SyncProtocol.TRANS_DONE;
				}
			}
		}
		else if (mState == TRANS_DONE)
		{
			// TODO don't end here
			if (in.equals(SyncProtocol.SYNC_DONE))
			{
				mState = DONE;
				out = SyncProtocol.SYNC_DONE;
			}
		}
		else if (mState == DONE)
		{
			return null;
		}
		
		return out;
	}
}
