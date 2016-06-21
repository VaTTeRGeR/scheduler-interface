package de.dortmund.tu.wmsi.routine;

import de.dortmund.tu.wmsi.routine.timing.RoutineTiming;

public abstract class WorkloadModelRoutine {
	private RoutineTiming timer = null;
	private long t_lastExecution = Long.MIN_VALUE;
	
	public WorkloadModelRoutine(RoutineTiming timer) {
		setExecutionTimer(timer);
	}
	
	public final void startProcessing(long t_now) {
		t_lastExecution = t_now;
		process(t_now);
	}
	
	protected abstract void process(long t_now);
	
	public void setExecutionTimer(RoutineTiming timer) {
		this.timer = timer;
	}
	
	public long getNextExecutionTime(long t_now){
		return timer.getNextTime(t_lastExecution, t_now);
	}
	
	public long getLastExecutionTime(){
		return t_lastExecution;
	}
}
