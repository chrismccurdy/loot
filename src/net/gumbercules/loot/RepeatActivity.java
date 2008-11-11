package net.gumbercules.loot;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TabHost;

public class RepeatActivity extends TabActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TabHost tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.repeat, tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec("Never")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel))
                .setContent(R.id.repeat_none));
        tabHost.addTab(tabHost.newTabSpec("Daily")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_day))
                .setContent(R.id.repeat_daily));
        tabHost.addTab(tabHost.newTabSpec("Weekly")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_week))
                .setContent(R.id.repeat_weekly));
        tabHost.addTab(tabHost.newTabSpec("Monthly")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_month))
                .setContent(R.id.repeat_monthly));
        tabHost.addTab(tabHost.newTabSpec("Yearly")
                .setIndicator("",
                		getResources().getDrawable(android.R.drawable.ic_menu_today))
                .setContent(R.id.repeat_yearly));
    }
}
