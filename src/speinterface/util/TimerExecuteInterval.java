package speinterface.util;

public class TimerExecuteInterval implements UPRExecutionTimer {

	private long tnow = Long.MIN_VALUE;
	private long tstart = Long.MIN_VALUE;
	private long ti = 0;
	
	public TimerExecuteInterval(long timeStart, long timeInterval) {
		tstart = timeStart;
		ti = timeInterval;
	}
	
	@Override
	public long getNextTime() {
		if(tnow>tstart)
			return tstart + ti * (long)Math.ceil(((double)(tnow-tstart))/((double)ti));
		else return tstart;
	}

	@Override
	public void update(long time) {
		tnow = time;
	}

}
