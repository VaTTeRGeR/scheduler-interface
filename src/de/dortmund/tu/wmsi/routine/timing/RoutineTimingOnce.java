package de.dortmund.tu.wmsi.routine.timing;

public class RoutineTimingOnce implements RoutineTiming {

	private long t = Long.MIN_VALUE;
	
	public RoutineTimingOnce(long time) {
		t = time;
	}
	
	public long getNextTime(long time) {
		return t;
	}
}
