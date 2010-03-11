package net.gumbercules.loot.premium;

import java.util.ArrayList;

import net.gumbercules.loot.R;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class ViewImage extends Activity implements OnGestureListener
{
	public static final String KEY_URIS		= "k_uris";
	
	private ArrayList<Uri> mUris;
	private ImageSwitcher mSwitcher;
	private GestureDetector mGesture;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mUris = new ArrayList<Uri>();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mUris = (ArrayList<Uri>)extras.get(KEY_URIS);
		}
		
		if (mUris.size() == 0)
		{
			Uri uri = getIntent().getData();
			
			if (uri != null)
			{
				mUris.add(uri);
			}
		}
		
		if (mUris.size() == 0)
		{
			finish();
		}
		
		setContentView(R.layout.animated_image_view);
		mGesture = new GestureDetector(this);
		mSwitcher = (ImageSwitcher)findViewById(R.id.ImageSwitcher);
		
		ImageView view;
		final LayoutParams fill = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		for (Uri uri : mUris)
		{
			view = new ImageView(this);
			view.setLayoutParams(fill);
			view.setAdjustViewBounds(true);
			view.setScaleType(ScaleType.FIT_CENTER);
			view.setImageURI(uri);
			mSwitcher.addView(view, fill);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me)
	{
		return mGesture.onTouchEvent(me);
	}
	
	@Override
	public boolean onDown(MotionEvent arg0)
	{
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		if (mUris.size() > 1)
		{
			if (velocityX < 0)
			{
				mSwitcher.setInAnimation(this, R.anim.slide_in_right);
				mSwitcher.setOutAnimation(this, R.anim.slide_out_left);
				mSwitcher.showNext();
			}
			else
			{
				mSwitcher.setInAnimation(this, R.anim.slide_in_left);
				mSwitcher.setOutAnimation(this, R.anim.slide_out_right);
				mSwitcher.showPrevious();
			}
		}
		
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) { 
		mSwitcher.showNext();
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) { }

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return false;
	}
}
