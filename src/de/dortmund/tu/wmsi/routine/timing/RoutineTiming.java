package de.dortmund.tu.wmsi.routine.timing;

public interface RoutineTiming {
	public long getNextTime(long t_lastExecution, long t_now);
}
