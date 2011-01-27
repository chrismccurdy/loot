/*
 * This file is part of the loot project for Android.
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. This program is distributed in the 
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. You should have received a copy of the GNU General 
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2008, 2009, 2010, 2011 Christopher McCurdy
 */

/*
 * Portions of this file have been derived from code originally licensed
 * under the Apache License, Version 2.0.
 * 
 * Changes made by Christopher McCurdy, 2009.
 *
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 */

package net.gumbercules.loot.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerPreference extends DialogPreference
{
	private int mInitialColor;
    private int mCurrentColor;
    private ColorPickerView mCPView;

	private static class ColorPickerView extends View
	{
	    private Paint mPaint;
	    private Paint mCenterPaint;
	    private Paint mHSVPaint;
	    private final int[] mColors;
	    private int[] mHSVColors;
	    private boolean mRedrawHSV;
	    private OnColorChangedListener mListener;
	    private boolean mTrackingCenter;
	    private boolean mHighlightCenter;
	    private float mScale;
	    private RectF mHSVBar;
	    
	    private int HSV_LEFT;
	    private int HSV_RIGHT;
	    private int HSV_TOP;
	    
	    private int CENTER_X;
	    private int CENTER_Y;
	    private int CENTER_RADIUS;
	    
	    // density-independent pixel values
	    private static final int DI_CENTER_X = 125;
	    private static final int DI_CENTER_Y = 125;
	    private static final int DI_CENTER_RADIUS = 32;
	    
	    private static final float PI = 3.1415926f;

	    ColorPickerView(Context c, OnColorChangedListener l, int color)
	    {
	        super(c);
	        
	        mScale = c.getResources().getDisplayMetrics().density;
	        
	        CENTER_X = pixels(DI_CENTER_X);
	        CENTER_Y = pixels(DI_CENTER_Y);
	        CENTER_RADIUS = pixels(DI_CENTER_RADIUS);
	        
	        HSV_LEFT = pixels(-100);
	        HSV_RIGHT = pixels(100);
	        HSV_TOP = pixels(125);
	        mHSVBar = new RectF(HSV_LEFT, HSV_TOP, HSV_RIGHT, pixels(185));
	        
	        mListener = l;
	        mColors = new int[] {
	            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
	            0xFFFFFF00, 0xFFFF0000
	        };
	        Shader s = new SweepGradient(0, 0, mColors, null);
	        
	        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	        mPaint.setShader(s);
	        mPaint.setStyle(Paint.Style.STROKE);
	        mPaint.setStrokeWidth(CENTER_RADIUS);
	        
	        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	        mCenterPaint.setColor(color);
	        mCenterPaint.setStrokeWidth(pixels(5));
	        
	        mHSVColors = new int[] {
	        		0xFF000000, color, 0xFFFFFFFF
	        };
	
	        mHSVPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	        mHSVPaint.setStrokeWidth(pixels(10));
	        
	        mRedrawHSV = true;
	    }
	    	
	    public int getColor()
	    {
	    	return mCenterPaint.getColor();
	    }
	    
	    private int pixels(float f)
	    {
	    	return (int)(f * mScale);
	    }
	    
	    @Override 
	    protected void onDraw(Canvas canvas)
	    {
	        canvas.translate(CENTER_X, CENTER_Y);
	        
	        float r = pixels(mPaint.getStrokeWidth() * 2.25f);
	        
	        int c = mCenterPaint.getColor();
	
	        if (mRedrawHSV)
	        {
	            mHSVColors[1] = c;
	            mHSVPaint.setShader(new LinearGradient(-100, 0, 100, 0, 
	            		mHSVColors, null, Shader.TileMode.CLAMP));
	        }
	        
	        canvas.drawOval(new RectF(-r, -r, r, r), mPaint);            
	        canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
	        canvas.drawRect(mHSVBar, mHSVPaint);
	        
	        if (mTrackingCenter)
	        {
	            mCenterPaint.setStyle(Paint.Style.STROKE);
	            
	            if (mHighlightCenter)
	            {
	                mCenterPaint.setAlpha(0xFF);
	            }
	            else
	            {
	                mCenterPaint.setAlpha(0x80);
	            }
	            canvas.drawCircle(0, 0,
	                              CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
	                              mCenterPaint);
	            
	            mCenterPaint.setStyle(Paint.Style.FILL);
	            mCenterPaint.setColor(c);
	        }
	        
	        mRedrawHSV = true;
	    }
	    
	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	    {
	        setMeasuredDimension(CENTER_X*2, (int)(CENTER_Y*2.5));
	    }
	    
	    private int ave(int s, int d, float p)
	    {
	        return s + java.lang.Math.round(p * (d - s));
	    }
	    
	    private int interpColor(int colors[], float unit)
	    {
	        if (unit <= 0)
	        {
	            return colors[0];
	        }
	        if (unit >= 1)
	        {
	            return colors[colors.length - 1];
	        }
	        
	        float p = unit * (colors.length - 1);
	        int i = (int)p;
	        p -= i;
	
	        // now p is just the fractional part [0...1) and i is the index
	        int c0 = colors[i];
	        int c1 = colors[i+1];
	        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
	        int r = ave(Color.red(c0), Color.red(c1), p);
	        int g = ave(Color.green(c0), Color.green(c1), p);
	        int b = ave(Color.blue(c0), Color.blue(c1), p);
	        
	        return Color.argb(a, r, g, b);
	    }
	    
	    @Override
	    public boolean onTouchEvent(MotionEvent event)
	    {
	        float x = event.getX() - CENTER_X;
	        float y = event.getY() - CENTER_Y;
	        boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;
	        
	        switch (event.getAction()) 
	        {
	            case MotionEvent.ACTION_DOWN:
	                mTrackingCenter = inCenter;
	                if (inCenter)
	                {
	                    mHighlightCenter = true;
	                    invalidate();
	                    break;
	                }
	            case MotionEvent.ACTION_MOVE:
	                if (mTrackingCenter) 
	                {
	                    if (mHighlightCenter != inCenter)
	                    {
	                        mHighlightCenter = inCenter;
	                        invalidate();
	                    }
	                } 
	                // see if we're in the hsv slider
	                else if ((x >= HSV_LEFT & x <= HSV_RIGHT) && (y >= HSV_TOP))
	                {
	                	int a, r, g, b, c0, c1;
	                	float p;
	
	                	// set the center paint to this color
	                	if (x < 0)
	                	{
	                		c0 = mHSVColors[0];
	                		c1 = mHSVColors[1];
	                		p = (x + 100)/100;
	                	}
	                	else
	                	{
	                		c0 = mHSVColors[1];
	                		c1 = mHSVColors[2];
	                		p = x/100;
	                	}
	                	
	            		a = ave(Color.alpha(c0), Color.alpha(c1), p);
	            		r = ave(Color.red(c0), Color.red(c1), p);
	            		g = ave(Color.green(c0), Color.green(c1), p);
	            		b = ave(Color.blue(c0), Color.blue(c1), p);
	            		
	            		mCenterPaint.setColor(Color.argb(a, r, g, b));
	                	
	                	mRedrawHSV = false;
	                	invalidate();
	        		}
	        		else
	                {
	                    float angle = (float)java.lang.Math.atan2(y, x);
	                    // need to turn angle [-PI ... PI] into unit [0....1]
	                    float unit = angle/(2*PI);
	                    if (unit < 0) {
	                        unit += 1;
	                    }
	                    mCenterPaint.setColor(interpColor(mColors, unit));
	                    invalidate();
	                }
	                break;
	            case MotionEvent.ACTION_UP:
	                if (mTrackingCenter)
	                {
	                    if (inCenter) 
	                    {
	                        mListener.colorChanged(mCenterPaint.getColor());
	                    }
	                    mTrackingCenter = false;    // so we draw w/o halo
	                    invalidate();
	                }
	                break;
	        }
	        return true;
	    }
	}

	public interface OnColorChangedListener
	{
	    void colorChanged(int color);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if (positiveResult)
		{
			// set alpha to 0xEF
			mCurrentColor = (mCPView.getColor() & 0x00FFFFFF) | 0xEF000000;
			SharedPreferences.Editor editor = getEditor();
			editor.putInt(getKey(), mCurrentColor);
			editor.commit();
			callChangeListener(new Integer(mCurrentColor));
		}
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
		super.onPrepareDialogBuilder(builder);
		
		OnColorChangedListener l = new OnColorChangedListener()
		{
            public void colorChanged(int color)
            {
                mCurrentColor = color;
                onDialogClosed(true);
                getDialog().dismiss();
            }
		};
		
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		mInitialColor = prefs.getInt(getKey(), Color.LTGRAY);

		mCPView = new ColorPickerView(getContext(), l, mInitialColor);
		builder.setView(mCPView);
		builder.setPositiveButton(null, null);
		builder.setNegativeButton(null, null);
	}

}
