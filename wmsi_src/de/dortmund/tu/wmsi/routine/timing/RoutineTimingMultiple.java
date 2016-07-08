package de.dortmund.tu.wmsi.routine.timing;

import java.util.Arrays;

public class RoutineTimingMultiple implements RoutineTiming {

	private long t_execute[] = null;
	
	public RoutineTimingMultiple(long... times) {
		if(times == null || times.length == 0)
			throw new IllegalStateException("This RoutineTiming needs at least one execution time");
		t_execute = times;
		Arrays.sort(t_execute);
	}
	
	public long getNextTime(long t_lastExecution, long t_now) {
		int i;
		for (i = 0; i < t_execute.length; i++) {
			if(t_execute[i] > t_lastExecution && t_execute[i] >= t_now)
				return t_execute[i];
		}
		return Long.MAX_VALUE;
	}
}
