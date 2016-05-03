package de.dortmund.tu.wmsi.event;

@Deprecated
public class JobFinishedEvent extends Event {

	private int jobId;
	public JobFinishedEvent(long time, int jobId) {
		super(time);
		this.jobId = jobId;
	}
	
	public int getJobId() {
		return jobId;
	}
}
