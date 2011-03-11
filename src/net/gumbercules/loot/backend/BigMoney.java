package net.gumbercules.loot.backend;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

public final class BigMoney
{
	// get phone locale currency by default
	private static Currency mCurrency = NumberFormat.getInstance().getCurrency();
	
	public static final BigDecimal money(String str)
	{
		if (str == null || str.equals(""))
			str = "0.0";
		
		return new BigDecimal(str).setScale(mCurrency.getDefaultFractionDigits());
	}
	
	public static void setCurrency(Currency c)
	{
		mCurrency = c;
	}
}
