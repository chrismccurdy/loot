package net.gumbercules.loot.premium;

import java.util.List;

import net.gumbercules.loot.account.Account;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class PremiumCaller
{
	public static final String IMPORT	= "net.gumbercules.loot.premium.IMPORT";
	public static final String EXPORT	= "net.gumbercules.loot.premium.EXPORT";
	public static final String CHART	= "net.gumbercules.loot.premium.CHART";
	
	private Activity mActivity;
	
	public PremiumCaller(Activity a)
	{
		mActivity = a;
	}
	
	public void showActivity(String action)
	{
		showActivity(action, -1, -1);
	}
	
	public void showActivity(String action, int account_id)
	{
		showActivity(action, account_id, -1);
	}
	
	public void showActivity(String action, int account_id, int request)
	{
		Intent i = new Intent(action, null);
		List<ResolveInfo> apps = mActivity.getPackageManager().queryIntentActivities(i, 0);
		if (!apps.isEmpty())
		{
			ComponentName targetComp = new ComponentName(apps.get(0).activityInfo.applicationInfo.packageName,
					apps.get(0).activityInfo.name);
			i = new Intent();
			i.setComponent(targetComp);
			
			if (account_id >= 0)
			{
				Account acct = Account.getAccountById(account_id);
				i.putExtra("id", account_id);
				i.putExtra("name", acct.name);
			}
		}
		
		try
		{
			mActivity.startActivityForResult(i, request);
		}
		catch (ActivityNotFoundException e)
		{
			i = new Intent(mActivity, PremiumNotFoundActivity.class);
			mActivity.startActivity(i);
		}
	}
}
