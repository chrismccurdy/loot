package net.gumbercules.loot.premium;

import net.gumbercules.loot.R;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class ViewImage extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Uri uri = getIntent().getData();
		
		setContentView(R.layout.image_view);
		ImageView image = (ImageView)findViewById(R.id.ImageView);
		image.setImageURI(uri);
	}
}
