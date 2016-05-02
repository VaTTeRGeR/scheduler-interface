package de.dortmund.tu.wmsi.routine.timing;

public class RoutineTimingInterval implements RoutineTiming {

	private long tnow = Long.MIN_VALUE;
	private long tstart = Long.MIN_VALUE;
	private long ti = 0;
	
	public RoutineTimingInterval(long timeStart, long timeInterval) {
		tstart = timeStart;
		ti = timeInterval;
	}
	
	public long getNextTime() {
		if(tnow>tstart)
			return tstart + ti * (long)Math.ceil(((double)(tnow-tstart))/((double)ti));
		else return tstart;
	}

	public void update(long time) {
		tnow = time;
	}

}
