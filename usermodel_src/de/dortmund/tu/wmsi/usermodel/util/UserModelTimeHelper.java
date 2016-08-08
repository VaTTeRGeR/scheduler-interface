package de.dortmund.tu.wmsi.usermodel.util;

import java.util.concurrent.TimeUnit;

public class UserModelTimeHelper {

	private static int SECONDS_PER_MINUTE	= 60;
	private static int SECONDS_PER_HOUR		= 60*60;
	private static int SECONDS_PER_DAY		= 60*60*24;
	private static int SECONDS_PER_WEEK		= 60*60*24*7;
	
	
	public static String time(long secondsFromStart) {
		
		String dayArray[] = new String[7];
		dayArray[0] = "Mon";
		dayArray[1] = "Tue";
		dayArray[2] = "Wed";
		dayArray[3] = "Thu";
		dayArray[4] = "Fri";
		dayArray[5] = "Sat";
		dayArray[6] = "Sun";
		
		int week = (int)secondsFromStart/SECONDS_PER_WEEK;
		secondsFromStart = secondsFromStart % SECONDS_PER_WEEK;
		int day = (int)secondsFromStart/SECONDS_PER_DAY;
		secondsFromStart = secondsFromStart % SECONDS_PER_DAY;
		int hour = (int)secondsFromStart/SECONDS_PER_HOUR;
		secondsFromStart = secondsFromStart % SECONDS_PER_HOUR;
		int minute = (int)secondsFromStart/SECONDS_PER_MINUTE;
		secondsFromStart = secondsFromStart % SECONDS_PER_MINUTE;
		int seconds = (int)secondsFromStart;
		
		return "#" + week + ", " + dayArray[day] + ", " + hour + ":" + minute + ":" + seconds;
	}
	
	public static long toDays(long seconds) {
		return TimeUnit.SECONDS.toDays(seconds);
	}

	public static long toWeeks(long seconds) {
		return TimeUnit.SECONDS.toDays(seconds)/7L;
	}

	public static long toSeconds(long weeks) {
		return TimeUnit.DAYS.toSeconds(weeks*7L);
	}
}
