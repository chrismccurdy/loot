package net.gumbercules.loot.backend;

import android.text.Editable;

public class NoDecimalCurrencyWatcher extends CurrencyWatcher
{
	private static final String TAG			= "net.gumbercules.loot.backend.CurrencyWatcher";
	
	public NoDecimalCurrencyWatcher()
	{
		super();
		
		mAccepted = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	}
	
	public char[] getAcceptedChars()
	{
		int len = mAccepted.length;
		char[] accepted = new char[len];
		for (int i = len - 1; i >= 0; --i)
		{
			accepted[i] = mAccepted[i];
		}
		return accepted;
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		super.afterTextChanged(s);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after)
	{
		super.beforeTextChanged(s, start, count, after);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		super.onTextChanged(s, start, before, count);
	}
}
