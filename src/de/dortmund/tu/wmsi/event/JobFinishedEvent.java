package de.dortmund.tu.wmsi.event;

public class JobFinishedEvent  {

	private int jobId;
	private long time;
	public JobFinishedEvent(long time, int jobId) {
		this.time = time;
		this.jobId = jobId;
	}
	
	public int getJobId() {
		return jobId;
	}

	public long getTime() {
		return time;
	}
}
