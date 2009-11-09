package net.gumbercules.loot;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PremiumNotFoundActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.premium);
		
		Button buy = (Button)findViewById(R.id.buy);
		
        buy.setOnClickListener(new Button.OnClickListener()
        {
			public void onClick(View v)
			{
	            Intent intent = new Intent(Intent.ACTION_VIEW);
	            intent.setData(Uri.parse("market://search?q=pname:net.gumbercules.loot.premium"));

	            try
	            {
	                startActivity(intent);
	            }
	            catch (ActivityNotFoundException e)
	            {
	                Toast.makeText(v.getContext(), R.string.no_market, Toast.LENGTH_SHORT).show();
	            }
			}       	
        });
	}
}
