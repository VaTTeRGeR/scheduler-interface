package speinterface.util;

public class TimerExecuteOnce implements UPRExecutionTimer {

	private long t = Long.MIN_VALUE;
	
	public TimerExecuteOnce(long time) {
		t = time;
	}
	
	@Override
	public long getNextTime() {
		return t;
	}

	@Override
	public void update(long time) {}

}
