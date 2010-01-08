package net.gumbercules.loot;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

public class CurrencyWatcher implements TextWatcher
{
	private static final String TAG	= "net.gumbercules.loot.CurrencyWatcher";
	private String mOld;
	private final char mSeparator;
	private boolean mChanged;
	private final int mFractionDigits;
	private Character[] mAccepted;
	
	public CurrencyWatcher()
	{
		Log.i(TAG + ".CurrencyWatcher()", "Detected locale: " + Locale.getDefault().getDisplayName());
		
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		mChanged = false;
		String new_currency = Database.getOptionString("override_locale");
		Currency cur = null;
		if (new_currency != null && !new_currency.equals(""))
		{
			cur = Currency.getInstance(new_currency);
		}
		else
		{
			cur = NumberFormat.getInstance().getCurrency();
		}
		dfs.setCurrency(cur);
		mSeparator = dfs.getMonetaryDecimalSeparator();
		mFractionDigits = cur.getDefaultFractionDigits();
		mAccepted = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', mSeparator};
		Log.i(TAG + ".CurrencyWatcher()", "Separator: " + mSeparator);
	}
	
	protected char[] getAcceptedChars()
	{
		int len = mAccepted.length;
		char[] accepted = new char[len];
		for (int i = len - 1; i >= 0; --i)
		{
			accepted[i] = mAccepted[i];
		}
		return accepted;
	}

	public void afterTextChanged(Editable s)
	{
		String str = s.toString();		
		if (mChanged)
		{
			mChanged = false;
			mOld = str;
			return;
		}

		Log.i(TAG + ".afterTextChanged()", "Attempting input: " + str);

		final ArrayList<Character> accepted = new ArrayList<Character>(Arrays.asList(mAccepted));
		for (char c : str.toCharArray())
		{
			if (!accepted.contains(c))
			{
				s.replace(0, s.length(), mOld);
				return;
			}
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
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		mOld = s.toString();
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) { }	
}
