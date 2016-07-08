package de.dortmund.tu.wmsi.event;

import de.dortmund.tu.wmsi.job.Job;

public class JobStartedEvent extends JobEvent {

	public JobStartedEvent(long time, Job job) {
		super(time, job);
	}
}
