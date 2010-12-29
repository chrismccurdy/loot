package net.gumbercules.loot.premium;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.widget.EditText;

public class DateButton
{
    public static void setDateEditListener(int y, int m, int day, Date date, EditText e)
    {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, y);
    	cal.set(Calendar.MONTH, m);
    	cal.set(Calendar.DAY_OF_MONTH, day);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	setDateEdit(cal.getTime(), date, e);
    }

    public static void setDateEdit(Date srcDate, Date dstDate, EditText dateEdit)
	{
		Calendar cal = Calendar.getInstance();
		if (srcDate != null)
			cal.setTime(srcDate);

		dstDate.setTime(srcDate.getTime());
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		dateEdit.setText(df.format(cal.getTime()));
	}
	
	public static Date parseDateEdit(EditText dateEdit)
	{
		Date date;
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		
		try
		{
			date = df.parse(dateEdit.getText().toString());
		}
		catch (ParseException e)
		{
			// set the date to today if there's a parsing error
			date = new Date();
		}
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		
		return date;
	}
	
	public static int[] dateEditToYMD(EditText dateEdit)
	{
		int[] ymd = new int[3];
		Date date = parseDateEdit(dateEdit);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		ymd[0] = cal.get(Calendar.YEAR);
		ymd[1] = cal.get(Calendar.MONTH);
		ymd[2] = cal.get(Calendar.DAY_OF_MONTH);
		
		return ymd;
	}
}
