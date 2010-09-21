package net.gumbercules.loot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends Activity
{
	private static final String PAYPAL_URL	= "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&" +
			"hosted_button_id=1368357";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		
		Button pp_button = (Button)findViewById(R.id.pp_button);
		pp_button.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
	            Intent intent = new Intent(Intent.ACTION_VIEW);
	            intent.setData(Uri.parse(PAYPAL_URL));
                startActivity(intent);
			}
		});
	}
}
