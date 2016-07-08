package de.dortmund.tu.wmsi.routine.timing;

public class RoutineTimingOnce implements RoutineTiming {

	private long t_execute = Long.MIN_VALUE;
	
	public RoutineTimingOnce(long time) {
		t_execute = time;
	}
	
	public long getNextTime(long t_lastExecution, long t_now) {
		return t_execute > t_lastExecution ? t_execute :Long.MAX_VALUE;
	}
}
