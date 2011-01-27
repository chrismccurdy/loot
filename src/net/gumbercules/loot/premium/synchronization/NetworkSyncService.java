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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

import net.gumbercules.loot.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class NetworkSyncService extends Service implements ISyncServer
{
	private byte[] mAddress;
	private NotificationManager mNM;
	private ServerThread mServerThread;
	private ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	@SuppressWarnings("unused")
	private static final String TAG	= "net.gumbercules.loot.premium.synchronization.NetworkSyncService";
	public static final int PORT 	= 15735;

	public static final String MSG_STATUS			= "ST";
	public static final String MSG_DETAIL			= "DT";
	public static final String MSG_ALLOWED_CLIENT	= "AC";
	public static final String MSG_CONNECTION_CODE	= "CC";
	
	public static final int MSG_REGISTER			= 1;
	public static final int MSG_SET_CLIENT			= 2;
	public static final int MSG_GET_CODE			= 3;
	public static final int MSG_BAD_CHECKSUM		= 4;
	public static final int MSG_NEED_ACCOUNTS		= 5;
	public static final int MSG_HAVE_ACCOUNTS		= 6;
	public static final int MSG_STOP_SERVICE		= -1;

	private void setNetworkInterfaces()
	{
		try
		{
			// try to select the wifi interface as the preferred interface
			byte[] preferred_address = null;

			for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces()))
			{
				for (InetAddress ipAddr : Collections.list(ni.getInetAddresses()))
				{
					if (!ipAddr.getHostAddress().equals("127.0.0.1"))
					{
						preferred_address = ipAddr.getAddress();
						if (ni.getDisplayName().contains("wlan"))
						{
							break;
						}
					}
				}
			}
			
			mAddress = preferred_address;
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}
	
	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Bundle bun = msg.getData();
			int status = bun.getInt(MSG_STATUS);
			String detail = bun.getString(MSG_DETAIL);
			
			if (status == SyncProtocol.ERROR_CLIENT_REJECTED)
			{
				// TODO: actually do stuff
				
				// client provided a bad checksum to the server
				if (detail.equals(SyncProtocol.CHECKSUM_BAD))
				{
					
				}
				// client was rejected by the access restriction
				else if (detail.equals(SyncProtocol.CLIENT_UNVERIFIED))
				{
					
				}
			}
			else if (status == MSG_STOP_SERVICE)
			{
				//stopSelf();
				if (mClients == null || mClients.size() == 0)
				{
					return;
				}
				
				for (int i = mClients.size() - 1; i >= 0; --i)
				{
					try
					{
						mClients.get(i).send(Message.obtain(null, MSG_STOP_SERVICE));
					}
					catch (RemoteException e)
					{
						// client is dead, remove it from the list
						mClients.remove(i);
					}
				}
			}
		}
	};
	
	private final Messenger mMessenger = new Messenger(new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_REGISTER:
					mClients.add(msg.replyTo);
					break;
					
				case MSG_SET_CLIENT:
					setAllowedClient(msg.getData().getString(MSG_ALLOWED_CLIENT));
					break;
					
				case MSG_GET_CODE:
					sendCode();
					break;
					
				case MSG_HAVE_ACCOUNTS:
					// TODO something here
					break;
					
				default:
					super.handleMessage(msg);
			}
		}
	});
	
	public void sendCode()
	{
		Message msg = Message.obtain(null, MSG_GET_CODE);
		Bundle bundle = new Bundle();
		bundle.clear();
		bundle.putString(MSG_CONNECTION_CODE, getId());
		msg.setData(bundle);
		
		for (int i = mClients.size() - 1; i >= 0; --i)
		{
			try
			{
				mClients.get(i).send(msg);
			}
			catch (RemoteException e)
			{
				// client is dead, remove it from the list
				mClients.remove(i);
			}
		}
	}

	public void setAllowedClient(String client)
	{
		Handler threadHandler = mServerThread.getHandler();
		Message msg = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.clear();
		bundle.putString(NetworkSyncService.MSG_ALLOWED_CLIENT, client);
		msg.setData(bundle);
		threadHandler.sendMessage(msg);
	}
	
	@Override
	public void onCreate()
	{
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		setNetworkInterfaces();
		startServer();
	}

	@Override
	public void onDestroy()
	{
		mNM.cancel(R.string.sync_service_started);
		Toast.makeText(this, R.string.sync_service_stopped, Toast.LENGTH_SHORT).show();
		stopServer();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mMessenger.getBinder();
	}

	@Override
	public String getId()
	{
		return SyncProtocol.computeConnectionCode(mAddress);
	}

	@Override
	public void startServer()
	{
		mServerThread = new ServerThread(mHandler);
		mServerThread.start();
	}

	@Override
	public void stopServer()
	{
		mServerThread.pleaseStop();
	}
	
	private void showNotification()
	{
		CharSequence text = getText(R.string.sync_service_started);
		Notification notification = new Notification(android.R.drawable.alert_light_frame, text,
				System.currentTimeMillis());
		
		// TODO: probably want a different activity here
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SyncActivity.class), 0);
		notification.setLatestEventInfo(this, getText(R.string.sync_title), text, pi);
		
		mNM.notify(R.string.sync_service_started, notification);
	}
	
	private class ServerThread extends Thread
	{
		private ServerSocket mServerSocket;
		private PrintWriter mOut;
		private BufferedReader mIn;
		private Bundle mBundle;
		private SyncProtocol mSp;
		private Handler mExtHandler;

		private Handler mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				Bundle bun = msg.getData();
				String client = bun.getString(MSG_ALLOWED_CLIENT);
				
				if (client != null && client.equals(""))
				{
					client = null;
				}
				
				mSp.setAllowedClient(client);
			}
		};

		public ServerThread(Handler pHandler)
		{
			mExtHandler = pHandler;
			mBundle = new Bundle();
			mSp = new SyncProtocol(mAddress);
			mSp.setContentResolver(getContentResolver());
		}
		
		public Handler getHandler()
		{
			return mHandler;
		}
		
		public void pleaseStop()
		{
			try
			{
				mServerSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void run()
		{
			Socket clientSocket = null;
			try
			{
				mServerSocket = new ServerSocket(PORT);
				clientSocket = mServerSocket.accept();
				
				// TODO: on client connection, a message should be sent to the activity to stop trying
				// to connect to a different server; activity should then kill the thread that attempts
				// to make this connection

				mOut = new PrintWriter(clientSocket.getOutputStream(), true);
				mIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				String client = SyncProtocol.computeConnectionCode(
						clientSocket.getInetAddress().getAddress());
				if (mSp.verifyClient(client))
				{
					processInputs();
				}
				else
				{
					notifyBadClient();
					mOut.println(SyncProtocol.CLIENT_UNVERIFIED);
				}
			}
			catch (SocketException e)
			{
				return;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InvalidClientException e)
			{
				sendMessage(SyncProtocol.ERROR_CLIENT_REJECTED, e.getMessage());
			}
			finally
			{
				cleanup(clientSocket);
			}
			
			// send a message to the activity to stop the service
			Log.i("TEEEEEEEEEEEEEEEEST", "done with the syncing process");
			sendMessage(MSG_STOP_SERVICE, null);
		}
		
		private void sendMessage(int status, String detail)
		{
			Message msg = Message.obtain();
			mBundle.clear();
			mBundle.putInt(MSG_STATUS, status);
			mBundle.putString(MSG_DETAIL, detail);
			msg.setData(mBundle);
			mExtHandler.sendMessage(msg);
		}
		
		private void processInputs() throws IOException, InvalidClientException
		{
			String outputLine, inputLine = null;
			
			outputLine = mSp.processInput(null);
			mOut.println(outputLine);
			
			while ((inputLine = mIn.readLine()) != null)
			{
				Log.i("TEEEEEEEEEEEEEEEEST", "GOT LINE:     " + inputLine);
				outputLine = mSp.processInput(inputLine);
				Log.i("TEEEEEEEEEEEEEEEEST", "SENDING LINE: " + outputLine);
				mOut.println(outputLine);
				if (outputLine.equals(SyncProtocol.CHECKSUM_BAD))
				{
					throw new InvalidClientException(outputLine);
				}
				else if (outputLine.equals(SyncProtocol.SYNC_DONE))
				{
					break;
				}
			}
		}
		
		private void notifyBadClient() throws IOException, InvalidClientException
		{
			String outputLine = SyncProtocol.CLIENT_UNVERIFIED;
			mOut.println(outputLine);
			throw new InvalidClientException(outputLine);
		}
		
		private void cleanup(Socket clientSocket)
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
			catch (IOException e) {	}
			if (clientSocket != null)
			{
				try
				{
					clientSocket.close();
				}
				catch (IOException e) { }
			}
			try
			{
				if (mServerSocket != null)
				{
					mServerSocket.close();
				}
			}
			catch (IOException e) { }
		}
	}
}
