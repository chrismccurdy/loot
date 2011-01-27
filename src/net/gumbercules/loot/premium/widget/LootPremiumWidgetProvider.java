package net.gumbercules.loot.premium.widget;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class LootPremiumWidgetProvider extends AppWidgetProvider
{
	private static final int BALANCE_ACTUAL		= 0;
	private static final int BALANCE_POSTED		= 1;
	private static final int BALANCE_BUDGETED	= 2;
	private static final int BALANCE_NONE		= 3;
	
	private static final String TAG				= "net.gumbercules.loot.premium.widget.LootPremiumWidgetProvider";
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		if (appWidgetIds == null)
		{
			return;
		}

		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();

		for (int widget_id : appWidgetIds)
		{
			prefs.remove(WidgetConfigure.PREFS_PREFIX + widget_id);
		}

		prefs.commit();
	}

	@Override
	public void onDisabled(Context context)
	{
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();
		Bundle extras = intent.getExtras();
		
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action))
		{
			final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
			{
				this.onDeleted(context, new int[] { appWidgetId });
			}
		}
		else if (action.equals("net.gumbercules.loot.intent.ACCOUNT_UPDATED"))
		{
			// find all appWidgetIds and update them
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			ArrayList<Integer> widget_list = new ArrayList<Integer>();
			ArrayList<Integer> account_list = new ArrayList<Integer>();
			ArrayList<Integer> bal_pref_list = new ArrayList<Integer>();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			Map<String, ?> all_prefs = prefs.getAll();
			int widget_id;

			if (extras != null)
			{
				int received_id = extras.getInt("account_id", -1);
				int transfer_id = extras.getInt("transfer_account", -1);
				int id;
				
				for (String key : all_prefs.keySet())
				{
					id = (Integer)all_prefs.get(key);
					if (id == received_id || id == transfer_id)
					{
						if (key.contains(WidgetConfigure.PREFS_PREFIX))
						{
							try
							{
								widget_id = Integer.valueOf(key.substring(
										WidgetConfigure.PREFS_PREFIX.length()));
								widget_list.add(widget_id);
								account_list.add(id);
								bal_pref_list.add(prefs.getInt(
										WidgetConfigure.WIDGET_BALANCE + widget_id, 0));
							} 
							catch (NumberFormatException e) { }
						}
					}
				}
			}
			else
			{
				for (String key : all_prefs.keySet())
				{
					if (key.contains(WidgetConfigure.PREFS_PREFIX))
					{
						try
						{
							widget_id = Integer.valueOf(key.substring(WidgetConfigure.PREFS_PREFIX.length()));
							widget_list.add(widget_id);
							account_list.add((Integer)all_prefs.get(key));
							bal_pref_list.add(prefs.getInt(WidgetConfigure.WIDGET_BALANCE + widget_id, 0));
						}
						catch (NumberFormatException e) { }
					}
				}
			}
			
			for (int i = 0; i < widget_list.size(); ++i)
			{
				updateWidget(context, manager, widget_list.get(i), account_list.get(i), bal_pref_list.get(i));
			}
		}
		else
		{
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int accountId;
		int balanceId;
		
		for (int widgetId : appWidgetIds)
		{
			accountId = prefs.getInt(WidgetConfigure.PREFS_PREFIX + widgetId, -1);
			balanceId = prefs.getInt(WidgetConfigure.WIDGET_BALANCE + widgetId, 0);
			if (accountId != -1)
			{
				updateWidget(context, appWidgetManager, widgetId, accountId, balanceId);
			}
		}
	}
	
	public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, int accountId)
	{
		updateWidget(context, appWidgetManager, appWidgetId, accountId, 0);
	}
	
	public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, int accountId, int balanceId)
	{
		String[] selectionArgs = new String[] { Integer.toString(accountId) };
		Cursor cur = context.getContentResolver().query(
				Uri.parse("content://net.gumbercules.loot.accountprovider/"),
				new String[]{"name", "balance"}, "purged = 0 and id = ?", 
				selectionArgs, null);
		
		if (cur == null || !cur.moveToFirst())
		{
			Log.i(TAG + ".updateWidget", "account cursor is null or no rows returned");
			return;
		}

		String account_name = cur.getString(0);
		double balance = cur.getDouble(1);
		
		cur.close();
		
		String selection = "account = ? and purged = 0 ";
		
		switch (balanceId)
		{
			case BALANCE_ACTUAL:
				selection += "and budget = 0";
				break;
				
			case BALANCE_POSTED:
				selection += "and posted = 1 and budget = 0";
				break;
				
			case BALANCE_BUDGETED:
				break;
		}
		
		cur = context.getContentResolver().query(
				Uri.parse("content://net.gumbercules.loot.transactionprovider/transaction/sum"),
				null, selection, selectionArgs, null);
		
		if (!cur.moveToFirst())
		{
			Log.i(TAG + ".updateWidget", "transaction cursor is empty");
			return;
		}
		
		balance += cur.getDouble(0);

		NumberFormat nf = NumberFormat.getCurrencyInstance();
		
		cur = context.getContentResolver().query(
				Uri.parse("content://net.gumbercules.loot.optionsprovider/override_locale"), 
				null, null, null, null);
		String new_currency = null;
		if (cur.moveToFirst())
		{
			new_currency = cur.getString(0);
		}
		cur.close();
		
		if (new_currency != null && !new_currency.equals(""))
		{
			nf.setCurrency(Currency.getInstance(new_currency));
		}
		
		String balance_text = nf.format(balance);
		
		int layout = R.layout.premium_widget;
		AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
		if (info != null)
		{
			if (info.label.toLowerCase().contains("(small)"))
			{
				layout = R.layout.premium_widget_2x1;
			}
		}
		else
		{
			Log.i(TAG + ".updateWidget", "Could not access appWidgetId " + appWidgetId);
			return;
		}
		
		RemoteViews views = new RemoteViews(context.getPackageName(), layout);

		Intent i = new Intent("net.gumbercules.action.WIDGET", null);
		i.addCategory("net.gumbercules.category.LAUNCHER");
		i.setData(Uri.parse("widget://net.gumbercules.loot/" + appWidgetId));
		List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(i, 0);

		if (apps.size() > 0)
		{
			i.putExtra("widget_id", appWidgetId);
			i.putExtra("account_id", accountId);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setOnClickPendingIntent(R.id.LinearLayout02, pi);
		}

        views.setTextViewText(R.id.account_text, account_name);
        if (balanceId == BALANCE_NONE)
        {
        	views.setViewVisibility(R.id.balance_text, View.INVISIBLE);
        }
        else
        {
        	views.setTextViewText(R.id.balance_text, balance_text);
        }
        
        appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}
