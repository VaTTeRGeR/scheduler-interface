package speinterface.util;

public abstract class UserProgrammeableRoutine {
	UPRExecutionTimer timer = null;

	public UserProgrammeableRoutine(UPRExecutionTimer timer) {
		setExecutionTimer(timer);
	}
	
	public abstract void process(long time);
	
	public void setExecutionTimer(UPRExecutionTimer timer) {
		this.timer = timer;
	}
	
	public long getNextExecutionTime(){
		if(timer != null)
			return timer.getNextTime();
		else
			return Long.MIN_VALUE;
	}
}
