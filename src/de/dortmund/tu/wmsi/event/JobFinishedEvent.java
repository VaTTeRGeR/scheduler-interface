package de.dortmund.tu.wmsi.event;

import de.dortmund.tu.wmsi.job.Job;

public class JobFinishedEvent  {

	private Job job;
	private long time;
	public JobFinishedEvent(long time, Job job) {
		this.time = time;
		this.job = job;
	}
	
	public Job getJob() {
		return job;
	}

	public long getTime() {
		return time;
	}
}
