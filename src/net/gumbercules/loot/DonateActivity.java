package net.gumbercules.loot;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class DonateActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		WebView wv = new WebView(this);
		wv.loadUrl(getResources().getString(R.string.donate_url));
		
		setContentView(wv);
	}
}
