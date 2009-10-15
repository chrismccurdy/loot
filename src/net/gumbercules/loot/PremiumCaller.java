package net.gumbercules.loot;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class PremiumCaller
{
	public static final String IMPORT	= "net.gumbercules.loot.premium.IMPORT";
	public static final String EXPORT	= "net.gumbercules.loot.premium.EXPORT";
	public static final String CHART	= "net.gumbercules.loot.premium.CHART";
	
	private Context mContext;
	
	public PremiumCaller(Context c)
	{
		mContext = c;
	}
	
	public void showActivity(String action)
	{
		showActivity(action, -1);
	}
	
	public void showActivity(String action, int account_id)
	{
		Intent i = new Intent(action, null);
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
