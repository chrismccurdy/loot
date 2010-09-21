package net.gumbercules.loot.backend;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Logger
{
	public static boolean logStackTrace(Exception e, Context c)
	{
		return logStackTrace(e, c, Environment.getExternalStorageDirectory().getPath() + "/loot");
	}
	
	public static boolean logStackTrace(Exception ex, Context c, String path)
	{
		File dir = new File(path);
		if (!dir.exists())
		{
			try
			{
				dir.mkdir();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			if (!dir.isDirectory())
			{
				try
				{
					dir = new File(path + '_' + (new Date()).getTime());
					dir.mkdir();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					return false;
				}
			}
		}
		
		String logFile = path + "/loot_" + (new Date()).getTime() + ".log";
		try
		{
			FileWriter fw = new FileWriter(logFile);
			PrintWriter pw = new PrintWriter(fw);
			ex.printStackTrace(pw);
			ex.printStackTrace();
			if (pw.checkError())
			{
				Log.e(Logger.class.toString(), "PrintWriter had an error");
				return false;
			}
			String msg = "Error logged to " + logFile;
			Log.i(Logger.class.toString(), msg);
			Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
