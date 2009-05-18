package net.gumbercules.loot;

import java.text.NumberFormat;
import java.util.Currency;

import android.text.Editable;
import android.text.Selection;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class CurrencyKeyListener extends NumberKeyListener
{
	public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event)
	{
		Currency cur = NumberFormat.getInstance().getCurrency();
		String textStr = text.toString();
		int decimals = cur.getDefaultFractionDigits();
		int dot = textStr.lastIndexOf('.');
		char ch = event.getMatch(getAcceptedChars());
		
		// return successfully if we're backspacing
		Log.e("CurrencyKeyListener", "keyCode: " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_DEL ||
		// return successfully if we're using the arrow keys
				keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
				keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_UP ||
		// return if hitting enter or back keys
				keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK)
			return super.onKeyDown(view, text, keyCode, event);
		
		// return if it's not an accepted char
		if (ch == '\0')
			return true;
		
		// return successfully if there is no dot yet
		if (dot == -1)
			return super.onKeyDown(view, text, keyCode, event);

		// return if we're trying to add another dot when one is already present
		if (ch == '.')
			return true;
		
		int a = Selection.getSelectionStart(text);
		int b = Selection.getSelectionEnd(text);
		int start = Math.min(a, b);
		
		// return successfully if we're adding another digit before the dot
		if (start <= dot ||
		// return successfully if we're adding a digit after the dot and we haven't
		// yet reached the decimal limit for this currency
				(start > dot && (textStr.length() - dot <= decimals)))
			return super.onKeyDown(view, text, keyCode, event);

		return true;
	}

	@Override
	protected char[] getAcceptedChars()
	{
		return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};
	}

	public int getInputType()
	{
		return 0;
	}
}
