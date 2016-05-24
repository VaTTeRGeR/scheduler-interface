package de.dortmund.tu.wmsi.routine;

import de.dortmund.tu.wmsi.routine.timing.RoutineTiming;

public abstract class WorkloadModelRoutine {
	private RoutineTiming timer = null;
	private long lastExecutionTime = Long.MIN_VALUE;
	
	public WorkloadModelRoutine(RoutineTiming timer) {
		setExecutionTimer(timer);
	}
	
	public final void startProcessing(long time) {
		lastExecutionTime = time;
		process(time);
	}
	
	public abstract void process(long time);
	
	public void setExecutionTimer(RoutineTiming timer) {
		this.timer = timer;
	}
	
	public long getNextExecutionTime(long time){
		return timer.getNextTime(time);
	}
	
	public long getLastExecutionTime(){
		return lastExecutionTime;
	}
}
