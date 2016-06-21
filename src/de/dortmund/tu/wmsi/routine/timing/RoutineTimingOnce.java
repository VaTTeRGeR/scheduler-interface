package de.dortmund.tu.wmsi.routine.timing;

public class RoutineTimingOnce implements RoutineTiming {

	private long t_executeOnce = Long.MIN_VALUE;
	
	public RoutineTimingOnce(long time) {
		t_executeOnce = time;
	}
	
	public long getNextTime(long t_lastExecution, long t_now) {
		return t_executeOnce > t_lastExecution ? t_executeOnce :Long.MAX_VALUE;
	}
}
