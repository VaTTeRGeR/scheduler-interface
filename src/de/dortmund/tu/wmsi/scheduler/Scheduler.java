package de.dortmund.tu.wmsi.scheduler;

import de.dortmund.tu.wmsi.job.Job;

public interface Scheduler {
	public void init();
	public long simulateUntil(long t);
	public void enqueueJob(Job job);
	public void loadModelConfig(String path);
}
