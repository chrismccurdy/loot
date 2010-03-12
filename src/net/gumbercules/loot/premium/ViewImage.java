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
import android.widget.ImageSwitcher;
import android.widget.ImageView;

public class ViewImage extends Activity implements OnGestureListener
{
	public static final String KEY_URIS		= "k_uris";
	
	private ArrayList<Uri> mUris;
	private ImageSwitcher mSwitcher;
	private ImageView mActive;
	private ImageView mInactive;
	private GestureDetector mGesture;
	
	private int mPosition;
	
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
		mActive = (ImageView)findViewById(R.id.FirstImage);
		
		mPosition = 0;
		mActive.setImageURI(mUris.get(mPosition));
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
		final int sz = mUris.size();
		if (sz > 1)
		{
			if (velocityX < 0 && mPosition < sz - 1)
			{
				mInactive = mActive;
				mActive = (ImageView) mSwitcher.getNextView();
				mActive.setImageURI(mUris.get(++mPosition));
				
				mSwitcher.setInAnimation(this, R.anim.slide_in_right);
				mSwitcher.setOutAnimation(this, R.anim.slide_out_left);
				mSwitcher.showNext();
			}
			else if (velocityX > 0 && mPosition > 0)
			{
				mInactive = mActive;
				mActive = (ImageView) mSwitcher.getNextView();
				mActive.setImageURI(mUris.get(--mPosition));
				
				mSwitcher.setInAnimation(this, R.anim.slide_in_left);
				mSwitcher.setOutAnimation(this, R.anim.slide_out_right);
				mSwitcher.showPrevious();
			}
		}
		
		mInactive.setImageURI(null);
		
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
