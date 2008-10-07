package net.gumbercules.loot;

import java.util.Date;

public class RepeatSchedule
{
	// repetition iterator type
	public static final int NO_REPEAT	= 0;
	public static final int DAILY		= 1;
	public static final int WEEKLY		= 2;
	public static final int MONTHLY		= 3;
	public static final int YEARLY		= 4;
	public static final int CUSTOM		= 5;
	
	// custom weekly repetition
	public static final int SUNDAY		= 1 << 0;
	public static final int MONDAY		= 1 << 1;
	public static final int TUESDAY		= 1 << 2;
	public static final int WEDNESDAY	= 1 << 3;
	public static final int THURSDAY	= 1 << 4;
	public static final int FRIDAY		= 1 << 5;
	public static final int SATURDAY	= 1 << 6;
	
	// custom monthly repetition
	public static final int DAY			= 0;
	public static final int DATE		= 1;
	
	int iter;			// repetition iterator type
	int freq;			// number between repetitions
	int custom;			// used only for custom types
	Date start;			// start date
	Date end;			// end date
	Date due;			// date of the next repetition
	int id;				// id of the database repeat_pattern, if available
	
	public RepeatSchedule()
	{
		
	}
	
	public RepeatSchedule( int it, int fr, int cu, Date st, Date en, Date du, int id )
	{
		this.iter = it;
		this.freq = fr;
		this.custom = cu;
		this.start = st;
		this.end = en;
		this.due = du;
		this.id = id;
	}
}
