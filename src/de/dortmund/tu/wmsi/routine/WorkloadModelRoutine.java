package de.dortmund.tu.wmsi.routine;

import de.dortmund.tu.wmsi.routine.timing.RoutineTiming;

public abstract class WorkloadModelRoutine {
	RoutineTiming timer = null;

	public WorkloadModelRoutine(RoutineTiming timer) {
		setExecutionTimer(timer);
	}
	
	public abstract void process(long time);
	
	public void setExecutionTimer(RoutineTiming timer) {
		this.timer = timer;
	}
	
	public long getNextExecutionTime(){
		if(timer != null)
			return timer.getNextTime();
		else
			return Long.MIN_VALUE;
	}
}
