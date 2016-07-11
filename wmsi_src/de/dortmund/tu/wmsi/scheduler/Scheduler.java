package de.dortmund.tu.wmsi.scheduler;

import de.dortmund.tu.wmsi.job.Job;

public interface Scheduler {
	public void init(String configPath);
	public long simulateUntil(long t_now, long t_target);
	public void enqueueJob(Job job);
}