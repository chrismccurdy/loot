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
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		date = cal.getTime();
		
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
