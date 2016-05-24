package de.dortmund.tu.wmsi.routine.timing;

public class RoutineTimingInterval implements RoutineTiming {

	private long t_now = Long.MIN_VALUE;
	private long t_begin = Long.MIN_VALUE;
	private long t_interval = 0;
	
	public RoutineTimingInterval(long time_begin, long time_interval) {
		t_begin = time_begin;
		t_interval = time_interval;
	}
	
	public long getNextTime(long time) {
		if(time == t_now)
			t_now  = time + t_interval;
		else
			t_now = time;
		
		if(t_now > t_begin) {
			return t_begin + t_interval * (long)Math.ceil(((double)(t_now-t_begin))/((double)t_interval));
		} else
			return t_begin;
	}
}
