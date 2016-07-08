package de.dortmund.tu.wmsi.event;

import de.dortmund.tu.wmsi.job.Job;

public class JobFinishedEvent extends JobEvent {

	public JobFinishedEvent(long time, Job job) {
		super(time, job);
	}
}
