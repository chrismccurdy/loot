package net.gumbercules.loot.premium;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.gumbercules.loot.R;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
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
		
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = 4;
		
		try
		{
			mActive.setImageBitmap(resizeImage(mUris.get(mPosition), opt));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
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
		final int sz = mUris.size();
		if (sz > 1)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			boolean swapping = false;
			int in = 0, out = 0, position = 0;

			if (velocityX < 0 && mPosition < sz - 1)
			{
				swapping = true;
				position = ++mPosition;
				in = R.anim.slide_in_right;
				out = R.anim.slide_out_left;
			}
			else if (velocityX > 0 && mPosition > 0)
			{
				swapping = true;
				position = --mPosition;
				in = R.anim.slide_in_left;
				out = R.anim.slide_out_right;
			}
			
			if (swapping)
			{
				mInactive = mActive;
				mActive = (ImageView) mSwitcher.getNextView();
				Uri uri = mUris.get(position);
				
				try
				{
					mActive.setImageBitmap(resizeImage(uri, options));

					mSwitcher.setInAnimation(this, in);
					mSwitcher.setOutAnimation(this, out);
					mSwitcher.showNext();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			// this block of threads sleeps, then clears the inactive image view
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e) { }
					
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							mInactive.setImageBitmap(null);
						}
					});
				}
			}.start();
		}
		
		return true;
	}
	
	private Bitmap resizeImage(Uri uri, BitmapFactory.Options options)
		throws FileNotFoundException
	{
		String[] columns = new String[] { Images.Media.SIZE };
		Cursor cur = Images.Media.query(getContentResolver(), uri, columns);

		if (!cur.moveToFirst())
		{
			options.inSampleSize = 4;
		}
		else
		{
			options.inSampleSize = getSampleSize(cur.getLong(0));
		}
		
		return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
	}
	
	private int getSampleSize(long size)
	{
		int sampleSize = 0;
		long maxSize = 65536; // 2^16
		
		while (size > maxSize)
		{
			size /= 4;
			++sampleSize;
		}
		
		return sampleSize;
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
