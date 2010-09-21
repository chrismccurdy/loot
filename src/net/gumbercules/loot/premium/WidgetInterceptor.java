package net.gumbercules.loot.premium;

import net.gumbercules.loot.PinActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WidgetInterceptor extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Intent i = new Intent(this, PinActivity.class);
		i.addCategory("net.gumbercules.category.LAUNCHER");
		i.putExtras(getIntent().getExtras());
		startActivity(i);
		finish();
	}
}
