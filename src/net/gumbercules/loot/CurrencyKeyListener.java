package net.gumbercules.loot;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;

import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class CurrencyKeyListener extends NumberKeyListener
{
	private char mSeparator;
	
	public CurrencyKeyListener()
	{
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		mSeparator = dfs.getMonetaryDecimalSeparator();
	}
	
	public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event)
	{
		Currency cur = NumberFormat.getInstance().getCurrency();
		String textStr = text.toString();
		int decimals = cur.getDefaultFractionDigits();
		int separator = textStr.lastIndexOf(mSeparator);
		char ch = event.getMatch(getAcceptedChars());
		
		// return successfully if we're backspacing
		if (keyCode == KeyEvent.KEYCODE_DEL ||
		// return successfully if we're using the arrow keys
				keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
				keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_UP ||
		// return successfully if hitting enter or back keys
				keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK)
			return super.onKeyDown(view, text, keyCode, event);
		
		// return if it's not an accepted char
		if (ch == '\0')
			return true;
		
		int a = Selection.getSelectionStart(text);
		int b = Selection.getSelectionEnd(text);
		int start = Math.min(a, b);
		int end = text.length();
		
		// return successfully if there is no separator yet
		if (separator == -1 && (end - start) <= decimals)
			return super.onKeyDown(view, text, keyCode, event);

		// return if we're trying to add another separator when one is already present
		if (ch == mSeparator)
			return true;
		
		// return successfully if we're adding another digit before the separator
		if (start <= separator ||
		// return successfully if we're adding a digit after the separator and we haven't
		// yet reached the decimal limit for this currency
				(start > separator && (textStr.length() - separator <= decimals)))
			return super.onKeyDown(view, text, keyCode, event);

		return true;
	}

	@Override
	protected char[] getAcceptedChars()
	{
		return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', mSeparator};
	}

	public int getInputType()
	{
		return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
	}
	
	public static class CurrencyWatcher implements TextWatcher
	{
		private String mOld;
		private final char mSeparator;
		private boolean mChanged;
		private final int mFractionDigits;
		
		public CurrencyWatcher()
		{
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			mSeparator = dfs.getMonetaryDecimalSeparator();
			mChanged = false;
			Currency cur = NumberFormat.getInstance().getCurrency();
			mFractionDigits = cur.getDefaultFractionDigits();
		}
		
		public void afterTextChanged(Editable s)
		{
			String str = s.toString();
			Log.i(CurrencyWatcher.class.toString(), "Text changing from " + mOld + " to " + str);
			if (mChanged)
			{
				mChanged = false;
				mOld = str;
				Log.i(CurrencyWatcher.class.toString(), "Changed is true, not changing anymore");
				return;
			}
			
			int separator_count = 0;
			int last_sep = str.indexOf(mSeparator);
			int previous_sep = -1;
			
			// see if there is more than one separator being added
			while (last_sep != -1)
			{
				++separator_count;
				previous_sep = last_sep;
				last_sep = str.indexOf(mSeparator, last_sep + 1);
			}
			
			// check if there is a separator being added that would make
			// us exceed the maximum fractional digits
			if (separator_count > 1 || (previous_sep != -1 && 
					(s.length() - previous_sep - 1) > mFractionDigits))
			{
				mChanged = true;
				s.replace(0, s.length(), mOld);
				Log.i(CurrencyWatcher.class.toString(), "Denying change to " + str);
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
			mOld = s.toString();
		}

		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
		}	
	}
}
