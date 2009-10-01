package net.gumbercules.loot;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class Export
{
	private Context mContext;
	
	public Export(Context c)
	{
		mContext = c;
	}
	
	public void showExport()
	{
		showExport(-1);
	}
	
	public void showExport(int account_id)
	{
		Intent i = new Intent("net.gumbercules.loot.premium.EXPORT", null);
		List<ResolveInfo> apps = mContext.getPackageManager().queryIntentActivities(i, 0);
		if (!apps.isEmpty())
		{
			ComponentName targetComp = new ComponentName(apps.get(0).activityInfo.applicationInfo.packageName,
					apps.get(0).activityInfo.name);
			i = new Intent();
			i.setComponent(targetComp);
			
			if (account_id >= 0)
			{
				Account acct = Account.getAccountById(account_id);
				i.putExtra("name", acct.name);
			}
		}
		
		try
		{
			mContext.startActivity(i);
		}
		catch (ActivityNotFoundException e)
		{
			i = new Intent(mContext, PremiumNotFoundActivity.class);
			mContext.startActivity(i);
		}
	}
}
