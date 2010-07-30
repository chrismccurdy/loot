package net.gumbercules.loot.backend;

import java.io.File;

import net.gumbercules.loot.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

public class CopyThread extends Thread
{
	public static final int BACKUP	= 0;
	public static final int RESTORE	= 1;
	
	public static final int OFFLINE = 0;
	public static final int ONLINE	= 1;
	
	public static final Object[] sDataLock = new Object[0];
	
	public static final String TEMP_BACKUP = "backup.db";
	
	private Context mContext;
	private int mOp;
	private int mMod;
	private ProgressDialog mPd;
	private static boolean copyInProgress;
	
	public CopyThread(int op, int mod, ProgressDialog pd, Context con)
	{
		mOp = op;
		mMod = mod;
		mContext = con;
		mPd = pd;
		copyInProgress = false;
	}
	
	@Override
	public void run()
	{
		if (copyInProgress)
		{
			return;
		}
		else
		{
			copyInProgress = true;
		}

		Looper.prepare();
		
		int res = 0;
		if (mOp == BACKUP)
		{
			String backup_path = null;
			
			if (mMod == OFFLINE)
			{
				backup_path = Environment.getExternalStorageDirectory().getPath() +
					mContext.getResources().getString(R.string.backup_path);
			}
			else
			{
				backup_path = TEMP_BACKUP;
			}
			
			if (mPd != null)
			{
				FileWatcherThread fwt = new FileWatcherThread(Database.getDbPath(), backup_path, mPd);
				fwt.start();
			}
			
			synchronized (sDataLock)
			{
	    		if (Database.backup(backup_path))
	    		{
	    			res = R.string.backup_successful;
	    			if (mPd != null)
	    			{
	    				mPd.setProgress(100);
	    			}
	    		}
	    		else
	    		{
	    			res = R.string.backup_failed;
	    		}
			}
		}
		else if (mOp == RESTORE)
		{
			String backup_path = null;
			
			if (mMod == OFFLINE)
			{
				backup_path = Environment.getExternalStorageDirectory().getPath() +
					mContext.getResources().getString(R.string.backup_path);
			}
			else
			{
				backup_path = TEMP_BACKUP;
			}

			if (mPd != null)
			{
				FileWatcherThread fwt = new FileWatcherThread(backup_path, Database.getDbPath(), mPd);
				fwt.start();
			}

			synchronized (sDataLock)
			{
				if (Database.restore(backup_path))
	    		{
	    			res = R.string.restore_successful;
	    			if (mPd != null)
	    			{
	    				mPd.setProgress(100);
	    			}
	    			
	    			new Thread()
	    			{
	    				public void run()
	    				{
	    					try
	    					{
								Thread.sleep(3000);
							}
	    					catch (InterruptedException e) { }
	    					System.exit(0);
	    				}
	    			}.start();
	    		}
	    		else
	    		{
	    			res = R.string.restore_failed;
	    		}
			}
		}
		
		if (mPd != null)
		{
			mPd.dismiss();
		}
		copyInProgress = false;
		
		if (res != 0)
		{
			Toast.makeText(mContext, res, Toast.LENGTH_LONG).show();
		}
		
		Looper.loop();
	}

	private class FileWatcherThread extends Thread
	{
		private ProgressDialog mPd;
		private String mFilename;
		private long mTarget;
		
		public FileWatcherThread(String from_fn, String fn, ProgressDialog pd)
		{
			File fromFile = new File(from_fn);
			mTarget = fromFile.length();
			
			mFilename = fn;
			mPd = pd;
		}
		
		@Override
		public void run()
		{
			File mFile;
			
			try
			{
				int progress = 0;
				while (progress < 100)
				{
					mPd.setProgress(progress);
					Thread.sleep(100);
					mFile = new File(mFilename);
					progress = (int)(((float)mFile.length() / mTarget) * 100);
				}
			}
			catch (InterruptedException e) { }
		}
	}
}